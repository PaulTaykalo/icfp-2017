(ns icfp.client.client
  (:require [icfp.server.tcp :as tcp]
            [omniconf.core :as cfg]
            icfp.client.first-smart)
  (:gen-class))

(defn -main [& args]
  (cfg/define {:server {:nested {:host {:description "host of the server"
                                        :type :string
                                        :default "localhost"}
                                 :port {:description "port of the server"
                                        :type :number
                                        :default 13000}}}
               :strategy {:description "which strat to use"
                          :type :keyword
                          :required true
                          :one-of [:random :smart-v1]}})
  (when-not (seq args)
    (cfg/populate-from-cmd ["--help"])
    (System/exit 1))
  (cfg/populate-from-cmd args)
  (cfg/verify :quit-on-error true, :silent true)
  (cfg/with-options [server strategy]
   (tcp/make-tcp-client (str "CLJ/" strategy)
                        (case strategy
                          :random (icfp.client.first-smart/make-random-client)
                          :smart-v1 (icfp.client.first-smart/make-smart-client))
                        (:host server) (:port server))))
