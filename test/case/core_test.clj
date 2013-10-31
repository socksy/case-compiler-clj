(ns case.core-test
  (:require [clojure.test :refer :all]
            [case.core :refer :all]))

(deftest a-test
  (testing "Plain int"
    (is (= (case.core/get-int 
             (case.core/run "IntTest" 
               (case.core/node :int 5)))
           5))))
