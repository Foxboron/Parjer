(ns parjer.commands
  (:require [parjer.parser :as parser :refer (add-event evt-handler)]
            [parjer.config :refer (fetch-conf)]
            [parjer.network :as net :refer (join! write-to-out join-channels write-to-irc connect*)]
            [parjer.quote :refer :all]
            [clojure.string :as s :refer (split join)]
            [clojail.core :refer [sandbox]]
            [clojail.testers :refer [secure-tester]]))

(def cmd-handler (atom {}))

(def ignore-list (atom #{}))

(defn add-cmd [event f]
  (swap! cmd-handler assoc event f))

(defn add-to-ignore [name]
  (swap! ignore-list conj name))

(def owner ((fetch-conf) :owner))

(def mark ((fetch-conf) :mark))

(def pat
  (re-pattern (str "[^" mark "][\\d\\w\\S]*")))

(defmacro event [e & args-body]
  `(add-event ~e (fn ~@args-body)))

(defmacro cmd [e & args-body]
  `(add-cmd ~e (fn ~@args-body)))

(event "NOTICE"
       [c x]
       (println "Notice: " x))

(event "PART"
       [c x]
       (println "Part"))

(event "PING"
       [c x]
       (write-to-out c (str "PONG :" (x 4)))
       (println "Pong Sent"))

(event "MODE"
       [c x]
       (join-channels c (@c :chans)))

(event "PRIVMSG"
       [c x]
       (let [name ((split (x 1) #"!") 0)
             cmd (if (= mark (str (first (x 4)))) (re-find pat (x 4)) "")
             channel (x 3)
             args (rest (split (x 4) #" "))
             info-map {:chan channel :nick name :cmd cmd :raw x :out c :args args}]
         (if (false? (contains? @ignore-list name)) ;;; Check for ignored users
           (if (contains? @cmd-handler cmd)         ;;; Is the cmd in the handler?
             ((@cmd-handler cmd) info-map)))
         (println cmd)))

;;; Lets sandbox this....better....
(def sb (sandbox secure-tester :timeout 5000))

(defn excp! [ev]
  (try (sb (read-string ev))
       (catch Exception e (str "Exception: " (.getMessage e)))))


;;; Common! Tell me how stupid i am!
(cmd "eval"
     [imap]
     (let [st (join " " (imap :args))]
       (write-to-irc imap (str "λ → " (excp! st)))))

(cmd "uptime"
     [imap]
     (write-to-irc imap "NOTIME"))

(cmd "say"
     [imap]
     (let [st (join " " (imap :args))]
       (write-to-irc imap st)))


;;; This is random. I am 100% sure!
(cmd "dice"
     [imap]
     (write-to-irc imap "4"))

(cmd "join"
     [imap]
     (let [chan (first (imap :args))]
       (join! (imap :out) chan)))

(cmd "part"
     [imap]
     (let [st (first (imap :args))]
       (write-to-out (imap :out) (str "PART :" st))))

(cmd "add-ignore"
     [imap]
     (let [st (first (imap :args))]
       (add-to-ignore st)))

(cmd "remove-ignore"
     [imap]
     (let [st (first (imap :args))]
       (reset! ignore-list (remove #(= % st) ignore-list))))

(cmd "whisper"
     [imap]
     (let  [chan (first (imap :args))
            say (join " " (rest (imap :args)))]
       (write-to-out (imap :out) (str "PRIVMSG " chan " :" say))))

(cmd "help"
     [imap]
     (let [cmd-help (join " " (keys @cmd-handler))]
       (write-to-irc imap cmd-help)))

(cmd "reload"
     [imap]
     (try (load-file "src/parjer/commands.clj")
          (catch Exception e (println (str "Exception: " (.getMessage e))))))

(cmd "kick"
     [imap]
     (let [nick (first (imap :args))]
       (write-to-out (imap :out) (str "KICK " (imap :chan) " " nick " :Bot Kick"))))

(cmd "quote"
     [imap]
     (let [qu (rand-quote)]
       (write-to-irc imap qu)))

(cmd "nick"
     [imap]
     (let [nick (first (imap :args))]
       (if (contains? owner (imap :nick))
         (write-to-out (imap :out) (str "NICK " nick)))))


(cmd "connect"
     [imap]
     (let [server (first (imap :args))
           port (Integer. (first (rest (imap :args))))
           channels (set (rest (rest (imap :args))))
           sock (Socket. server port)]
       (connect* sock channels)))
