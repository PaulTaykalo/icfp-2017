(ns icfp.client.offline-helpers
  (:require [jordanlewis.data.union-find :as u]))

(defn- uf-serialize-to-edn [uf]
  [(map (fn [[k v]] [k (into {} v)]) (.elt-map uf))
   (.num-sets uf)])

(defn- uf-deserialize-from-edn [uf-edn]
  (let [[elts num-sets] uf-edn]
    (u/->PersistentDSF
     (into {} (map (fn [[k v]] [k (u/map->UFNode v)])) elts)
     num-sets nil)))

(defn prepare-to-send-offline-state [state]
  (update state :union uf-serialize-to-edn))

(defn prepare-received-offline-state [serialized-state]
  (update serialized-state :union uf-deserialize-from-edn))
