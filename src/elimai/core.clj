(ns elimai.core
  (:import [org.pegdown PegDownProcessor Extensions]
  		   [java.io File]
         [java.net URLClassLoader])
  (:use [ring.adapter.jetty]
  		[clj-time.format :only [parse unparse formatters formatter]]
  		[watchtower.core])
  (:require [clojure.java.io :refer [resource file]]
  			[clojure.java.browse :refer [browse-url]]
  			[clojure.string :refer [split join replace-first]]
  			[selmer.parser :as parser]
  			[selmer.filters :as filters]
  			[compojure.core :refer [routes]]
    		[compojure.route :refer [files]])
  (:gen-class))

(def current-dir (System/getProperty "user.dir"))
(def md (PegDownProcessor. Extensions/FENCED_CODE_BLOCKS))
(def yyyy-MM-dd (formatters :date))
(def dd-MMM-yyyy (formatter "dd MMM, yyyy"))
(def conf (read-string (slurp (resource "conf.clj"))))

(defn path [values]
	(join File/separator values))

(defn template [name]
	(path [(:templates-folder conf) name]))

(defn output-file [name]
	(file current-dir name))

(defn config-selmer []
  (parser/set-resource-path! current-dir)
  (filters/add-filter! :markdown (fn [s] [:safe (.markdownToHtml md s)]))
  (filters/add-filter! :format-dd-MMM-yyyy (fn [s] (unparse dd-MMM-yyyy (parse yyyy-MM-dd s)))))

(defn parse-meta [post]
	(let [raw-meta (first (re-find #"(?<=\<!--)((?s).*?)(?=\-->)" post))]
		(when raw-meta (read-string raw-meta))))

(defn construct-url [file-]
  (let [[folder file-with-ext] (take-last 2 (split (.getPath file-) #"\/"))]
    (path [(replace-first folder #"_" "") 
           (replace-first file-with-ext #"md" "html")])))

(defn parse-data [file-]
	(let [url (construct-url file-)
		    content (slurp file-)]
		(merge (parse-meta content) {:url url :content content})))

(defn list-files [folder]
  (.listFiles (file current-dir folder)))

(defn parse-md-files [folder]
  (map parse-data (list-files folder)))

(defn recent-posts [size]
	(take size (->> (parse-md-files (:posts-folder conf)) 
                  (sort-by :date) 
                  reverse)))

(defn render [content out-file]
	(spit out-file (parser/render-file (template "default.html") {:content content})))

(defn render-html [file-name data]
  (let [html (parser/render-file (template file-name) data)]
    (render html (output-file (:url data)))))

(defn render-posts []
	(.mkdir (output-file "posts"))
	(doseq [post (parse-md-files (:posts-folder conf))] 
    (render-html "post.html" post)))

(defn render-pages []
  (.mkdir (output-file "pages"))
  (doseq [page (parse-md-files (:pages-folder conf))] 
    (render-html "page.html" page)))

(defn render-index []
	(let [index-html (parser/render-file (template "index.html") {:posts (recent-posts 10)})]
		(render index-html (output-file "index.html"))))

(defn render-all [] 
	(render-posts)
	(render-index))

(defn re-render-all [file]
	(println "Rendering" file "...")
	(render-all))

(defn add-watcher []
  (watcher (vals (select-keys conf [:templates-folder :posts-folder]))
  	(rate 50)
  	(on-change re-render-all)))

(defn -main [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))

  (config-selmer)
  (render-all)
  (add-watcher)

  (future (run-jetty (routes (files "/" {:root "."})) {:port 8080}))
  (browse-url "http://localhost:8080"))
