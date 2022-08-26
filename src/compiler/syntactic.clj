(ns compiler.syntactic)

(def INITIAL-STACK 
  [{:type :terminal, :value {:type :start :value nil}, :state 0}])

(defn action-shift [_ {:keys [stack input]} action]
  (let [[look-ahead & next-input] input]
    {:stack (conj stack {:type :terminal
                         :value look-ahead
                         :state (:state action)})
     :input next-input}))

(defn action-reduce [lr-table {:keys [stack] :as lr-analysis} action]
  (let [{:keys [productions rule]} (get-in lr-table [:production (:production action)])
        stack-limit (- (count stack) (count productions))
        reduced-stack (subvec stack 0 stack-limit)
        reduced-tokens (subvec stack stack-limit) 
        state (:state (peek reduced-stack))
        next-state (get-in lr-table [:goto state rule :state]) 
        add-action {:type :production
                    :value {:type :production
                            :value rule
                            :child reduced-tokens}
                    :state next-state}
        new-stack (conj reduced-stack add-action)]

    {:stack new-stack
     :input (:input lr-analysis)}))

(defn action-accept [_ {:keys [stack input]} _]
  {:stack stack
   :input  []
   :status :accept})

(defn next-state [lr-table {:keys [stack input] :as lr-analysis}]
  (let [action-map {:shift action-shift
                    :reduce action-reduce
                    :accept action-accept
                    :error #(println "error" %)}
        look-ahead (first input)
        {:keys [state]} (peek stack)
        action (get-in lr-table [:action state (:token look-ahead)]) 
        handler (get action-map (:action action))]
    (when (nil? handler)
      (throw (Exception. (str "Syntatical error."))))
    (handler lr-table lr-analysis action)))

(defn run-lr-analysis [lr-table input]
  (loop [input input
         stack INITIAL-STACK] 
    (if (not-empty input)
      (let [{:keys [input stack]} (next-state lr-table {:input input :stack stack})]
        (recur input stack))
      stack)))
