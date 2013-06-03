(ns parjer2.events
  (:require [parjer2.file :as file]
           [parjer2.server :as server] ))


(def evt-handler
  (atom {}))


(defn add-event [event f]
  (swap! evt-handler assoc event f))


(def mark (:mark file/fetch-conf))


(def pat
  (re-pattern (str "[^" mark "][\\d\\w\\S]*")))


(defmacro event [e & args-body]
  `(add-event ~(str e) (fn ~@args-body)))



(defn evt-dispatch [[_ _ evt :as msg] serv]
  (if (contains? @evt-handler evt)
    ((@evt-handler evt) serv msg)))


(event PING
       [{out :out :as serv} [_ _ _ _ ping :as msg]]
       {:raw (str "PONG :" ping) :out out})

(event 266
       [{chans :chans out :out :as serv} msg]
       (doseq [i chans]
         (server/join serv i)))

(event PRIVMSG
       [serv msg]
       (println msg))
