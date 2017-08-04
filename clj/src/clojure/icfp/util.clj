(ns icfp.util)

(defn river [from to]
  (if (> to from) [from to] [to from]))
