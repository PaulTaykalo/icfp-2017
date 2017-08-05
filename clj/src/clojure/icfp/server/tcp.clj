(ns icfp.server.tcp
  (:require [clojure.java.io :as io]
            [cheshire.core :as json])
  (:import [java.net InetSocketAddress Socket SocketTimeoutException ServerSocket InetAddress]))

(defn- send! [sock msg history]
  (swap! history conj msg)
  (doto (io/writer sock)
    (.write (str (count msg) ":"))
    (.write msg)
    (.flush)))

(defn- read-size [rdr]
  (loop [res ""]
    (let [c (char (.read rdr))]
      (if (= c \:)
        (Integer/parseInt res)
        (recur (str res c))))))

(defn- recv! [sock history]
  (let [buff (byte-array 1000)
        rdr (.getInputStream sock)
        sz (read-size rdr)]
    (loop [left-bytes sz, result ""]
      (if (zero? left-bytes)
        (do (swap! history conj result)
            result)
        (let [bytes-read (.read rdr buff)]
          (recur (- left-bytes bytes-read)
                 (str result (String. buff 0 bytes-read))))))))

(def punter-names (atom {}))

(defn start-tcp-server [port max-players game-loop-fn history-out-file]
  (let [punters (atom {})
        history (atom [])]
    (with-open [server-sock (ServerSocket. port 50 (InetAddress/getByName "0.0.0.0"))]
      (println "[INFO] Starting SimServerFutures on port" port)
      (while (< (count @punters) max-players)
        (println "[INFO] Waiting for" (- max-players (count @punters)) "more clients...")
        (let [sock (.accept server-sock)
              _ (println "[INFO] Connected:" sock)
              handshake (json/parse-string (recv! sock history) true)]
          (when (:me handshake)
            (swap! punter-names assoc (count @punters) (:me handshake))
            (send! sock (json/encode {:you (:me handshake)}) history)
            (swap! punters #(assoc % (count %) sock)))))
      (game-loop-fn
       (map (fn [[id punter-sock]]
              (fn [in & [dont-recv?]]
                (send! punter-sock in history)
                (when-not dont-recv?
                  (recv! punter-sock history))))
            @punters))
      (with-open [f (io/writer history-out-file)]
        (binding [*out* f]
          (run! println @history))))))

(defn make-tcp-client [name client host port]
  (let [history (atom [])]
    (with-open [socket (Socket. (InetAddress/getByName host) port)]
      (send! socket (json/encode {:me name}) history)
      (when-not (= (json/encode {:you name}) (recv! socket history))
        (throw (RuntimeException. "Wrong response")))
      (loop []
        (let [inp (recv! socket history)
              parsed-inp (json/decode inp true)]
          (when (or (:map parsed-inp)
                    (:move parsed-inp))
            (send! socket (client inp) history))
          (when-not (:stop parsed-inp)
            (recur)))))))
