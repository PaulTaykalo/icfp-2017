(ns icfp.util
  (:require [loom.graph :as g]
            [loom.alg :as ga]))

(defn river [from to]
  (if (> to from) [from to] [to from]))

(defn json-map->internal-map [json-map]
  (-> json-map
      (update :sites #(set (map :id %)))
      (update :rivers #(set (map (fn [r] (apply river ((juxt :source :target) r))) %)))))

(defn make-world [init-json-map punter-count]
  (let [internal-map (json-map->internal-map init-json-map)]
    (assoc internal-map
           :punter-count punter-count
           :initial-json-map init-json-map
           :claimed {}
           :punter-futures {}
           :moves-history (reverse
                           (for [i (range punter-count)]
                             {:pass {:punter i}}))
           :remaining-moves (count (:rivers internal-map)))))

(defn consume-move [world move]
  (-> (if-let [claim (and (not (:pass move)) (:claim move))]
        (let [to-claim (river (:source claim) (:target claim))]
          (if (and (get (:rivers world) to-claim)
                   (not (get (:claimed world) to-claim)))
            (assoc-in world [:claimed to-claim] (:punter claim))
            (throw (RuntimeException. "Invalid claim"))))
        world)
      (update :remaining-moves dec)))

(defn fast-shortest-paths [graph starts]
  (->> starts
       (mapcat
        (fn [mine]
          (ga/bf-traverse graph mine
                          :f (fn [node _ depth] (vector (river mine node) depth)))))
       (into {})))
