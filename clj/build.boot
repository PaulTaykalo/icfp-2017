(set-env! :source-paths #{"src/clojure" "src/java"}
          :test-paths #{"test/"}
          :resource-paths #{"res/"})

(task-options!
 pom  {:project 'lambada/icfp
       :version "0.1.0"})

(deftask build []
  (comp (javac) (aot :all true) (pom) (jar) (target)))
