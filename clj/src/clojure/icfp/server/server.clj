(ns icfp.server.server
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [icfp.util :refer [river]])
  (:import java.util.function.Function))

(defn json-map->internal-map [json-map]
  (-> json-map
      (update :sites #(set (map :id %)))
      (update :rivers #(set (map (fn [r] (apply river ((juxt :source :target) r))) %)))))

(defn make-world [internal-map init-json-map punter-count]
  (assoc internal-map
         :initial-json-map init-json-map
         :claimed {}
         :moves-history (reverse
                         (for [i (range punter-count)]
                           {:pass {:punter i}}))
         :remaining-moves (count (:rivers internal-map))))

(def world (atom nil))

(defn send-initial-state-to-punter [punter punter-id punters-count]
  (let [msg {:punter punter-id
             :punters punters-count
             :map (:initial-json-map @world)}
        resp  (json/decode (punter (json/encode msg)) true)]
    (when-not (= resp {:ready punter-id})
      (throw (ex-info (str "Wrong response from punter " punter-id ": " resp)
                      {})))))

(defn consume-move [move world]
  (-> (if-let [claim (and (not (:pass move)) (:claim move))]
        (let [to-claim (river (:source claim) (:target claim))]
          (if (and (get (:rivers world) to-claim)
                   (not (get (:claimed world) to-claim)))
            (assoc-in world [:claimed to-claim] (:punter claim))
            (throw (RuntimeException. "Invalid claim"))))
        world)
      (update :moves-history #(cons move %))
      (update :remaining-moves dec)))

(defn prompt-punter-for-move [punter punter-id punters-count]
  (let [msg {:move {:moves (sort-by (comp #(or (:claim %) (:path %)) :punter) (take punters-count (:moves-history @world)))}}
        resp (json/decode (punter (json/encode msg)) true)
        resp (assoc-in resp [:claim :punter] punter-id)]
    (dosync
     (reset! world (consume-move resp @world)))))

(defn send-stop-message-to-punter [punter punter-id punters-count]
  (let [msg {:stop {:moves (sort-by (comp #(or (:claim %) (:path %)) :punter) (take punters-count (:moves-history @world)))
                    :scores ()}}]
    (punter (json/encode msg))))

(defn game-loop [json-map-string punters]
  (reset! world (make-world (json-map->internal-map (json/parse-string json-map-string true))
                            (json/parse-string json-map-string true)
                            (count punters)))
  (let [punters (map #(fn [in] (.apply % in)) punters)]
    (dorun (map-indexed (fn [id punter]
                          (send-initial-state-to-punter punter id (count punters)))
                        punters))
    (while (> (:remaining-moves @world) 0)
      (dorun (map-indexed (fn [id punter]
                            (prompt-punter-for-move punter id (count punters)))
                          punters)))
    (dorun (map-indexed (fn [id punter]
                          (send-stop-message-to-punter punter id (count punters)))
                        punters))))

(defn random-punter [id]
  (reify Function
    (apply [_ in]
      (let [req (json/parse-string in true)]
        (cond (:move req)
              ;; Hodit'
              (let [[src tgt :as move] (some #(when-not ((:claimed @world) %)
                                                %)
                                             (shuffle (:rivers @world)))]
                (println (format "[P%s] Making move %s" id move))
                (json/encode
                 {:claim {:punter id, :source src, :target tgt}}))

              (:stop req)
              (do (println (format "[P%s] Stop! Hammertime!" id))
                  nil)

              :else (do (println (format "[P%s] Received initial state" id))
                        (json/encode {:ready id})))))))

(comment
  (game-loop (slurp (io/resource "test-map.json")) [(random-punter 0) (random-punter 1)])

  (send-initial-state-to-punter (random-punter 0) 0 2)
  (send-initial-state-to-punter (random-punter 1) 1 2)

  (prompt-punter-for-move (random-punter 0) 0 2)
  (prompt-punter-for-move (random-punter 1) 1 2)


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

  (game-loop (slurp (io/resource "test-map.json")) [alice bob]))


#_(slurp (io/resource "test-map.json"))
