(ns icfp.server.tcp
  (:require [clojure.java.io :as io]
            [cheshire.core :as json])
  (:import [java.net InetSocketAddress Socket SocketTimeoutException ServerSocket InetAddress]))

;; (defn socket-println
;;   "Prints a string to the output stream of the socket, followed by a newline."
;;   [socket string]
;;   (let [w (io/writer socket)]
;;     (.write w (str string "\n"))
;;     (.flush w)))

;; (defn socket-readline
;;   [socket]
;;   (.readLine (io/reader socket)))

;; (defmacro with-tcp-socket
;;   "Opens a TCP connection to given `host` and `port`, and lexically binds it to
;;   `socket` symbol. Executes body in this scope."
;;   [[socket host port {:keys [timeout] :or {timeout 10000}}] & body]
;;   `(retriable {}
;;               (let [~socket (Socket.)
;;                     addr# (InetSocketAddress. ~host ~port)]
;;                 (try (.connect ^Socket ~socket addr# ~timeout)
;;                      (catch SocketTimeoutException ex#
;;                        (throw (SocketTimeoutException. (format "Connection to %s timed out."
;;                                                                (str addr#))))))
;;                 (.setSoTimeout ~socket ~timeout)
;;                 (try ~@body
;;                      (catch SocketTimeoutException ex#
;;                        (throw (SocketTimeoutException. (format "Read from %s timed out."
;;                                                                (str addr#)))))
;;                      (finally (.close ~socket))))))

(defn- send [sock msg]
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

(defn- recv [sock]
  (let [buff (byte-array 1000)
        rdr (.getInputStream sock)
        sz (read-size rdr)]
    (loop [left-bytes sz, result ""]
      (if (zero? left-bytes)
        result
        (let [bytes-read (.read rdr buff)]
          (recur (- left-bytes bytes-read)
                 (str result (String. buff 0 bytes-read))))))))

(defn start-tcp-server [port max-players game-loop-fn]
  (let [punters (atom {})]
    (with-open [server-sock (ServerSocket. port 50 (InetAddress/getByName "0.0.0.0"))]
      (println "[INFO] Starting SimServer on port" port)
      (while (< (count @punters) max-players)
        (println "[INFO] Waiting for" (- max-players (count @punters)) "more clients...")
        (let [sock (.accept server-sock)
              _ (println "[INFO] Connected:" sock)
              handshake (json/parse-string (recv sock) true)]
          (when (:me handshake)
            (send sock (json/encode {:you (:me handshake)}))
            (swap! punters #(assoc % (count %) sock)))))
      (game-loop-fn
       (map (fn [[id punter-sock]]
              (fn [in & [dont-recv?]]
                (send punter-sock in)
                (when-not dont-recv?
                  (recv punter-sock))))
            @punters)))))
