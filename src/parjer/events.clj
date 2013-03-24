(ns parjer.events
  (:require [parjer.commands :refer (ignore-list cmd-handler)]
            [parjer.network :refer (write-to-out join-channels)]
            [parjer.config :refer (fetch-conf)]
            [clojure.string :refer (split join)]
            [parjer.parser :refer (add-event evt-handler)]))

(def mark ((fetch-conf) :mark))

(def pat
  (re-pattern (str "[^" mark "][\\d\\w\\S]*")))

(defmacro event [e & args-body]
  `(add-event ~e (fn ~@args-body)))

(event "PART"
       [c x]
       (println "Part"))

(event "PING"
       [c x]
       (write-to-out c (str "PONG :" (x 4)))
       (println "Pong Sent"))

(event "266"
       [c x]
       (join-channels c (@c :chans)))

(event "PRIVMSG"
       [c x]
       (let [name ((split (x 1) #"!") 0)
             cmd (if (= mark (str (first (x 4)))) (re-find pat (x 4)) "")
             channel (x 3)
             args (rest (split (x 4) #" "))
             arg (first (rest (split (x 4) #" ")))
             info-map (ref {:chan channel :nick name :cmd cmd :raw x :out c :arg arg :args args})]
         (if
             (false? (contains? @ignore-list name)) ;;; Check for ignored users
           (if (contains? @cmd-handler cmd)         ;;; Is the cmd in the handler?
             (future ((@cmd-handler cmd) info-map)))
           (println cmd))))
