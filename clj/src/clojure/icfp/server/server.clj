(ns icfp.server.server
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [icfp.util :as util]
            [icfp.scorer :as scorer]
            [icfp.client.first-smart :as smart1]
            [icfp.server.tcp :as tcp]
            [omniconf.core :as cfg])
  (:import java.util.function.Function)
  (:gen-class))

(def world (atom nil))

(defn send-initial-state-to-punter [punter punter-id punters-count]
  (let [msg {:punter punter-id
             :punters punters-count
             :map (:initial-json-map @world)}
        resp  (json/decode (punter (json/encode msg)) true)]
    (when-not (= (:ready resp) punter-id)
      (throw (ex-info (str "Wrong response from punter " punter-id ": " resp)
                      {})))
    (:state resp)))

(defn- prepare-moves [world]
  (->> (:moves-history world)
       (take (:punter-count world))
       (sort-by (comp :punter #(or (:claim %) (:pass %))))))

(defn prompt-punter-for-move [punter punter-id state]
  (let [msg ((if state #(assoc % :state state) identity)
             {:move {:moves (prepare-moves @world)}})
        resp (json/decode (punter (json/encode msg)) true)
        resp (cond (:claim resp) (assoc-in resp [:claim :punter] punter-id)
                   (:pass resp) (assoc-in resp [:pass :punter] punter-id)
                   :else resp)]
    (dosync
     (reset! world (-> (util/consume-move resp @world)
                       (update :moves-history #(cons resp %)))))
    (:state resp)))

(defn send-stop-message-to-punter [punter punter-id]
  (let [score (scorer/score @world)
        msg {:stop {:moves (prepare-moves @world)
                    :scores (map (fn [[punter score]] {:punter punter
                                                       :score score})
                                 score)}}]
    (punter (json/encode msg) true)))

(defn game-loop [json-map-string punters]
  (reset! world (util/make-world
                 (json/parse-string json-map-string true)
                 (count punters)))
  (let [punters-state (atom {})]
    (dorun (map-indexed (fn [id punter]
                          (swap! punters-state
                                 assoc
                                 id
                                 (send-initial-state-to-punter punter id (count punters))))
                        punters))
    (while (> (:remaining-moves @world) 0)
      (dorun (map-indexed
              (fn [id punter]
                (swap! punters-state
                       #(let [state (get % id)]
                          (assoc % id
                                 (prompt-punter-for-move punter id state)))))
                          punters)))
    (dorun (map-indexed (fn [id punter]
                          (send-stop-message-to-punter punter id))
                        punters))))

(comment
  (game-loop (slurp (io/resource "test-map.json"))
             [(smart1/make-random-client true) (smart1/make-random-client true)])
  (game-loop (slurp (io/resource "test-map.json"))
             [(smart1/make-random-client) (smart1/make-random-client)])

  (scorer/-score (slurp (io/resource "test-map.json"))
                 2
                 "[{\"claim\":{\"punter\":0,\"source\":1,\"target\":3}},{\"claim\":{\"punter\":1,\"source\":5,\"target\":6}}]")
  
  (send-initial-state-to-punter (random-punter 0) 0 2)
  (send-initial-state-to-punter (random-punter 1) 1 2)

  (prompt-punter-for-move (random-punter 0) 0)
  (prompt-punter-for-move (random-punter 1) 1)

  (game-loop (slurp (io/resource "test-map.json")) [alice bob])

  (score @world)

  (game-loop (slurp (io/resource "test-map.json")) [alice bob])

  (future
    (tcp/start-tcp-server 13000 2
                         #(game-loop (slurp (io/file "res/test-map.json")) %)
                         (io/file "/tmp/hist.json")))

  (future
    (tcp/make-tcp-client (rand-nth ["Bob" "Alice" "Joe" "John" "Mary" "Sue"])
                         (smart1/make-random-client)
                         "localhost"
                         13000))
  )

(defn -main [& args]
  (cfg/define {:map-file {:description "path to JSON with map"
                          :type :file
                          :verifier cfg/verify-file-exists
                          :required true}
               :port {:description "port where to start server"
                      :type :number
                      :default 13000}
               :punters {:description "number of players to wait for"
                         :type :number
                         :required true}
               :out-file {:description "path to file where to print game sequence"
                          :type :file
                          :required true}})
  (when-not (seq args)
    (cfg/populate-from-cmd ["--help"])
    (System/exit 1))
  (cfg/populate-from-cmd args)
  (cfg/verify :quit-on-error true, :silent true)
  (tcp/start-tcp-server (cfg/get :port) (cfg/get :punters)
                        #(game-loop (slurp (cfg/get :map-file)) %)
                        (cfg/get :out-file)))

#_(slurp (io/resource "test-map.json"))
