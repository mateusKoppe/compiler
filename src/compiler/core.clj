(ns compiler.core
  (:gen-class)
  (:require [compiler.tokens :refer [dfa-grammar lr-table]]
            [compiler.lexical :refer [get-lexical-tokens]]
            [compiler.syntactic :refer [run-lr-analysis]]
            [cheshire.core :as json]  :reload))

(defn -main
  "I don't do a whole lot ... yet."
  [file-name]
  (let [input (slurp file-name)
        tokens (get-lexical-tokens dfa-grammar input)]
     (print (json/generate-string (get (run-lr-analysis lr-table tokens) 1) {:pretty true}))))
