(ns compiler.lexical)

(def INITIAL-STATE 0)

(defn get-next-state [grammar state non-terminal]
  (get-in grammar [state :productions non-terminal]))

(defn get-final-token [grammar state]
  (get-in grammar [state :final-token]))

(defn get-lexical-tokens [grammar input]
  (first
   (reduce (fn [[sequence acc state] non-terminal]
             (let [next-state (get-next-state grammar state non-terminal)
                   final-token (get-final-token grammar state)
                   error? (nil? next-state)]
               (if error?
                 [(conj sequence {:token final-token
                                  :lexeme acc})
                  non-terminal (get-next-state grammar INITIAL-STATE non-terminal)]
                 [sequence (str acc non-terminal) next-state])))
           [[] "" INITIAL-STATE]
          ;;  Adding \n to add the last separator on file
           (clojure.string/split (str input "\n") #""))))
