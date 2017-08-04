(ns icfp.client.first-smart
    (:require [cheshire.core :as json]
              [icfp.util :as util]
              [icfp.scorer :refer [score]]))

(defn make-random-client []
  (let [world (atom nil)
        id (atom nil)]
    (fn [in & [dont-answer?]]
      (let [req (json/parse-string in true)]
        (cond (:move req)
              (do
                (doseq [move (:moves (:move req))]
                  (reset! world (util/consume-move move @world)))
                (let [[src tgt :as move] (some #(when-not ((:claimed @world) %)
                                                 %)
                                              (shuffle (:rivers @world)))]
                 (json/encode
                  {:claim {:punter @id, :source src, :target tgt}})))

              (:stop req) nil

              (:map req) (do (reset! world (util/make-world (:map req) (:punters req)))
                             (reset! id (:punter req))
                             (json/encode {:ready @id}))

              ;; ignore
              :else (do (println req)
                        nil))))))

