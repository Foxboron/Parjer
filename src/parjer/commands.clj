(ns parjer.commands
  (:gen-class)
  (:require [parjer.config :refer (fetch-conf)]
            [parjer.network :as net :refer (join! write-to-out join-channels write-to-irc connect*)]
            [parjer.quote :refer :all]
            [parjer.swear :refer :all]
            [clojure.string :as s :refer (split join)]
            [clojail.core :refer [sandbox]]
            [clojail.testers :refer [secure-tester]])
  (:import (java.net Socket)))

(def ignore-list (atom #{}))

(def help-list (atom {}))

(def cmd-handler (atom {}))

(defn add-cmd [event f]
  (swap! cmd-handler assoc event f))

(defn add-help [event msg]
  (swap! help-list assoc event msg))

(defn add-to-ignore [name]
  (swap! ignore-list conj name))

(defmacro cmd
  [e help & args-body]
  `(do (add-cmd ~e (fn ~@args-body))
       (add-help ~e ~help)))

(def owner ((fetch-conf) :owner))

;;; Lets sandbox this....better....
(def sb (sandbox secure-tester :timeout 5000))

(defn excp! [ev]
  (try (do (sb (read-string ev) {#'*out* writer}) (str writer))
       (catch Exception e (str "Exception: " (.getMessage e)))))

(cmd "eval"
     "eval [& stuff] | Evals the given stuff in a 'secure' sandbox."
     [imap]
     (let [st (join " " (imap :args))]
       (write-to-irc imap (str "λ → " (excp! st)))))

(def start-timer (. System (nanoTime)))
(defn uptime [x]
  (let [def-time (bigint (/ (bigint (- (. System (nanoTime)) x)) 1000000000.0))
        sec (mod def-time 60)
        mins (bigint (/ def-time 60))
        hours (bigint (/ mins 60))
        days (bigint (/ hours 24))]
    (str days "d " hours "h "  mins "m " sec "s total: " def-time "s")))

(cmd "uptime"
     "uptime | Does nothing."
     [imap]
    (write-to-irc imap (uptime start-timer)))

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
     (write-to-out (imap :out) (str "PART :" (imap :arg))))

(cmd "add-ignore"
     "add-ignore [nick] | Adds the given nick too the ignore list."
     [imap]
     (add-to-ignore (imap :arg)))

(cmd "remove-ignore"
     "remove-ignore [nick] | Removes the given nick from the ignore list."
     [imap]
     (reset! ignore-list (remove #(= % (imap :arg)) ignore-list)))

(cmd "whisper"
     "whisper [channel|nick] [& stuff] | Tells stuff too the given channel or nick."
     [imap]
     (let  [chan (imap :arg)
            say (join " " (rest (imap :args)))]
       (write-to-out (imap :out) (str "PRIVMSG " chan " :" say))))

(cmd "help"
     "help [cmd] | Displays the help for the given cmd."
     [imap]
     (let [word (imap :arg)
           l (join " " (apply sorted-set (keys @cmd-handler)))]
       (if (not= word nil)
         (if (contains? @help-list word)
           (write-to-irc imap (@help-list word))
           (write-to-irc imap "No help added."))
         (write-to-irc imap l))))

(cmd "reload"
     "reload | Reloads the commands.clj file (hacky stuff)."
     [imap]
     (try
       (load-file "src/parjer/commands.clj")
          (catch Exception e (println (str "Exception: " (.getMessage e))))))

(cmd "kick"
     "kick [nick] | Kick the given person."
     [imap]
     (let [nick (imap :arg)]
       (write-to-out (imap :out) (str "KICK " (imap :chan) " " nick " :Bot Kick"))))

(cmd "quote"
     "quote | Gives a random awsome quote."
     [imap]
     (let [qu (rand-quote)]
       (write-to-irc imap qu)))

(cmd "nick"
     "nick | Changes the nick. Needs owner."
     [imap]
     (let [nick (imap :arg)]
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

(cmd "swear"
     "swear {nick} | Swear at thou nick!"
     [imap]
     (let [nick (imap :arg)]
       (write-to-irc imap (rand-swear nick))))
