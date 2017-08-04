(ns icfp.core
  (:import icfp.Dummy
;           org.icfp2017.Main
           ))

(defn foobar [a]
  (str "foo " (Dummy/dummy a)))

#_(Main/main (into-array String ["foo" "bar"]))
