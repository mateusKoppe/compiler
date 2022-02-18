(ns compiler.grammar-test
  (:require [clojure.test :refer :all]
            [compiler.grammar :refer :all]))


(def if-grammar {:states {0 {:productions {\i [1]}, :is-final false}
                          1 {:productions {\f [2]}, :is-final false}
                          2 {:productions [], :is-final true}}
                 :token :if})

(deftest keywork->grammar-test
  (testing "Keywork->grammar with if keywork"
    (is (= (keywork->grammar "if") if-grammar))))