(ns icfp.client.first-smart
    (:require [cheshire.core :as json]
              [icfp.util :as util]
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
