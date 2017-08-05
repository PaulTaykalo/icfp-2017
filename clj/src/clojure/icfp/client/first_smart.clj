(ns icfp.client.first-smart
    (:require [cheshire.core :as json]
              [icfp.util :as util]
              [jordanlewis.data.union-find :as u]
              [loom.graph :as g]
              [loom.alg :as ga]
              [icfp.scorer :refer [score]]))

(defn make-client [init-state-function
                   consume-move-function
                   decision-function & [offline?]]
  "Take care of communication details.  Like save and restore state in offline mode.

   `init-state-function`, `consume-move-function` and `decision-function` do all
   intellectual job: enriching basic state with only world and id in it with
   custom payload, updating enriched state with new info on each move, and
   making a decision about the next move."
  (let [state (atom {:world nil
                     :id nil})]
    (fn [in & [dont-answer?]]
      (let [req (json/parse-string in true)]
        (cond (:move req)
              (do
                (when-let [saved-state (and offline?
                                            (:state req))]
                  (reset! state (read-string saved-state)))
                (reset! state
                        (reduce consume-move-function
                                @state
                                (-> req
                                    :move
                                    :moves)))
                (when-not dont-answer?
                  (let [[src tgt :as move] (decision-function @state)]
                    (json/encode
                     ((if offline?
                        #(assoc % :state (print-str @state))
                        identity)
                      {:claim {:punter (:id @state), :source src, :target tgt}})))))

              (:stop req) nil

              (:map req) (do (reset! state (-> {:world (-> (util/make-world (:map req) (:punters req))
                                                           ;; to reduce traffic
                                                           (dissoc :initial-json-map)
                                                           (assoc :moves-history nil))
                                                :id (:punter req)}
                                               init-state-function))
                             (json/encode ((if offline?
                                             #(assoc % :state (print-str @state))
                                             identity)
                                           {:ready (:id @state)
                                            :futures (map (fn [mine]
                                                            {:source mine
                                                             :target (rand-nth (seq (-> @state :world :sites)))})
                                                          (-> @state :world :mines))})))

              ;; ignore
              :else (do (println req)
                        nil))))))

(defn random-decision [state]
  (let [world (:world state)]
    (some #(when-not ((:claimed world) %)
             %)
          (shuffle (:rivers world)))))

(defn simple-consume-move [state move]
  (update state :world util/consume-move move))

(defn make-random-client [& [offline?]]
  (make-client identity simple-consume-move random-decision offline?))


;;;;;;;;;;;;;;;;;;

(defn smart-init [state]
  (let [world (:world state)
        graph (apply g/graph (seq (:rivers world)))
        sites (:sites world)
        mines (set (:mines world))
        scored-shortest-path (into {}
                                   (for [f sites
                                         t sites]
                                     [(util/river f t)
                                      (if (= f t)
                                        0
                                        (dec (count (ga/shortest-path graph f t))))]))
        unused-rivers (:rivers world)
        union (u/union-find sites)
        union-sites-count (into {} (map #(vector % 1) sites))
        union-mines-count (into {} (map #(vector % (if (mines %) 1 0)) sites))]
    (assoc state
           :scored-shortest-path scored-shortest-path
           :unused-rivers unused-rivers
           :union union
           :union-sites-count union-sites-count
           :union-mines-count union-mines-count)))

(defn get-claim [move]
  (when-let [claim (:claim move)]
    {:punter claim
     :move (util/river (:source claim) (:target claim))}))

(defn smart-consume-move [state move]
  (let [claim (get-claim move)
        updated-state (-> (update state :world util/consume-move move)
                          (update state :unused-rivers #(if claim (disj % (:river claim)) %)))]
    (if (and claim (= (:id state) (:punter claim)))
      (let [[from to] (:move claim)
            old-union (:union updated-state)
            [old-union old-from-head] (u/get-canonical old-union from)
            [old-union old-to-head] (u/get-canonical old-union to)
            new-union (u/union old-union from to)
            [new-union new-head] (u/get-canonical new-union from)
            union-sites-count (:union-sites-count state)
            union-mines-count (:union-mines-count state)]
        (-> updated-state
            (assoc :union new-union)
            (assoc-in [:union-sites-count new-head] (+ (get union-sites-count old-from-head)
                                                       (get union-sites-count old-to-head)))
            (assoc-in [:union-mines-count new-head] (+ (get union-mines-count old-from-head)
                                                       (get union-mines-count old-to-head)))))
      updated-state)))

(defn make-smart-random-client [& [offline?]]
  (make-client smart-init smart-consume-move random-decision offline?))
