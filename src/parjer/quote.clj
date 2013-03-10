(ns parjer.quote
  (:require [net.cgrand.enlive-html :refer :all]))

(def website (html-resource (java.net.URL. "http://www.cs.yale.edu/quotes.html")))


;;; Thanks TimMc ^^
(def quotes (->> (select website [:p]) (map (comp #(.replaceAll % "\n" " ") clojure.string/trim first unwrap)) (filter not-empty)))

(defn rand-quote []
  (let [n (rand-int (count quotes))]
    (clojure.string/replace (.get quotes n) #"\s" " ")))
