(ns compiler.core
  (:gen-class)
  (:require [compiler.tokens :refer [dfa-grammar lr-table]]
            [compiler.lexical :refer [get-lexical-tokens]]
            [compiler.syntactic :refer [run-lr-analysis]]))

(defn -main
  "I don't do a whole lot ... yet."
  [file-name]
  (let [input (slurp file-name)
        tokens (get-lexical-tokens dfa-grammar input)] 
    (run-lr-analysis lr-table tokens)))
