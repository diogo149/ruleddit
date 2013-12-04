(ns ruleddit.reddit
  (:require [net.cgrand.enlive-html :as html]
            [clojure.data.json :as json]))

(def reddit-base "http://reddit.com")
(def top-post-times #{:hour :day :week :month :all})

(defn strip
  "removes leading and trailing instances of an element from a sequence"
  [elem s]
  (let [drop-func (partial drop-while #(= % elem))]
    (->> s
         drop-func
         reverse
         drop-func
         reverse)))

(defn join-url
  "joins together several url fragments into a string; assumes that all parts aren't surrounded by forward slashes"
  [& urls]
  (->> urls
       (map #(strip \/ %))
       (map #(apply str %))
       (interpose \/)
       (apply str)))

(defn fetch-html
  "downloads a url; copied from https://github.com/swannodette/enlive-tutorial/blob/master/src/tutorial/scrape2.clj"
  [url]
  (html/html-resource (java.net.URL. url)))

(defn read-json [s]
  (json/read-str s :key-fn keyword))

(defn json-extract
  "extracts the json->data->children fields from a json object [this pattern is repeated a lot]"
  [m]
  (->> m
      :data
      :children
      (map :data)))

(defn fetch-json
  "downloads json from a url and reads it"
  [^String url]
  {:pre [(or (.endsWith url ".json")
             (re-find #"\.json\?.+" url))]
   :post [#(map? %)]}
  (->> url
       slurp
       read-json))

(defn- map-to-url-flags
  "converts a map of flags to a url e.g. {:a :b :c :d} -> a=b&c=d"
  [m]
  (let [to-str #(if (keyword? %) (name %) (str %))
        entry-to-flag (fn [[k v]] (str (to-str k) "=" (to-str v)))]
    (->> m
         (map entry-to-flag)
         (interpose "&")
         (apply str))))

(defn add-flags
  "adds flags to a url; e.g. a.com -> a.com?a=1&b=2"
  [url flag-map]
  (str url "?" (map-to-url-flags flag-map)))

(defn get-subreddit-top
   "get sequence of posts for a specific subreddit"
   [subreddit & {:keys [time]
                 :or {time :day}}]
   {:pre [(top-post-times time)]}
   (-> subreddit
       (#(join-url reddit-base "r" % "top"))
       (str ".json")
       (add-flags {:t time})
       fetch-json
       json-extract))

(defn get-comment-url
  "returns the comment url given the output map from get-subreddit"
  [m]
  (join-url reddit-base (:permalink m)))

(defn get-comments
  "gets comments for a specific post"
  [post-url & flags]
  (-> post-url
      (str ".json")
                                        ; TODO do something with flags
      fetch-json
      (#(map json-extract %))
      second ; first is original post, second is comments
      ))
