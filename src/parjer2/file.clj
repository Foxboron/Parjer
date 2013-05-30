(ns parjer2.file
  (:require [clojure.pprint :refer :all]))


(def fetch-conf
  (read-string (slurp "setup")))

(defn protocols->map [prot]
  (into []
        (for [i prot]
          {:server (:server i) :port (:port i) :chans (:chans i)})))


(defn save-config [serv-vec]
  (let [serv-map (protocols->map serv-vec)
        output (fetch-conf)
        new-map (assoc-in output [:servers] serv-map)]
    (binding
        [*out* (java.io.FileWriter. "setup2")
         *print-miser-width* 40
         *print-right-margin* 30]
      (pprint new-map))))


(defn read-config []
  (let [servs (:servers fetch-conf)]
    servs))
