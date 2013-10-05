## elimai [![Build Status](https://travis-ci.org/dlokesh/elimai.png?branch=master)](https://travis-ci.org/dlokesh/elimai)

A minimalistic static site generator in Clojure

## Dependencies

Install the following dependencies, which are necessary to run elimai locally:

	$ brew install leiningen
	$ brew install wget
	$ brew install drip

## Usage

	$ lein new elimai sample-blog
	$ cd sample-blog
	$ chmod +x elimai
	$ ./elimai

This would setup a base template under sample-blog and start jetty on watch mode to auto-reload templates & posts.

## License

Copyright © 2013 Lokeshwaran

Distributed under the Eclipse Public License, the same as Clojure.
