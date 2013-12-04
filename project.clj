(defproject ruleddit "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [enlive "1.1.4"]
                 [org.clojure/data.json "0.2.3"]
                 [com.novemberain/monger "1.7.0-beta1"]
                 [clj-time "0.6.0"]]
  :plugins [[lein-ring "0.8.8"]]
  :ring {:handler ruleddit.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
