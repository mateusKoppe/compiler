(ns compiler.tokens
  (:require [clojure.string :as string]
            [compiler.grammar :refer [keywork->grammar merge-grammar nfa->dfa]]
            [compiler.struct-grammar-parser :refer [parse-grammar-file]]))

(defonce GRAMMAR-CONFIG-FILE "grammar/pspslang.xml")

(def lowercase-letters ["a" "b" "c"])
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
   (keywork->grammar :breakline "\n")
   (keywork->grammar :tab "\t")
   (keywork->grammar :semicolon ";")
   (keywork->grammar :comma ",")
   (keywork->grammar :open_brackets "{")
   (keywork->grammar :close_brackets "}")])

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
   (keywork->grammar :else)
   (keywork->grammar :then)
   (keywork->grammar :let)])

(def tokens
  (concat
   [identifier-grammar numbers]
   reserved-words 
   separators
   operators
   types
   reserved-words))

(def nfa-grammar (reduce #(merge-grammar %1 %2) tokens))

(def dfa-grammar (nfa->dfa nfa-grammar))
(def lr-table (parse-grammar-file GRAMMAR-CONFIG-FILE))