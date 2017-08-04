(ns icfp.server.server
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [icfp.util :as util]
            [icfp.scorer :as scorer]
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
    (when-not (= resp {:ready punter-id})
      (throw (ex-info (str "Wrong response from punter " punter-id ": " resp)
                      {})))))

(defn prompt-punter-for-move [punter punter-id punters-count]
  (let [msg {:move {:moves (sort-by (comp #(or (:claim %) (:pass %)) :punter) (take punters-count (:moves-history @world)))}}
        resp (json/decode (punter (json/encode msg)) true)
        resp (assoc-in resp [:claim :punter] punter-id)]
    (dosync
     (reset! world (util/consume-move resp @world)))))

(defn send-stop-message-to-punter [punter punter-id punters-count]
  (let [score (scorer/score @world)
        msg {:stop {:moves (sort-by (comp #(or (:claim %) (:pass %)) :punter) (take punters-count (:moves-history @world)))
                    :scores (map (fn [[punter score]] {:punter punter
                                                      :score score})
                                 score)}}]
    (punter (json/encode msg) true)))

(defn game-loop [json-map-string punters]
  (reset! world (util/make-world
                 (json/parse-string json-map-string true)
                 (count punters)))
  (let [;punters (map #(fn [in] (.apply % in)) punters)
        ]
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

  (game-loop (slurp (io/resource "test-map.json")) [alice bob])

  (score @world)

  (game-loop (slurp (io/resource "test-map.json")) [alice bob])

  (tcp/start-tcp-server 13000 2
                        #(game-loop (slurp (io/file "res/london-tube.json")) %)
                        (io/file "/tmp/hist.json"))
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
