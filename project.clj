(defproject elimai "0.1.1"
  :description "A minimalistic static site generator in Clojure"
  :url "http://github.com/dlokesh/elimai"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
        				 [org.pegdown/pegdown "1.4.1"]
        				 [selmer "0.4.2"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [compojure "1.1.5"]
                 [clj-time "0.6.0"]
                 [watchtower "0.1.1"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}}
  :plugins [[lein-midje "3.0.0"]]                 
  :main elimai.core)
