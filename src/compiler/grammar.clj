(ns compiler.grammar)

(defn keywork->grammar [token]
  (let [string (name token)
        last-state {(count string)
                    {:productions {}
                     :final-token token}}
        map->state (fn [i non-terminal]
                     {i {:productions {(str non-terminal) [(inc i)]}}})]
    {:start-state 0
     :states (merge (apply
            merge
            (map-indexed map->state
                         string))
           last-state)}))

(defn merge-grammar [grammarA grammarB]
  )