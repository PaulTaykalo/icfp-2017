(set-env! :dependencies '[[org.bytopia/boot-javac-star "0.1.0"]

                          [boot/core "2.7.1" :scope "provided"]
                          [fudje "0.9.7"]
                          [adzerk/boot-test "1.2.0"]]
          :source-paths #{"src/clojure" "src/java"}
          :test-paths #{"test/"}
          :resource-paths #{"res/"})

(task-options!
 pom  {:project 'lambada/icfp
       :version "0.1.0"})

(deftask build []
  (comp (javac) (aot :all true) (pom) (jar) (target)))

(require '[boot-javac-star.core :refer [javac*]])

#_(boot (javac*))
