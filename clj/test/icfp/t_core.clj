(ns icfp.t-core
  (:require [icfp.core :as sut]))

(deftest dummy
  (fact "it works"
    (sut/foobar 42) => "foo Dummy 42"))
