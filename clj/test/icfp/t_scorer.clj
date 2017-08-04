(ns icfp.t-scorer
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [icfp.scorer :as sut]
            [icfp.server.server :as server]
            [clojure.test :refer :all]
            [fudje.sweet :refer :all])
  (:import java.util.function.Function))

(def alice
  (let [move-id (atom -1)]
    (reify Function
      (apply [_ in]
        (swap! move-id inc)
        (json/encode
         (cond (= @move-id 0) {:ready 0}
               (<= 1 @move-id 6) (let [[src tgt :as move] (case @move-id
                                                            1 [0 1]
                                                            2 [2 3]
                                                            3 [4 5]
                                                            4 [6 7]
                                                            5 [1 3]
                                                            6 [5 7])]
                                   (println (format "[P%s] Making move %s" 0 move))
                                   {:claim {:punter 0, :source src, :target tgt}}
                                   )
               :else (do (println (format "[P%s] Stop! Hammertime!" 0))
                         {})))))))

(def bob
  (let [move-id (atom -1)]
    (reify Function
      (apply [_ in]
        (swap! move-id inc)
        (json/encode
         (cond (= @move-id 0) {:ready 1}
               (<= 1 @move-id 6) (let [[src tgt :as move] (case @move-id
                                                            1 [1 2]
                                                            2 [3 4]
                                                            3 [5 6]
                                                            4 [7 0]
                                                            5 [3 5]
                                                            6 [7 1])]
                                   (println (format "[P%s] Making move %s" 1 move))
                                   {:claim {:punter 1, :source src, :target tgt}})
               :else (do (println (format "[P%s] Stop! Hammertime!" 1))
                         {})))))))

(deftest scorer
  (fact "Small map is scored correctly."
        (sut/score {:sites #{0 1 2 3 4}
                    :rivers #{[0 1] [0 2] [0 3] [1 3] [2 3]}
                    :mines #{1 2}
                    :claimed {[0 1] 0,
                              [0 2] 0,
                              [0 3] 0,
                              [1 3] 1,
                              [2 3] 0}}) => {0 12, 1 1})
  (fact "Very small map is scored correctly."
        (sut/score {:sites #{0 1}
                    :rivers #{[0 1]}
                    :mines #{0}
                    :claimed {[0 1] 0}}) => {0 1, 1 0})
  (fact "Alice and Bob's match is scored correctly"
        (do (server/game-loop (slurp (io/resource "test-map.json"))
                              [alice bob])
            (sut/score @server/world)) => {0 6, 1 6}))
