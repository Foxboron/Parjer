(ns parjer.commands
  (:require [parjer.parser :as parser :only (add-event evt-handler)]
            [parjer.network :as net :only (writeToOut joinChannel)]
            [clojure.string :as s :only (split join)]))

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
                          cmd (re-find pat (x 4))]
                      (if cmd
                        (if (contains? @cmd-handler cmd)
                          ((@cmd-handler cmd) c x))
                        (println x)))))

;;; Common! Tell me how stupid i am!
(add-cmd "eval"
         (fn [c x]
           (let [st (s/join " " (rest (s/split (x 4) #" ")))]
             (net/writeToIRC c (x 3) (load-string st)))))

(add-cmd "uptime"
         (fn [c x]
           (net/writeToIRC c (x 3) "NOTIME")))

(add-cmd "say"
         (fn [c x]
           (let [st (s/join " " (rest (s/split (x 4) #" ")))]
             (net/writeToIRC c (x 3) st))))
