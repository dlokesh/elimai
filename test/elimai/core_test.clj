(ns elimai.core-test
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [elimai.core :refer :all]
            [selmer.parser :as parser]
            [clojure.java.io :refer [resource file]]))

(defn- test-resource [file-name] (file (.getFile (resource (str "resources/" file-name)))))

(facts "about parse-meta"
	(fact "it should parse meta data from given string"
		(parse-meta "<!-- {:title \"Welcome\" :date \"2013-09-20\" :tags \"general\"} -->\n\nWelcome to your new blog\n")
			=> {:title "Welcome" :date "2013-09-20" :tags "general"}
		)
	(fact "it should return nil if meta data is not present"
		(parse-meta "Content without metadata") => nil))

(fact "it should parse data from given file"
	(let [content (slurp (test-resource "test.md"))]
		(parse-data (test-resource "test.md"))
			=> {:title "Welcome" :date "2013-09-20" :url "posts/test.html" 
				:file-name "test" :content content}
		(provided 
			(parse-meta content) => {:title "Welcome" :date "2013-09-20"})))

(fact "it should fetch all posts sorted by date"
	(all-posts) => [{:title "post2" :date "2013-09-29"} {:title "post1" :date "2013-09-20"}] 
	(provided 
		(all-post-files) => ["post1.md", "post2.md"]
		(parse-data "post1.md") => {:title "post1" :date "2013-09-20"}
		(parse-data "post2.md") => {:title "post2" :date "2013-09-29"}))

(fact "should render default template with given content"
	(parser/set-resource-path! (str current-dir "/test/resources"))
	(render "some content" (file current-dir "test/resources/out.html")) => nil	
	(provided 
		(template "default.html") => "default.html")
	(fact "should spit out rendered content to file"
		(let [out (test-resource "out.html")]
			(slurp out) => "<div>some content</div>"
			(.delete out))))

(fact "it should render post template with given post data"
	(let [post {:title "post1" :url "post1-url" :date "2013-09-20"}]
		(render-post post) => true
		(provided 
			(template "post.html") => "template-post"
			(parser/render-file "template-post" post) => "html content"
			(output-file "post1-url") => "output-post1-url"
			(render "html content" "output-post1-url") => true)))

(fact "it should render all posts"
	(render-posts) => nil
	(provided 
		(output-file "posts") => (file "resources/blah")
		(all-posts) => [1 2]
		(render-post 1) => 1
		(render-post 2) => 2))

(fact "it should render index page"
	(render-index) => true
	(provided
		(all-posts) => [1 2]
		(template "index.html") => "template-index"
		(parser/render-file "template-index" {:posts [1 2]}) => "html content"
		(output-file "index.html") => "output-index"
		(render "html content" "output-index") => true))

(fact "render-all should render index and posts"
	(render-all) => true
	(provided 
		(render-posts) => nil
		(render-index) => true))
