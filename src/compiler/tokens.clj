(ns compiler.tokens
  (:require [clojure.string :as string]
            [compiler.grammar :refer [keywork->grammar merge-grammar nfa->dfa]]))

(def lowercase-letters ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "o" "p" "q" "r" "s" "t" "u" "v" "x" "w" "y" "z"])
(def uppercase-letters (vec (map string/upper-case lowercase-letters)))
(def all-letters (vec (concat lowercase-letters uppercase-letters)))

(def numbers-chars ["0" "1" "2" "3" "4" "5" "6" "7" "8" "9"])

(def identifier-grammar
  (let [map-chars #(apply merge (map (fn [l] (sorted-map l #{%2})) %1))]
    {:start-state 0
     :states {0 {:productions (map-chars all-letters 1)}
              1 {:productions (map-chars (concat all-letters numbers-chars) 1)
                 :final-token :identifier}}}))

(def numbers
  (let [map-chars #(apply merge (map (fn [l] (sorted-map l #{%2})) %1))]
    {:start-state 0
     :states {0 {:productions (map-chars numbers-chars 1)}
              1 {:productions (map-chars numbers-chars 1)
                 :final-token :number}}}))

(def separators
  [(keywork->grammar :whitespace " ")
   (keywork->grammar :breakline "\\n")
   (keywork->grammar :tab "\\t")
   (keywork->grammar :semicolon ";")
   (keywork->grammar :comma ",")])

(def operators
  [(keywork->grammar :plus "+")
   (keywork->grammar :minus "-")
   (keywork->grammar :times "*")
   (keywork->grammar :divide "/")
   (keywork->grammar :assign "=")
   (keywork->grammar :equals "==")
   (keywork->grammar :different "!=")
   (keywork->grammar :bigger ">")
   (keywork->grammar :smaller "<")
   (keywork->grammar :bigger-equal ">=")
   (keywork->grammar :smaller-equal "<=")])

(def types 
  [(keywork->grammar :bool)
   (keywork->grammar :int)
   (keywork->grammar :float)
   (keywork->grammar :char)
   (keywork->grammar :string)])

(def reserved-words
  [(keywork->grammar :def)
   (keywork->grammar :if)
   (keywork->grammar :else)])

(def tokens
  (concat [identifier-grammar numbers]
          separators
          operators
          types
          reserved-words))

(def nfa-grammar (reduce #(merge-grammar %1 %2) tokens))
(def dfa-grammar (nfa->dfa nfa-grammar))

(clojure.pprint/pprint dfa-grammar)