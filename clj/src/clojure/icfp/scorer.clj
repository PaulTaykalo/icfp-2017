(ns icfp.scorer
  (:require [cheshire.core :as json]
            [loom.graph :as g]
            [loom.alg :as ga]
            [icfp.util :as util]))

(defn square [x] (* x x))
(defn cube [x] (* x x x))

(defn score [world]
  (let [graph (apply g/graph (seq (:rivers world)))
        mines (set (:mines world))
        sites (set (:sites world))
        futures (or (:punter-futures world) {})
        shortest-path (into {}
                            (for [f mines
                                  t sites]
                              [(util/river f t)
                               (if (= f t)
                                 0
                                 (dec (count (ga/shortest-path graph f t))))]))

        claimed (:claimed world)
        final-score (into {}
                          (for [punter (range (:punter-count world))]
                            [punter 0]))]
    (into final-score
          (for [[id owned] (group-by #(claimed %) (seq (:rivers world)))
                :when id]
            [id (let [connected (ga/connected-components (apply g/graph owned))]
                  (+
                   ;; Graph part
                   (reduce + 0
                           (for [conn connected]
                             (reduce + 0
                                     (for [from (filter mines conn)
                                           to conn]
                                       (square (shortest-path (util/river from to)))))))

                   ;; Futures
                   (->> (for [{:keys [mine site]} (get-in world [:punter-futures id])
                              :let [future-score (cube (shortest-path (util/river mine site)))]]
                          (if (some #(let [cs (set %)] (and (cs mine) (cs site)))
                                    connected)
                            future-score
                            (- future-score)))
                        (reduce +))))]))))

(defn -score [json-world punter-count json-moves]
  (let [world (util/make-world (json/decode json-world) punter-count)
        moves (json/decode json-moves)]
    (json/encode
     (map (fn [[punter score]]
            {:punter punter :score score})
          (score (reduce util/consume-move world moves))))))
