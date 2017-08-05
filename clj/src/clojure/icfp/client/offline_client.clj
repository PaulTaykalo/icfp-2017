(ns icfp.client.offline-client
  (:require [icfp.server.tcp :as tcp]
            [omniconf.core :as cfg]
            icfp.client.first-smart
            icfp.client.second-smart
            icfp.client.random
            )
  (:gen-class))

(defn -main [& args]
  (cfg/define {:strategy {:description "which strat to use"
                          :type :keyword
                          :required true
                          :one-of [:random :smart-v1 :smart-v2]}})
  (when-not (seq args)
    (cfg/populate-from-cmd ["--help"])
    (System/exit 1))
  (cfg/populate-from-cmd args)
  (cfg/verify :quit-on-error true, :silent true)
  (cfg/with-options [server strategy]
   (tcp/make-offline-client (str "CLJ/" strategy)
                            ((case strategy
                               :random icfp.client.random/make-client
                               :smart-v1 icfp.client.first-smart/make-client
                               :smart-v2 icfp.client.second-smart/make-client)
                             true))))
