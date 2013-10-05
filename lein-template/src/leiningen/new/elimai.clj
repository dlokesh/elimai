(ns leiningen.new.elimai
  (:use [leiningen.new.templates :only [renderer name-to-path ->files year]]))

(def render (renderer "elimai"))
(def current-date (str (java.sql.Date. (.getTime (java.util.Date.)))))

(defn elimai
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)
              :current-date current-date}]
    (->files data
    		 ["elimai" (render "elimai")]
    		 ["src/_templates/default.html" (render "_templates/default.html")]
    		 ["src/_templates/index.html" (render "_templates/index.html")]
         ["src/_templates/post.html" (render "_templates/post.html")]
    		 ["src/_posts/welcome.md" (render "welcome.md" data)]
         [".gitignore" (render "gitignore")]
         ["css/main.css" (render "css/main.css")])))
