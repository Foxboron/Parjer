(ns parjer.quote
  (:require [net.cgrand.enlive-html :refer :all]))

(def website (html-resource (java.net.URL. "http://www.cs.yale.edu/quotes.html")))

(def quotes (filter #(not= "\n" %) (flatten (map :content (flatten (select website [:p]))))))

(defn rand-quote []
  (let [n (rand-int (count quotes))]
    (clojure.string/replace (.get quotes n) #"\s" " ")))
