(ns parjer.commands
  (:require [parjer.parser :as parser :only (add-event evt-handler)]
            [parjer.network :as net :only (writeToOut joinChannel)]
            [clojure.string :as s :only (split join)]))

(def owner "Foxboron")
(def cmd-handler (atom {}))

(defn add-cmd [event f]
  (swap! cmd-handler assoc event f))

(def mark "@")

(def pat (re-pattern (str "[^" mark "][\\d\\w\\S]*")))

(parser/add-event "NOTICE"
           (fn [c x]
             (println "Notice: " x)))

(parser/add-event "PART"
           (fn [c x]
             (println "Part")))

(parser/add-event "PING"
           (fn [c x]
             (net/writeToOut c (str "PONG :" (x 4)))
             (println "Pong Sent")))

(parser/add-event "MODE"
                  (fn [c x]
                    (net/joinChannel c)))

(parser/add-event "PRIVMSG"
                  (fn [c x]
                    (let [name ((clojure.string/split (x 1) #"!") 0)
                          cmd (re-find pat (x 4))
                          channel (x 3)]
                      (if cmd
                        (if (= name owner)
                            (if (contains? @cmd-handler cmd)
                              ((@cmd-handler cmd) c x channel)))
                        (println x)))))

(defn excp! [ev]
  (try (load-string ev)
       (catch Exception e (str "Exception: " (.getMessage e)))))


;;; Common! Tell me how stupid i am!
(add-cmd "eval"
         (fn [c x channel]
           (let [st (s/join " " (rest (s/split (x 4) #" ")))]
             (net/writeToIRC c channel (excp! st)))))

(add-cmd "uptime"
         (fn [c x channel]
           (net/writeToIRC c channel "NOTIME")))

(add-cmd "say"
         (fn [c x channel]
           (let [st (s/join " " (rest (s/split (x 4) #" ")))]
             (net/writeToIRC c channel st))))

;;; This is random. I am 100% sure!
(add-cmd "dice"
         (fn [c x channel]
           (net/writeToIRC c channel "4")))

(add-cmd "join"
         (fn [c x channel]
           (let [st ((s/split (x 4) #" ") 1)]
             (net/joinChannel c st))))
(add-cmd "part"
         (fn [c x channel]
           (let [st ((s/split (x 4) #" ") 1)]
             (net/writeToOut (str "PART " st)))))
