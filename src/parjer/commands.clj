(ns parjer.commands
  (:require [parjer.parser :as parser :refer (add-event evt-handler)]
            [parjer.config :refer (fetch-conf)]
            [parjer.network :as net :refer (write-to-out join-channel write-to-irc)]
            [clojure.string :as s :refer (split join)]
            [clojail.core :refer [sandbox]]
            [clojail.testers :refer [secure-tester]]))

(def cmd-handler (atom {}))

(defn add-cmd [event f]
  (swap! cmd-handler assoc event f))

(def mark ((fetch-conf) :mark))

(def pat
  (re-pattern (str "[^" mark "][\\d\\w\\S]*")))

(defmacro event [e & args-body]
  `(add-event ~e (fn ~@args-body)))

(defmacro cmd [e & args-body]
  `(add-cmd ~e (fn ~@args-body)))

(event "NOTICE"
       [c x]
       (println nil))

(event "PART"
       [c x]
       (println "Part"))

(event "PING"
       [c x]
       (write-to-out c (str "PONG :" (x 4)))
       (println "Pong Sent"))

(event "MODE"
       [c x]
       (join-channel c))

(event "PRIVMSG"
       [c x]
       (let [name ((split (x 1) #"!") 0)
             cmd (re-find pat (x 4))
             channel (x 3)]
         (if cmd
           (if true ;;; Add ignored users here!
             (if (contains? @cmd-handler cmd)
               ((@cmd-handler cmd) c x channel)))
           (println cmd))))

;;; Lets sandbox this....better....
(def sb (sandbox secure-tester :timeout 5000))

(defn excp! [ev]
  (try (sb (read-string ev))
       (catch Exception e (str "Exception: " (.getMessage e)))))


;;; Common! Tell me how stupid i am!
(cmd "eval"
     [c x channel]
     (let [st (join " " (rest (split (x 4) #" ")))]
       (write-to-irc c channel (excp! st))))

(cmd "uptime"
     [c x channel]
     (write-to-irc c channel "NOTIME"))

(cmd "say"
     [c x channel]
     (let [st (join " " (rest (split (x 4) #" ")))]
       (write-to-irc c channel st)))


;;; This is random. I am 100% sure!
(cmd "dice"
     [c x channel]
     (write-to-irc c channel "4"))

(cmd "join"
     [c x channel]
     (let [st ((split (x 4) #" ") 1)]
       (join-channel c st)))

(cmd "part"
     [c x channel]
     (let [st ((split (x 4) #" ") 1)]
       (write-to-out (str "PART " st))))
