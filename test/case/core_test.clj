(ns case.core-test
  (:require [clojure.test :refer :all]
            [case.core :refer :all]))

(def testnames ["IntTest", "BoolTest", "AndTest", "OrTest", "AddTest", "MinusTest", "MultTest", "DivTest", "NestedAddsTest"])

(defn remove-clutter [f]
  (do (f)
      (map case.core/clean testnames)))
  
(use-fixtures :once remove-clutter)

(defn run-for-test [testname-no testnumber ast] 
    (case.core/run (str (get testnames testname-no) testnumber) ast))

(deftest int-test
  (testing "Plain ints"
    (is (= (case.core/get-int
             (run-for-test 0 1 
               (node :int 0)))
           0))

    (is (= (case.core/get-int
             (run-for-test 0 2 
               (node :int 5)))
           5))
    (is (= (case.core/get-int
             (run-for-test 0 3 
                (node :int 320)))
           320
           ))))

(deftest bool-test
  (testing "Plain bools"
    (is (= (case.core/get-bool
             (run-for-test 1 1
               (node :bool :true)))
           true))
    (is (= (case.core/get-bool
            (run-for-test 1 2
              (node :bool :false)))))))

(deftest and-test
  (testing "And"
    (is (= (case.core/get-bool
             (run-for-test 2 1
               (node :and (node :bool :true) (node :bool :true))))
           true))
    (is (= (case.core/get-bool
             (run-for-test 2 2
               (node :and (node :bool :false) (node :bool :true))))
           false))))


(deftest or-test
  (testing "And"
    (is (= (case.core/get-bool
             (run-for-test 3 1
               (node :or (node :bool :true) (node :bool :true))))
           true))
    (is (= (case.core/get-bool
             (run-for-test 3 2
               (node :or (node :bool :false) (node :bool :true))))
           true))
    (is (= (case.core/get-bool
             (run-for-test 3 3 
               (node :or (node :bool :false) (node :bool :false))))
           false))))

(deftest add-test
  (testing "Add"
    (is (= (case.core/get-int
             (run-for-test 4 1
               (node :add (node :int 3) (node :int 5))))
           8))
    (is (= (case.core/get-int
             (run-for-test 4 2
                (node :add (node :int 0) (node :int 101))))
           101))))


(deftest minus-test
  (testing "Minus"
    (is (= (case.core/get-int
             (run-for-test 5 1
               (node :minus (node :int 3) (node :int 5))))
           -2))
    (is (= (case.core/get-int
             (run-for-test 5 2
                (node :minus (node :int 10) (node :int 3))))
           7))))

(deftest mult-test
  (testing "Multiplication"
    (is (= (case.core/get-int
             (run-for-test 6 1
               (node :mult (node :int 3) (node :int 5))))
           15))
    (is (= (case.core/get-int
             (run-for-test 6 2
                (node :mult (node :int -1) (node :int 3))))
           -3))))

(deftest div-test
  (testing "Multiplication"
    (is (= (case.core/get-int
             (run-for-test 7 1
               (node :div (node :int 15) (node :int 5))))
           3))
    (is (= (case.core/get-int
             (run-for-test 7 2
                (node :div (node :int 10) (node :int 3))))
           3))))


(deftest nested
  (testing "Nested adds"
    ((is (= (case.core/get-int
             (run-for-test 8 1
                           (node :add (node :add (node :int 1) (node :int 2)) (node :int 2))) )
           5))
     (is (= (case.core/get-int
             (run-for-test 8 2 
                           (node :add (node :add (node :int 1) (node :int 2)) (node :add (node :int 5) (node :int 1)))) )
           9)))))
