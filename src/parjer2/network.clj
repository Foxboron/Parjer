(ns parjer2.network
  (:require [clojure.java.io :as io]
            [parjer2.events :as evt]
            [parjer2.parser :as re]))

(defmulti write-out
  "Writes to IRC"
  (fn [data]
    (cond
     (find data :raw) :raw
     :else nil)))


(defmethod write-out :raw
  [{out :out, msg :raw :as imap}]
  (binding [*out* out]
    (println msg)))


(defmethod write-out :default
  [{chan :chan, msg :msg, out :out :as imap}]
  (binding [*out* out]
    (println (str "PRIVMSG " chan " :" msg))))


(defn send-info [{out :out :as serv}]
  (write-out {:raw "NICK Test" :out out})
  (write-out {:raw "USER Test 0 * :Test" :out out}))


(defn dispatch 
  [msg serv]
  (let [msg (re/re-parse msg)
        evt (evt/evt-dispatch msg serv)]
    (write-out evt)
    (println evt)))


(defn conn-handler [serv]
  (send-info serv)
  (while (nil? (:exit serv))
    (let [msg (.readLine (:in serv))]
      (println msg)
      (cond
       (re-find #"^Error :Closing Link:" msg)
         (dosync (alter serv merge {:exit true}))
       :else
       (future (dispatch msg serv))))))
