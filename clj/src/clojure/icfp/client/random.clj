(ns icfp.client.random
    (:require [icfp.util :as util]
              [icfp.client.framework :as client.fw]
              [jordanlewis.data.union-find :as u]
              [loom.graph :as g]
              [loom.alg :as ga]
              [icfp.scorer :refer [score]]))

(defn random-decision [state]
  (let [world (:world state)]
    (some #(when-not ((:claimed world) %)
             %)
          (shuffle (:rivers world)))))

(defn simple-consume-move [state move]
  (update state :world util/consume-move move))

(defn make-client [& [offline?]]
  (clint.fw/make-client identity simple-consume-move random-decision offline?))

