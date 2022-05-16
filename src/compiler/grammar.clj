(ns compiler.grammar
  (:require [clojure.set :refer [rename-keys]]))

(defn keywork->grammar
  ([keyword] (keywork->grammar keyword keyword))
  ([token keyword]
   (let [string (name keyword)
         last-state {(count string)
                     {:productions {}
                      :final-token token}}
         map->state (fn [i non-terminal]
                      {i {:productions {(str non-terminal) #{(inc i)}}}})]
     {:start-state 0
      :states (merge (apply
                      merge
                      (map-indexed map->state
                                   string))
                     last-state)})))

(defn find-available-index [list last-index]
  (if (some #(= % last-index) list)
    (recur list (inc last-index))
    last-index))

(defn remap-state [states replace-map]
  (let [remap-productions (fn [productions]
                            (zipmap
                             (keys productions)
                             (map (fn [nexts]
                                    (set (map #(get replace-map %) nexts)))
                                  (vals productions))))]
    (zipmap
     (keys states)
     (map (fn [rules]
            (merge rules {:productions (remap-productions (:productions rules))}))
          (vals states)))))

(defn updated-used-states [grammar used-states]
  (let [gramar-states (keys (:states grammar))
        replace-map (loop [states gramar-states
                           used-states used-states
                           replace-map {}
                           last-index 0]
                      (let [[state & states] states]
                        (if state
                          (let [to-replace? (and
                                             (some #(= state %) used-states)
                                             (not (= state (:start-state grammar))))
                                last-index (if to-replace? (find-available-index used-states last-index) last-index)
                                used-states (if to-replace? (conj used-states last-index) (conj used-states state))
                                replace-map (merge {state (if to-replace? last-index state)} replace-map)]
                            (recur states used-states replace-map last-index))
                          replace-map)))
        remaped-states (rename-keys (:states grammar) replace-map)
        updated-states (remap-state remaped-states replace-map)]
    (merge grammar {:states updated-states})))

(defn merge-start-grammar [grammarA grammarB]
  (merge-with (fn [startA startB]
                (merge-with (fn [nextsA nextsB] (set (concat nextsA nextsB))) startA startB))
              (get (:states grammarA) (:start-state grammarA))
              (get (:states grammarB) (:start-state grammarB))))

(defn merge-grammar [grammarA grammarB]
  (let [used-states (set (keys (:states grammarA)))]
    {:start-state 0
     :states (merge
              {0 (merge-start-grammar grammarA (updated-used-states grammarB used-states))}
              (dissoc (:states grammarA) (:start-state grammarA))
              (dissoc (:states (updated-used-states grammarB used-states)) (:start-state grammarB)))}))

(defn get-or-create-state [coll item]
  (if (empty? item)
    [coll]
    (let [index (first (keep-indexed #(when (= item %2) %1) coll))]
      (if index [coll index] (get-or-create-state (conj coll item) item)))))

(defn dfa-merge-states [nfa-grammar states]
  (reduce
   (fn [acc state]
     (merge acc
            state
            {:productions
             (apply merge-with (conj (map :productions [acc state]) into))}))
   (map #(get nfa-grammar %) states)))

(defn nfa-determine-dfa
  ([grammar] (nfa-determine-dfa (:states grammar) {} #{(:start-state grammar)}))
  ([nfa-grammar dfa-grammar state-to-discover]
   (if (contains? dfa-grammar state-to-discover)
     dfa-grammar
     (let [new-state (dfa-merge-states nfa-grammar state-to-discover)
           dfa-grammar (into dfa-grammar {state-to-discover new-state})]
       (reduce (fn [dfa-grammar state]
                 (nfa-determine-dfa nfa-grammar dfa-grammar state))
               dfa-grammar
               (-> new-state :productions vals))))))

(defn dfa-fix-sets-states
  ([dfa-grammar]
   (let [[dfa-gramar used-states] (dfa-fix-sets-states dfa-grammar [#{0}] #{0})
         remap-state (reduce-kv #(into %1 {%3 %2}) {} used-states)]
     (into (sorted-map) (rename-keys dfa-gramar remap-state))))
  ([dfa-grammar used-states state-discover]
   (reduce (fn [[dfa-grammar used-states] [non-terminal state]]
             (let [state-processed? (some #(= state %) used-states)
                   [used-states new-state] (get-or-create-state used-states state)
                   dfa-grammar (assoc-in dfa-grammar [state-discover :productions non-terminal] new-state)]
               (if-not state-processed?
                 (dfa-fix-sets-states dfa-grammar used-states state)
                 [dfa-grammar used-states])))
           [dfa-grammar used-states]
           (-> dfa-grammar (get state-discover) :productions))))

(defn nfa->dfa [grammar]
  (-> grammar nfa-determine-dfa dfa-fix-sets-states))