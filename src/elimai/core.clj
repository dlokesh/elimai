(ns elimai.core
  (:import (org.pegdown PegDownProcessor Extensions))
  (:use [ring.adapter.jetty]
  		[clj-time.format :only [parse unparse formatters formatter]]
  		[watchtower.core])
  (:require [clojure.java.io :refer [file]]
  			[clojure.java.browse :refer [browse-url]]
  			[clojure.string :refer [split]]
  			[selmer.parser :as parser]
  			[selmer.filters :as filters]
  			[compojure.core :refer [routes]]
    		[compojure.route :refer [files]])
  (:gen-class))

(def current-dir (System/getProperty "user.dir"))
(def md (PegDownProcessor. Extensions/FENCED_CODE_BLOCKS))
(def yyyy-MM-dd (formatters :date))
(def dd-MMM-yyyy (formatter "dd MMM, yyyy"))
(def ^:dynamic conf {:templates-folder "public/_templates", :posts-folder "public/_posts", :output-folder "public"})

(defn template [name]
	(str (:templates-folder conf) "/" name))

(defn output-file [name]
	(file current-dir (:output-folder conf) name))

(defn config-selmer []
  (parser/set-resource-path! current-dir)
  (filters/add-filter! :markdown (fn [s] [:safe (.markdownToHtml md s)]))
  (filters/add-filter! :format-dd-MMM-yyyy (fn [s] (unparse dd-MMM-yyyy (parse yyyy-MM-dd s)))))

(defn parse-meta [post]
	(let [raw-meta (first (re-find #"(?<=\<!--)((?s).*?)(?=\-->)" post))]
		(when-not (nil? raw-meta) (read-string raw-meta))))

(defn parse-data [file-]
	(let [file-name (first (split (.getName file-) #"\."))
		  post (slurp file-)
		  post-meta (parse-meta post)]
		(merge post-meta {:url (str "posts/" file-name ".html") :file-name file-name :content post})))

(defn all-post-files []
	(.listFiles (file current-dir (:posts-folder conf))))

(defn all-posts []
	(let [files (all-post-files)]
		(->> files
			(map parse-data)
			(sort-by :date)
			reverse)))

(defn render [content out-file]
	(spit out-file (parser/render-file (template "default.html") {:content content})))

(defn render-post [post]
	(let [post-html (parser/render-file (template "post.html") post)]
		(render post-html (output-file (:url post)))))

(defn render-posts []
	(.mkdir (output-file "posts"))
	(doseq [post (all-posts)] (render-post post)))

(defn render-index []
	(let [index-html (parser/render-file (template "index.html") {:posts (all-posts)})]
		(render index-html (output-file "index.html"))))

(defn render-all [] 
	(render-posts)
	(render-index))

(defn re-render-all [file]
	(println "Rendering" file "...")
	(render-all)
	(println "Done!"))

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

  (future (run-jetty (routes (files "/" {:root "public"})) {:port 8080}))
  (browse-url "http://localhost:8080"))
