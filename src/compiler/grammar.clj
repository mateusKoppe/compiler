(ns compiler.grammar)

(defn keywork->grammar [token]
  (let [string (str token)
        last-state {(count string)
                    {:productions []
                     :is-final true}}
        map->state (fn [i non-terminal]
                     {i {:productions {non-terminal [(inc i)]}
                         :is-final false}})]
    {:states (merge (apply
                     merge
                     (map-indexed map->state
                                  string))
                    last-state)
     :token (keyword string)}))