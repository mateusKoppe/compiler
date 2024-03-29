(ns compiler.struct-grammar-parser
  (:require [clojure.xml :as xml]))

(defonce SYMBOL-TYPE-MAP
  {"0" :production
   "1" :terminal
   "3" :EOF
   "7" :error
   "2" :whitespace})

(def ACTION-TYPE-MAP
  {"1" :shift
   "2" :reduce
   "3" :goto
   "4" :accept})

(defn get-attribute [tag attribute]
  (get-in tag [:attrs attribute]))

(defn get-lr-column
  ([tag]
   (cond
     (some #(= % (get ACTION-TYPE-MAP (get-attribute tag :Action)))
           [:shift :reduce :accept]) :action
     (= (get ACTION-TYPE-MAP (get-attribute tag :Action)) :goto) :goto)))

(defn format-to-map
  ([table values]
   (format-to-map table values #(Integer. (get-attribute % :Index))))
  ([table values predicate]
   (into (sorted-map)
         (zipmap
          (map predicate table)
          values))))


(defn format-symbols [file-data]
  (let [format-symbol (fn [tag]
                        {:name (get-attribute tag :Name)
                         :type (get SYMBOL-TYPE-MAP
                                    (get-attribute tag :Type))})
        symbol-table (get-in file-data [:content 1 :content])]
    (format-to-map symbol-table (map format-symbol symbol-table))))


(defn get-symbol [file-data symbol-index]
  (let [symbols-table (get-in file-data [:content 1 :content])
        tag (first (filter #(= symbol-index (get-attribute % :Index)) symbols-table))
        symbol-data (:attrs tag)
        type (get SYMBOL-TYPE-MAP (:Type symbol-data))
        name (:Name symbol-data)]
    {:name (keyword name)
     :type type}))

(defn format-charset-table [file-data]
  (let [table (get-in file-data [:content 4 :content])
        tags->unicodes (fn [tags] (map #(->
                                         (get-attribute % :UnicodeIndex)
                                         Integer.
                                         char
                                         str) tags))
        contents (map :content table)
        values (map tags->unicodes contents)]
    (format-to-map table values)))

(defn format-dfa-state [file-data charset-table tag]
  (let [accept-symbol (get-attribute tag :AcceptSymbol)
        content (:content tag)
        charsets (map
                  (fn [tag]
                    {:state (Integer. (get-attribute tag :Target))
                     :charset (Integer. (get-attribute tag :CharSetIndex))}) content)
        values (reduce
                (fn [acc {:keys [state charset]}]
                  (let [chars (get charset-table charset)]
                    (merge acc (reduce #(merge %1 {%2 state}) {} chars))))
                {}
                charsets)]
    {:symbol (get-symbol file-data accept-symbol)
     :transitions values}))

(defn format-dfa [file-data]
  (let [table (get-in file-data [:content 5 :content])
        charset-table (format-charset-table file-data)]
    (format-to-map
     table
     (map #(format-dfa-state file-data charset-table %)
          table))))

(defn format-productions [file-data]
  (let [get-symbol (partial get-symbol file-data)
        format-production
        (fn [tag]
          {:rule (keyword (:name (get-symbol (get-attribute tag :NonTerminalIndex))))
           :productions (into
                         []
                         (map
                          #(:name (get-symbol
                                   (get-attribute % :SymbolIndex)))
                          (:content tag)))})
        production-table (get-in file-data [:content 3 :content])]
    (format-to-map
     production-table
     (map #(format-production %) production-table))))

(defn format-action-data [tag]
  (let [action-map {:shift :state
                    :goto :state
                    :reduce :production}
        value (Integer. (get-attribute tag :Value))
        action (get ACTION-TYPE-MAP (get-attribute tag :Action))]
    (-> {:action action}
        (assoc (get action-map action) value))))

(defn format-action-type [file-data type]
  (let [format-action-content
        (fn [{:keys [content]}]
          (let [filtered (filter
                          #(= type (get-lr-column %))
                          content)]
            (format-to-map
             filtered
             (map format-action-data filtered)
             #(keyword (:name (get-symbol file-data (get-attribute % :SymbolIndex)))))))
        production-table (get-in file-data [:content 6 :content])]
    (format-to-map production-table (map format-action-content production-table))))

(def format-action (partial #(format-action-type % :action)))
(def format-goto (partial #(format-action-type % :goto)))

(defn parse-grammar-file [file]
  (let [file-data (-> (slurp file)
                      .getBytes
                      java.io.ByteArrayInputStream. xml/parse)]
    {:production (format-productions file-data)
     :action (format-action file-data)
     :goto (format-goto file-data)
     :dfa (format-dfa file-data)}))