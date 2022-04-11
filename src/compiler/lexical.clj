(ns compiler.lexical)

(def INITIAL-STATE 0)
(def SEPARATORS #{"(" ")" " " "\n"})

(defn get-next-state [grammar state non-terminal]
  (get-in grammar [state :productions non-terminal]))

(defn get-final-token [grammar state]
  (get-in grammar [state :final-token]))

(defn get-lexical-tokens [grammar input]
  (first (reduce (fn [[sequence acc state] non-terminal]
            (let [separator? (contains? SEPARATORS non-terminal)
                  next-state (get-next-state grammar state non-terminal)
                  final-token (get-final-token grammar state)]
              (cond
                (and separator? final-token) [(conj sequence {:token final-token
                                                              :lexeme acc})
                                              ""
                                              INITIAL-STATE]
                :else [sequence (str acc non-terminal) next-state])))
          [[] "" INITIAL-STATE]
          ;;  Adding \n to add the last separator on file
          (clojure.string/split (str input "\n") #""))))
