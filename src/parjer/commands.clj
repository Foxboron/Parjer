(ns parjer.commands
  (:require [parjer.parser :as parser :only (add-event)]
            [parjer.networks :as net :only (writeToOut)]))

(parser/add-event "NOTICE"
           (fn [c x]
             (println "do you even?")
             (println "Notice: " x)))

(parser/add-event "PART"
           (fn [c x]
             (println "Part")))

(parser/add-event "PING"
           (fn [c x]
             (println x)
             (net/writeToOut c (str "PONG :" (x 4)))
             (println "Pong Sent")))

(parser/add-event "001"
                  (fn [c x]
                    nil))
