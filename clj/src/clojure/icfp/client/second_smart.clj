(ns icfp.client.second-smart
    (:require [icfp.util :as util]
              [icfp.client.framework :as client.fw]
              [jordanlewis.data.union-find :as u]
              [loom.graph :as g]
              [loom.alg :as ga]
              [icfp.scorer :as scorer]))

(defn smart-init [state]
  (let [world (:world state)
        graph (apply g/graph (seq (:rivers world)))
        sites (:sites world)
        mines (set (:mines world))
        scored-shortest-path (->> (util/fast-shortest-paths graph mines)
                                  (map (fn [[key val]] [key (scorer/square val)]))
                                  (into {}))
        unused-rivers (:rivers world)
        union (apply u/union-find sites)
        union-sites-count (into {} (map #(vector % 1) sites))
        union-mines-count (into {} (map #(vector % (if (mines %) 1 0)) sites))
        union-mines (->> sites
                         (keep #(when (mines %) [% [%]]))
                         (into {}))
        union-sites (->> sites
                         (map #(vector % [%]))
                         (into {}))]
    (assoc state
           :scored-shortest-path scored-shortest-path
           :unused-rivers unused-rivers
           :union union
           :union-sites-count union-sites-count
           :union-mines-count union-mines-count
           :union-mines union-mines
           :union-sites union-sites)))

(defn smart-consume-move [state move]
  (let [claim (util/get-claim move)
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
            union-mines-count (:union-mines-count state)
            union-mines (:union-mines state)
            union-sites (:union-sites state)]
        (-> updated-state
            (assoc :union new-union)
            (assoc-in [:union-sites-count new-head] (+ (get union-sites-count old-from-head)
                                                       (get union-sites-count old-to-head)))
            (assoc-in [:union-mines-count new-head] (+ (get union-mines-count old-from-head)
                                                       (get union-mines-count old-to-head)))
            (assoc-in [:union-mines new-head] (concat (get union-mines old-from-head)
                                                      (get union-mines old-to-head)))
            (assoc-in [:union-sites new-head] (concat (get union-sites old-from-head)
                                                      (get union-sites old-to-head)))))
      updated-state)))

(defn score-move [state current-graph [from to]]
  (let [{:keys [union
                union-mines-count
                union-sites-count
                union-mines
                union-sites
                scored-shortest-path
                unused-rivers]} state
        from-head (union from)
        to-head (union to)]
    (+ (reduce +
               (count (get current-graph from))
               (map #(scored-shortest-path % 0)
                    (union-mines to)))
       (reduce +
               (count (get current-graph to))
               (map #(scored-shortest-path % 0)
                    (union-mines from)))
       (if (zero? (union-mines-count from))
         (count (get current-graph from))
         0)
       (if (zero? (union-mines-count to))
         (count (get current-graph to))
         0)
       (if (= from-head to-head)
         (* -1 (count unused-rivers))
         (scorer/square
          (+ (* (union-mines-count from-head)
                (union-sites-count to-head))
             (* (union-mines-count to-head)
                (union-sites-count from-head))))))))

(defn smart-decision [state]
  (let [current-graph (apply g/graph (seq (:unused-rivers state)))
        scores (->> (:unused-rivers state)
                    (map #(vector % (score-move state current-graph %)))
                    (sort #(> (second %1) (second %2))))
        [move score] (first scores)]
    move))

(defn make-client [& [offline?]]
  (client.fw/make-client smart-init smart-consume-move smart-decision offline?))
