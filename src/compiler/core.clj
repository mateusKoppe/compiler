(ns compiler.core
  (:gen-class)
  (:require [compiler.struct-grammar-parser :refer [parse-grammar-file]]
            [compiler.lexical :refer [get-lexical-tokens]]
            [compiler.syntactic :refer [run-lr-analysis]]
            [cheshire.core :as json]  :reload))

(defonce GRAMMAR-CONFIG-FILE "grammar/pspslang.xml")

(defn -main
  "I don't do a whole lot ... yet."
  [file-name]
  (let [input (slurp file-name)
        grammar (parse-grammar-file GRAMMAR-CONFIG-FILE)
        tokens (get-lexical-tokens (:dfa grammar) input)]
    (print (json/generate-string (get (run-lr-analysis grammar tokens) 1) {:pretty true}))))