(ns ruleddit.rules)

(def comparison-map
  {"<" <
   "=" =
   ">" >
   "<=" <=
   ">=" >=
   "substring" #(not (nil? (re-find (re-pattern %2) %1)))})

(defn subreddit-rules
  "recursively filters a collection of maps given a hierarchy of rules"
  [{:keys [comp key val t f] :as rules} maps]
  {:pre [(or (string? rules) (and comp key val t f))]
   :post [map?]}
  (println comp key val t f)
  (if (string? rules) {(keyword rules) maps}
      (let [comparison (comparison-map comp)
            comparison-func #(comparison ((keyword key) %) val)
            {t-vals true f-vals false} (group-by comparison-func maps)]
        (println (count f-vals))
        (merge-with concat
                    (eval-rules t t-vals)
                    (eval-rules f f-vals)))))

(defn validate-rules
  "TODO recursively validates a set of rules"
  [rules]
  true)

#_(def sample-rules
  {:comp "="
   :val 2082
   :key "ups"
   :t "link"
   :f {:comp "substring"
       :val "happiness"
       :key "title"
       :t "comments"
       :f "ignore"}
   })
