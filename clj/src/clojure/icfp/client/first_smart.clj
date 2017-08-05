(ns icfp.client.first-smart
    (:require [icfp.util :as util]
              [icfp.client.framework :as client.fw]
              [jordanlewis.data.union-find :as u]
              [loom.graph :as g]
              [loom.alg :as ga]
              [icfp.scorer :refer [score]]))

(defn smart-init [state]
  (let [world (:world state)
        ;; graph (apply g/graph (seq (:rivers world)))
        sites (:sites world)
        mines (set (:mines world))
        ;; scored-shortest-path (into {}
        ;;                            (for [f sites
        ;;                                  t sites]
        ;;                              [(util/river f t)
        ;;                               (if (= f t)
        ;;                                 0
        ;;                                 (dec (count (ga/shortest-path graph f t))))]))
        unused-rivers (:rivers world)
        union (apply u/union-find sites)
        union-sites-count (into {} (map #(vector % 1) sites))
        union-mines-count (into {} (map #(vector % (if (mines %) 1 0)) sites))]
    (assoc state
           ;; :scored-shortest-path scored-shortest-path
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
        updated-state (-> state
                          (update :world util/consume-move move)
                          (update :unused-rivers
                                  #(if claim (disj % (:move claim)) %)))]
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

(defn score-move [state current-graph [from to]]
  (let [{:keys [union union-mines-count union-sites-count unused-rivers]} state
        from-head (union from)
        to-head (union to)]
    (+ (if (zero? (union-mines-count from))
         (count (get current-graph from))
         0)
       (if (zero? (union-mines-count to))
         (count (get current-graph to))
         0)
       (if (= from-head to-head)
         (* -1 (count unused-rivers))
         (+ (* (union-mines-count from-head) (union-sites-count to-head))
            (* (union-mines-count to-head) (union-sites-count from-head)))))))

(defn smart-decision [state]
  (let [current-graph (apply g/graph (seq (:unused-rivers state)))
        scores (->> (:unused-rivers state)
                    (map #(vector % (score-move state current-graph %)))
                    (sort #(> (second %1) (second %2))))
        [move score] (first scores)]
    move))

(defn make-client [& [offline?]]
  (client.fw/make-client smart-init smart-consume-move smart-decision offline?))
