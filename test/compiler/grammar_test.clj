(ns compiler.grammar-test
  (:require [clojure.test :refer :all]
            [compiler.grammar :refer :all]))


(def if-grammar {:start-state 0
                 :states {0 {:productions {"i" #{1}}}
                          1 {:productions {"f" #{2}}}
                          2 {:productions {}, :final-token :if}}})

(def end-grammar {:start-state 0
                 :states {0 {:productions {"e" #{1}}}
                          1 {:productions {"n" #{2}}}
                          2 {:productions {"d" #{3}}}
                          3 {:productions {}, :final-token :end}}})

(def with-grammar {:start-state 0
                  :states {0 {:productions {"w" #{1}}}
                           1 {:productions {"i" #{2}}}
                           2 {:productions {"t" #{3}}}
                           3 {:productions {"h" #{4}}}
                           4 {:productions {}, :final-token :with}}})

(def when-grammar {:start-state 0
                   :states {0 {:productions {"w" #{1}}}
                            1 {:productions {"h" #{2}}}
                            2 {:productions {"e" #{3}}}
                            3 {:productions {"n" #{4}}}
                            4 {:productions {}, :final-token :when}}})

(deftest keywork->grammar-test
  (testing "Keywork->grammar with if keywork"
    (is (= (keywork->grammar :if) if-grammar))))

(deftest updated-used-states-test
  (testing "updated-used-states-test"
    (is (= (updated-used-states end-grammar #{0 1 2})
           {:start-state 0
            :states {0 {:productions {"e" #{3}}}
                     3 {:productions {"n" #{4}}}
                     4 {:productions {"d" #{5}}}
                     5 {:productions {}, :final-token :end}}}))))

(deftest merge-grammar-test
  (testing "merge-grammar-test 'if' grammar and 'end' grammar"
    (is (= (merge-grammar if-grammar end-grammar)
           {:start-state 0
            :states {0 {:productions {"i" #{1} "e" #{3}}}
                     1 {:productions {"f" #{2}}}
                     2 {:productions {}, :final-token :if}
                     3 {:productions {"n" #{4}}}
                     4 {:productions {"d" #{5}}}
                     5 {:productions {}, :final-token :end}}})))
  
  (testing "merge-grammar-test 'when' grammar and 'with' grammar"
    (is (= (merge-grammar with-grammar when-grammar)
           {:start-state 0
            :states {0 {:productions {"w" #{1 5}}}
                     1 {:productions {"i" #{2}}}
                     2 {:productions {"t" #{3}}}
                     3 {:productions {"h" #{4}}}
                     4 {:productions {}, :final-token :with}
                     5 {:productions {"h" #{6}}}
                     6 {:productions {"e" #{7}}}
                     7 {:productions {"n" #{8}}}
                     8 {:productions {}, :final-token :when}}}))))

(deftest nfa->dfa-test
  (testing "nfa->dfa"
    (is (= (nfa->dfa {:start-state 0
                      :states {0 {:productions {"w" #{1 5}}}
                               1 {:productions {"i" #{2}}}
                               2 {:productions {"t" #{3}}}
                               3 {:productions {"h" #{4}}}
                               4 {:productions {}, :final-token :with}
                               5 {:productions {"h" #{6}}}
                               6 {:productions {"e" #{7}}}
                               7 {:productions {"n" #{8}}}
                               8 {:productions {}, :final-token :when}}})
           {0 {:productions {"w" 1}}
            1 {:productions {"i" 2 "h" 3}}
            2 {:productions {"t" 4}}
            3 {:productions {"e" 5}}
            4 {:productions {"h" 6}}
            5 {:productions {"n" 7}}
            6 {:productions {}, :final-token :with}
            7 {:productions {}, :final-token :when}}))))
