(ns icfp.core
  (:import icfp.Dummy))

(defn foobar [a]
  (str "foo " (Dummy/dummy a)))
