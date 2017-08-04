(ns icfp.scorer
  (:require [loom.graph :as g]
            [loom.alg :as ga]
            [icfp.util :as util]))

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

        claimed (:claimed world)]
    (for [[id owned] (group-by #(claimed %) (seq (:rivers world)))]
      (do (println "ID" id)
          [id (let [connected (ga/connected-components (apply g/graph owned))]
                (reduce + 0
                        (for [conn connected]
                          (reduce + 0
                                  (for [from (filter mines conn)
                                        to conn]
                                    (do (println from to (shortest-path (util/river from to)))
                                        (shortest-path (util/river from to))))))))]))))



