(ns leiningen.new.elimai
  (:use [leiningen.new.templates :only [renderer name-to-path ->files]]))

(def render (renderer "elimai"))

(defn elimai
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (->files data
    		 ["elimai" (render "elimai")]
    		 ["src/_templates/default.html" (render "_templates/default.html")]
    		 ["src/_templates/index.html" (render "_templates/index.html")]
         ["src/_templates/post.html" (render "_templates/post.html")]
    		 ["src/_posts/welcome.md" (render "welcome.md")]
         ["css/main.css" (render "css/main.css")])))
