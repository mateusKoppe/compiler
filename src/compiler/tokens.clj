(ns compiler.tokens
  (:require [clojure.string :as string]
            [compiler.grammar :refer [keywork->grammar merge-grammar nfa->dfa]]))

(def lowercase-letters ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "o" "p" "q" "r" "s" "t" "u" "v" "x" "w" "y" "z"])
(def uppercase-letters (vec (map string/upper-case lowercase-letters)))
(def all-letters (vec (concat lowercase-letters uppercase-letters)))

(def numbers ["0" "1" "2" "3" "4" "5" "6" "7" "8" "9"])

(def identifier-grammar
  (let [map-chars #(apply merge (map (fn [l] (sorted-map l #{%2})) %1))]
    {:start-state 0
     :states {0 {:productions (map-chars all-letters 1)}
              1 {:productions (map-chars (concat all-letters numbers) 1)
                 :final-token :identifier}}}))

(def tokens
  [identifier-grammar
   (keywork->grammar :if)
   (keywork->grammar :loop)
   (keywork->grammar :when)
   ])

(def nfa-grammar (reduce #(merge-grammar %1 %2) tokens))
(def dfa-grammar (nfa->dfa nfa-grammar))

