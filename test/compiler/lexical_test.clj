(ns compiler.lexical-test
  (:require [clojure.test :refer :all]
            [compiler.lexical :refer :all]))

(def simple-grammar {0 {:productions {"i" 1, "l" 7}},
              1 {:productions {"f" 2}},
              2 {:productions {"-" 3}, :final-token :if},
              3 {:productions {"n" 4}},
              4 {:productions {"o" 5}},
              5 {:productions {"t" 6}},
              6 {:productions {}, :final-token :if-not},
              7 {:productions {"o" 8}},
              8 {:productions {"o" 9}},
              9 {:productions {"p" 10}},
              10 {:productions {}, :final-token :loop}})

(deftest get-next-state-test
  (testing "get-next-state with l of loop"
    (is (= (get-next-state simple-grammar 0 "l") 7)))
  
  (testing "get-next-state with f of if"
    (is (= (get-next-state simple-grammar 0 "i") 1))))

(deftest get-lexical-tokens-test
  (testing "get-lexical-tokens with loop if if-not grammar"
    (is (= (get-lexical-tokens simple-grammar "loop if if-not")
           [{:token :loop :lexeme "loop"}
            {:token :if :lexeme "if"}
            {:token :if-not :lexeme "if-not"}]))))