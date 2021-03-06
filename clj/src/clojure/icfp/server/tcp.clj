(ns icfp.server.tcp
  (:require [clojure.java.io :as io]
            [cheshire.core :as json])
  (:import [java.net InetSocketAddress Socket SocketTimeoutException ServerSocket InetAddress]))

(defn- send! [sock msg out-file]
  (let [msg (dissoc (json/parse-string msg true) :state)
        msg (if (:move msg)
              (update-in msg [:move :moves]
                         (fn [move] (map #(dissoc % :state) move)))
              msg)
        msg (json/encode msg)]
    (binding [*out* out-file]
      (println msg)))
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

(defn- recv! [sock out-file]
  (let [buff (byte-array 1000)
        rdr (.getInputStream sock)
        sz (read-size rdr)]
    (loop [left-bytes sz, result ""]
      (if (zero? left-bytes)
        (do (let [msg (dissoc (json/parse-string result true) :state)
                  msg (if (:move msg)
                        (update-in msg [:move :moves]
                                   (fn [move] (map #(dissoc % :state) move)))
                        msg)
                  msg (json/encode msg)]
              (binding [*out* out-file]
                (println msg)))
            result)
        (let [bytes-read (.read rdr buff)]
          (recur (- left-bytes bytes-read)
                 (str result (String. buff 0 bytes-read))))))))

(def punter-names (atom {}))

(defn start-tcp-server [port max-players game-loop-fn history-out-file]
  (let [punters (atom {})]
    (with-open [server-sock (ServerSocket. port 50 (InetAddress/getByName "0.0.0.0"))
                out-file (io/writer history-out-file)]
      (println "[INFO] Starting SimServerFutures on port" port)
      (while (< (count @punters) max-players)
        (println "[INFO] Waiting for" (- max-players (count @punters)) "more clients...")
        (let [sock (.accept server-sock)
              _ (println "[INFO] Connected:" sock)
              handshake (json/parse-string (recv! sock out-file) true)]
          (when (:me handshake)
            (swap! punter-names assoc (count @punters) (:me handshake))
            (send! sock (json/encode {:you (:me handshake)}) out-file)
            (swap! punters #(assoc % (count %) sock)))))
      (println "[INFO] All clients connected, starting game loop")
      (game-loop-fn
       (map (fn [[id punter-sock]]
              (fn [in & [dont-recv?]]
                (send! punter-sock in out-file)
                (when-not dont-recv?
                  (recv! punter-sock out-file))))
            @punters)))))

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

(defn make-offline-client [name client]
  (let [history (atom [])]
    (send! *out* (json/encode {:me name}) history)
    (when-not (= (json/encode {:you name}) (recv! *in* history))
      (throw (RuntimeException. "Wrong response")))
    (loop []
      (let [inp (recv! *in* history)
            parsed-inp (json/decode inp true)]
        (when (or (:map parsed-inp)
                  (:move parsed-inp))
          (send! *out* (client inp) history))
        (when-not (:stop parsed-inp)
          (recur))))))

#_(make-offline-client "hi" (icfp.client.random/make-client true))
