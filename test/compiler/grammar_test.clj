(ns compiler.grammar-test
  (:require [clojure.test :refer :all]
            [compiler.grammar :refer :all]))


(def if-grammar {:start-state 0
                 :states {0 {:productions {"i" [1]}}
                          1 {:productions {"f" [2]}}
                          2 {:productions {}, :final-token :if}}})

(def end-grammar {:start-state 0
                 :states {0 {:productions {"e" [1]}}
                          1 {:productions {"n" [2]}}
                          2 {:productions {"d" [3]}}
                          3 {:productions {}, :final-token :end}}})

(deftest keywork->grammar-test
  (testing "Keywork->grammar with if keywork"
    (is (= (keywork->grammar :if) if-grammar))))

(deftest merge-grammar
  (testing "Mergeging if grammar and end grammar"
    (is (= (merge-grammar if-grammar end-grammar)
           {:start-state 0
            :states {0 {:productions {"i" [1] "e" [3]}}
                     1 {:productions {"f" [2]}}
                     2 {:productions {}, :final-token :if}
                     3 {:productions {"e" [4]}}
                     4 {:productions {"n" [5]}}
                     5 {:productions {"d" [6]}}
                     6 {:productions {}, :final-token :end}}}))))