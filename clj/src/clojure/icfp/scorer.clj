(ns icfp.scorer
  (:require [loom.graph :as g]
            [loom.alg :as ga]
            [icfp.util :as util]))

(defn square [x] (* x x))

(defn score [world]
  (let [graph (apply g/graph (seq (:rivers world)))
        mines (set (:mines world))
        sites (set (:sites world))
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
          (for [[id owned] (group-by #(claimed %) (seq (:rivers world)))]
            [id (let [connected (ga/connected-components (apply g/graph owned))]
                  (reduce + 0
                          (for [conn connected]
                            (reduce + 0
                                    (for [from (filter mines conn)
                                          to conn]
                                      (square (shortest-path (util/river from to))))))))]))))