(ns compiler.lexical)

(def INITIAL-STATE 0)
(def IGNORE-TOKENS [:whitespace :breakline :tab])

(defn get-next-state [grammar state non-terminal]
  (get-in grammar [state :productions non-terminal]))

(defn get-final-token [grammar state]
  (get-in grammar [state :final-token]))

;; TODO: Handle error, do not allow token to be nil
(defn get-lexical-tokens [grammar input] 
   (-> (reduce (fn [[sequence acc state] non-terminal]
                 (let [next-state (get-next-state grammar state non-terminal)
                       final-token (get-final-token grammar state)
                       error? (nil? next-state)]
                   (if error?
                     [(if (some #(= final-token %) IGNORE-TOKENS)
                       sequence
                       (conj sequence {:token final-token
                                      :lexeme acc}))
                      non-terminal (get-next-state grammar INITIAL-STATE non-terminal)]
                     [sequence (str acc non-terminal) next-state])))
               [[] "" INITIAL-STATE]
          ;;  Adding \n to add the last separator on file
               (clojure.string/split (str input "\n") #""))
       first 
       (conj {:token :EOF :lexeme ""})))
