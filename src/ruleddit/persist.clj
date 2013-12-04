(ns ruleddit.persist
  (:use monger.operators)
  (:require [monger.core :as mg]
            [ruleddit.reddit :as reddit]
            [monger.collection :as mc]
            [clj-time.core :as time-core]
            [clj-time.coerce :as time-coerce])
  (:import [com.mongodb DB WriteConcern]))

(def ruleddit-db "ruleddit-db")
(def subreddit-coll "subreddit-top-today")
(def rules-coll "rules")
(def mongo-host "127.0.0.1")
(def mongo-port 27017)

(mg/connect! { :host mongo-host :port mongo-port })
(mg/set-db! (mg/get-db ruleddit-db))

(defn current-time-utc
  "returns unix time (seconds) of current time utc"
  []
  (/ (time-coerce/to-long (time-core/now)) 1000))

(defn time-utc-ago
  "returns unix time input units of time ago"
  [& {:keys [days hours minutes seconds]
      :or {days 0 hours 0 minutes 0 seconds 0}}]
  (- (current-time-utc)
     (* days 60 60 24)
     (* hours 60 60)
     (* minutes 60)
     seconds))

(defn random-uuid [] (str (java.util.UUID/randomUUID)))

(defn persist-subreddit
  "save all top posts for a subreddit"
  [& args]
  (let [raw-posts (apply reddit/get-subreddit-top args)
        add-id #(assoc % :_id (:id %))
        posts-with-id (map add-id raw-posts)]
    ; batch insert doesnt work because it doesn't overwrite
    ;; (mc/insert-batch subreddit-coll posts-with-id WriteConcern/NORMAL)
    (map #(mc/upsert subreddit-coll {:_id (:_id %)} %) posts-with-id)))

(defn cleanup-old-subreddit-coll
  "removes old posts from the subreddit collection a given time ago"
  [& time-args]
  (mc/remove subreddit-coll {:created_utc
                             {$lt (apply time-utc-ago time-args)}}))

(defn find-subreddit
  "find all stored posts from a subreddit over given time constraints"
  [subreddit & time-args]
  (mc/find-maps subreddit-coll {:subreddit subreddit
                                :created_utc {$gt (apply time-utc-ago time-args)}}))

(defn persist-rules
  "stores rules and returns an id to find the rules at a later time"
  [rules]
  {:pre [(map? rules)]
   :post [string?]}
  (let [uuid (random-uuid)]
    (mc/insert rules-coll (into {:_id uuid} rules))
    uuid))

(defn get-rules
  "deserialize rules from database"
  [uuid]
  (mc/find-map-by-id rules-coll uuid))

(defn get-all
  "get all maps from a collection; meant for debugging"
  [coll-name]
  (mc/find-maps coll-name))
