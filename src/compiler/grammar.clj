(ns compiler.grammar 
  (:require [clojure.set :refer [intersection rename-keys]]))

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

(defn find-available-index [list last-index]
  (if (some #(= % last-index) list)
    (recur list (inc last-index))
    last-index))

(defn remap-state [states replace-map]
  (let [remap-productions (fn [productions]
                            (zipmap
                             (keys productions)
                             (vec
                              (map (fn [nexts]
                                     (vec (map #(get replace-map %) nexts)))
                                   (vals productions)))))]
    (zipmap
     (keys states)
     (map (fn [rules]
            (merge rules {:productions (remap-productions (:productions rules))}))
          (vals states)))))

(defn updated-used-states [grammar used-states]
  (let [gramar-states (keys (:states grammar))
        to-replace-states (remove #(= (:start-state grammar) %)
                                  (vec (intersection (set used-states) (set gramar-states))))
        replace-map (loop [states gramar-states
                           used-states used-states
                           replace-map {}
                           last-index 0]
                      (let [state (first states)
                            states (rest states)]
                        (if state
                          (let [to-replace? (some #(= state %) to-replace-states)
                                last-index (if to-replace? (find-available-index used-states last-index) last-index)
                                used-states (if to-replace? (conj used-states last-index) used-states)
                                replace-map (merge {state (if to-replace? last-index state)} replace-map)]
                            (recur states used-states replace-map last-index))
                          replace-map)))
        remaped-states (rename-keys (:states grammar) replace-map)
        updated-states (remap-state remaped-states replace-map)]
    (merge grammar {:states updated-states})
    ))


(defn merge-grammar [grammarA grammarB]
  ; Replace repated states
  ; Merge
  (let [used-states (keys (:states grammarA))]
    used-states))