(ns compiler.grammar)

(defn keywork->grammar [token]
  {:states (merge
            (apply merge
                   (map-indexed
                    (fn [i non-terminal] {i {:productions {non-terminal [(inc i)]}
                                             :is-final false}}) (str token)))
            {(count string) {:productions []
                             :is-final true}})
   :token (keyword string)})

(map #(vector %) (str :if))

(count :str)