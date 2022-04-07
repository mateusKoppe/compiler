(ns compiler.grammar
  (:require [clojure.set :refer [rename-keys]]))

(defn keywork->grammar [token]
  (let [string (name token)
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
                    last-state)}))

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
  (let [index (first (keep-indexed #(when (= item %2) %1) coll))]
    (if index [coll index] (get-or-create-state (conj coll item) item))))


(defn last-state? [coll index]
  (>= (inc index) (count coll)))

(defn dfa-add-state [dfa-gramar state-to-run state-config non-terminal new-next-state]
  (assoc
   dfa-gramar
   state-to-run
   (merge state-config {:productions (assoc (get-in dfa-gramar [state-to-run :productions]) non-terminal new-next-state)})))

(defn dfa-discover-state [dfa-gramar state-remap state-config state-to-run]
  (loop [productions (:productions state-config)
         dfa-gramar dfa-gramar
         state-remap state-remap]
    (let [[[non-terminal nfa-state] & productions] productions
          [state-remap new-next-state] (get-or-create-state state-remap nfa-state)
          dfa-gramar (dfa-add-state dfa-gramar state-to-run state-config non-terminal new-next-state)]

      (if (empty? productions)
        [dfa-gramar state-remap]
        (recur productions dfa-gramar state-remap)))))

(defn nfa->dfa
  ([grammar]
   (first (let [initial-state (:start-state grammar)]
     (nfa->dfa (:states grammar) {} [#{initial-state}] 0 #{initial-state}))))
  ([nfa-grammar dfa-gramar state-remap state-to-run]
   (nfa->dfa nfa-grammar dfa-gramar state-remap state-to-run (get state-remap state-to-run)))
  ([nfa-grammar dfa-gramar state-remap state-to-run states-to-discover]
  ;; Run all productions and recurse it
   (let
    [[dfa-gramar state-remap]
     (let [[state-to-discover & states-to-discover] states-to-discover
           [dfa-gramar state-remap] (dfa-discover-state
                                     dfa-gramar
                                     state-remap
                                     (get nfa-grammar state-to-discover)
                                     state-to-run)]
       (if (empty? states-to-discover)
         [dfa-gramar state-remap]
         (nfa->dfa nfa-grammar dfa-gramar state-remap state-to-run states-to-discover)))]
     (if (last-state? state-remap state-to-run)
       [dfa-gramar state-remap]
       (nfa->dfa nfa-grammar dfa-gramar state-remap (inc state-to-run))))))

(nfa->dfa grammar)

(def grammar {:start-state 0
              :states {0 {:productions {"w" #{1 5} "a" #{2 3}}}
                       1 {:productions {"i" #{2} "w" #{2 3}}}
                       2 {:productions {"t" #{3}}}
                       3 {:productions {"h" #{4}}}
                       4 {:productions {}
                        :final-token :with}
                       5 {:productions {"h" #{6}}}
                       6 {:productions {"e" #{7}}}
                       7 {:productions {"n" #{8}}}
                       8 {:productions {}, :final-token :when}}})


(def a
  {0 {:productions {"w" 1, "a" 2}}
   7 {:productions {"n" 9}}
   1 {:productions {"i" 3, "w" 2, "h" 4}}
   4 {:productions {"e" 7}}
   6 {:productions {"h" 5}}
   3 {:productions {"t" 6}}
   2 {:productions {"h" 5, "t" 6}}
   9 {:productions {nil 8}, :final-token :when}
   5 {:productions {nil 8}, :final-token :with}
   8 {:productions {nil 8}}})


