(set-env! :dependencies '[[org.clojure/clojure "1.9.0-alpha17"]

                          [org.bytopia/boot-javac-star "0.1.0" :scope "test"]
                          [boot/core "2.7.1" :scope "provided"]
                          [fudje "0.9.7" :scope "test"]
                          [adzerk/boot-test "1.2.0" :scope "test"]
                          [cheshire "5.7.1"]
                          [org.jordanlewis/data.union-find "0.1.0"]
                          [aysylu/loom "1.0.0" :exclusions [org.clojure/clojurescript]]
                          [com.grammarly/omniconf "0.2.6"]]
          :source-paths #{"src/clojure" "src/java"}
          :test-paths #{"test/"}
          :resource-paths #{"res/"})

(task-options!
 pom  {:project 'lambada/icfp
       :version "0.1.1"})

(require '[boot.core :as core :refer [deftask]]
          '[boot.util :as util]
          '[boot.pod :as pod]
          '[boot.file :as file]
          'boot.repl
          '[clojure.set :as set]
          '[clojure.java.io :as io]
          '[clojure.walk :as walk])

(deftask collect-deps
  [d collect-dir    PATH   str      "Where to collect dependencies."
   s include-scope  SCOPE  #{str}  "The set of scopes to add."
   S exclude-scope  SCOPE  #{str}  "The set of scopes to remove."]
  (let [collect-dir (or collect-dir "deps")
        dfl-scopes #{"compile" "runtime" "provided"}
        scopes     (-> dfl-scopes
                       (set/union include-scope)
                       (set/difference exclude-scope))
        scope?     #(contains? scopes (:scope (util/dep-as-map %)))
        jars       (-> (core/get-env)
                       (update-in [:dependencies] (partial filter scope?))
                       pod/resolve-dependency-jars)
        jars       (remove #(.endsWith (.getName %) ".pom") jars)]
    (core/with-pre-wrap [fs]
      (when (seq jars)
        (util/info "Collecting dependencies...\n"))
      (.mkdir (io/file collect-dir))
      (doseq [jar jars
              :let [dst (io/file collect-dir (.getName jar))]]
        (println "Collecting" (.getName jar) "...")
        (file/copy-atomically jar dst))
      fs)))

(deftask build []
  (comp (javac) (aot :namespace '#{icfp.core icfp.server.server}) (pom) (jar)
        (collect-deps :collect-dir "jars" :exclude-scope #{"test" "provided"})
        (target)))

(deftask build-sim-server []
  (comp (javac) (aot :namespace '#{icfp.server.server})
        (collect-deps :collect-dir "jars" :exclude-scope #{"test" "provided"})
        (pom :project 'lambada/icfp-sim-server)
        (jar :file "sim-server-futures.jar" :main 'icfp.server.server)
        (target)))

(deftask build-clj-client []
  (comp (javac) (aot :namespace '#{icfp.client.client})
        (pom :project 'lambada/icfp-clj-client)
        (uber) (jar :file "clj-client.jar" :main 'icfp.client.client)
        (target)))

(require '[boot-javac-star.core :refer [javac*]])

#_(boot (javac*))
#_(set-env! :dependencies #(conj % '[org.icfp2017/kotlin-part "1.0.0"]))
