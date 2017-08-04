(ns icfp.t-core
  (:require [icfp.core :as sut]
            [clojure.test :refer :all]
            [fudje.sweet :refer :all]))

(deftest dummy
  (fact "it works"
    (sut/foobar 42) => "foo Dummy 42"))
