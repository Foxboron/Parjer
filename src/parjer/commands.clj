(ns parjer.commands
  (:require [parjer.parser :as parser :only (add-event)]
            [parjer.network :as net :only (writeToOut)]))

(add-event "NOTICE"
           (fn [x]
             (println "Notice: " x)))

(add-event "PART"
           (fn [x]
             (println "Part")))

(add-event "PING"
           (fn [x]
             (println "Pong sent")
             (writeToOut (str ((clojure.string/split x #" :") 1) ))))
