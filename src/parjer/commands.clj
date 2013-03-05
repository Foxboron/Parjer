(ns parjer.commands
  (:require [parjer.parser :as parser :refer (add-event evt-handler)]
            [parjer.config :refer (fetch-conf)]
            [parjer.network :as net :refer (writeToOut joinChannel writeToIRC)]
            [clojure.string :as s :refer (split join)]
            [clojail.core :refer [sandbox]]
            [clojail.testers :refer [secure-tester]]))

(def cmd-handler (atom {}))

(defn add-cmd [event f]
  (swap! cmd-handler assoc event f))

(def mark ((fetch-conf) :mark))

(def pat
  (re-pattern (str "[^" mark "][\\d\\w\\S]*")))

(add-event "NOTICE"
           (fn [c x]
             (println "Notice: " x)))

(add-event "PART"
           (fn [c x]
             (println "Part")))

(add-event "PING"
           (fn [c x]
             (writeToOut c (str "PONG :" (x 4)))
             (println "Pong Sent")))

(add-event "MODE"
           (fn [c x]
             (joinChannel c)))

(add-event "PRIVMSG"
           (fn [c x]
             (let [name ((split (x 1) #"!") 0)
                   cmd (re-find pat (x 4))
                   channel (x 3)]
               (if cmd
                 (if true ;;; Add ignored users here!
                   (if (contains? @cmd-handler cmd)
                     ((@cmd-handler cmd) c x channel)))
                 (println x)))))

;;; Lets sandbox this....better....
(def sb (sandbox secure-tester :timeout 5000))

(defn excp! [ev]
  (try (sb (read-string ev))
       (catch Exception e (str "Exception: " (.getMessage e)))))


;;; Common! Tell me how stupid i am!
(add-cmd "eval"
         (fn [c x channel]
           (let [st (join " " (rest (split (x 4) #" ")))]
             (writeToIRC c channel (excp! st)))))

(add-cmd "uptime"
         (fn [c x channel]
           (writeToIRC c channel "NOTIME")))

(add-cmd "say"
         (fn [c x channel]
           (let [st (join " " (rest (split (x 4) #" ")))]
             (writeToIRC c channel st))))


;;; This is random. I am 100% sure!
(add-cmd "dice"
         (fn [c x channel]
           (writeToIRC c channel "4")))

(add-cmd "join"
         (fn [c x channel]
           (let [st ((split (x 4) #" ") 1)]
             (joinChannel c st))))

(add-cmd "part"
         (fn [c x channel]
           (let [st ((split (x 4) #" ") 1)]
             (writeToOut (str "PART " st)))))
