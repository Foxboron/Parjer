(ns parjer.commands
  (:require [parjer.parser :as parser :refer (add-event evt-handler)]
            [parjer.config :refer (fetch-conf)]
            [parjer.network :as net :refer (join! write-to-out join-channels write-to-irc connect*)]
            [parjer.quote :refer :all]
            [clojure.string :as s :refer (split join)]
            [clojail.core :refer [sandbox]]
            [clojail.testers :refer [secure-tester]])
  (:import (java.net Socket)))

(def cmd-handler (atom {}))

(def ignore-list (atom #{}))

(def help-list (atom {}))

(defn add-help [event msg]
  (swap! help-list assoc event msg))

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

(defmacro cmd
  [e help & args-body]
  `(do (add-cmd ~e (fn ~@args-body))
       (add-help ~e ~help)))

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
             info-map {:chan channel :nick name :cmd cmd :raw x :out c :args args}]
         (if (false? (contains? @ignore-list name)) ;;; Check for ignored users
           (if (contains? @cmd-handler cmd)         ;;; Is the cmd in the handler?
             ((@cmd-handler cmd) info-map))
           (println cmd))))

;;; Lets sandbox this....better....
(def sb (sandbox secure-tester :timeout 5000))

(defn excp! [ev]
  (try (sb (read-string ev))
       (catch Exception e (str "Exception: " (.getMessage e)))))


;;; Common! Tell me how stupid i am!
(cmd "eval"
     "eval [& stuff] | Evals the given stuff in a 'secure' sandbox."
     [imap]
     (let [st (join " " (imap :args))]
       (write-to-irc imap (str "λ → " (excp! st)))))

(cmd "uptime"
     "uptime | Does nothing."
     [imap]
     (write-to-irc imap "NOTIME"))

(cmd "say"
     "say [& stuff] | Tells you stuff."
     [imap]
     (let [st (join " " (imap :args))]
       (write-to-irc imap st)))


;;; This is random. I am 100% sure!
(cmd "dice"
     "dice | Rolls the dice XKCD style!"
     [imap]
     (write-to-irc imap "4"))

(cmd "join"
     "join [chan] | Makes the bot join the given channel."
     [imap]
     (let [chan (first (imap :args))]
       (join! (imap :out) chan)))

(cmd "part"
     "part [chan] | Makes the bot leave the given chan."
     [imap]
     (let [st (first (imap :args))]
       (write-to-out (imap :out) (str "PART :" st))))

(cmd "add-ignore"
     "add-ignore [nick] | Adds the given nick too the ignore list."
     [imap]
     (let [st (first (imap :args))]
       (add-to-ignore st)))

(cmd "remove-ignore"
     "remove-ignore [nick] | Removes the given nick from the ignore list."
     [imap]
     (let [st (first (imap :args))]
       (reset! ignore-list (remove #(= % st) ignore-list))))

(cmd "whisper"
     "whisper [channel|nick] [& stuff] | Tells stuff too the given channel or nick."
     [imap]
     (let  [chan (first (imap :args))
            say (join " " (rest (imap :args)))]
       (write-to-out (imap :out) (str "PRIVMSG " chan " :" say))))

(cmd "help"
     "help [cmd] | Displays the help for the given cmd."
     [imap]
     (let [word (first (imap :args))
           l (join " " (apply sorted-set (keys @cmd-handler)))]
       (if (not= word nil)
         (if (contains? @help-list word)
           (write-to-irc imap (@help-list word))
           (write-to-irc imap "No help added."))
         (write-to-irc imap l))))

(cmd "reload"
     "reload | Reloads the commands.clj file (hacky stuff)."
     [imap]
     (try (load-file "src/parjer/commands.clj")
          (catch Exception e (println (str "Exception: " (.getMessage e))))))

(cmd "kick"
     "kick [nick] | Kick the given person."
     [imap]
     (let [nick (first (imap :args))]
       (write-to-out (imap :out) (str "KICK " (imap :chan) " " nick " :Bot Kick"))))

(cmd "quote"
     "quote | Gives a random awsome quote."
     [imap]
     (let [qu (rand-quote)]
       (write-to-irc imap qu)))

(cmd "nick"
     "nick | Changes the nick. Needs owner."
     [imap]
     (let [nick (first (imap :args))]
       (if (contains? owner (imap :nick))
         (write-to-out (imap :out) (str "NICK " nick)))))

(cmd "connect"
     "connect [server] [port] [chan] | Connects too a new network."
     [imap]
     (let [server (first (imap :args))
           port (Integer. (first (rest (imap :args))))
           channels (set (rest (rest (imap :args))))
           sock (Socket. server port)]
       (connect* sock channels)))

(cmd "quit"
     "quit | Quits the network."
     [imap]
     (write-to-out (imap :out) "QUIT :C ya suckers!"))
