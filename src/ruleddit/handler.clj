(ns ruleddit.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [clojure.data.json :as json]
            [ruleddit.persist :as persist]))

(defroutes app-routes
  (GET "/" [] (resp/resource-response "index.html" {:root "public"}))
  (GET "/hello" [] "Hello World")
  (GET "/echo" [& args] (json/write-str args))
  (GET "/redirect" [& args] (resp/resource-response "index.html" {:root "public"}))
  (GET "/api/load/:uuid" [uuid] (json/write-str {:uuid uuid}))
  (POST "/api/save" [& subreddit-rules] nil)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
