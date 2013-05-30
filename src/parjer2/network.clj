(ns parjer2.network
  (:require [clojure.java.io :as io]))


(defn send-info []
  (write-out {}))


(defmulti write-out
  "Writes to IRC"
  (fn [data]
    (cond
     (find data :raw) :raw
     :else nil)))


(defmethod write-out :raw
  [imap]
  (binding [*out* (:out imap)]
    (println (:msg imap))))


(defmethod write-out :default
  [imap]
  (binding [*out* (:out imap)]
    (println (str "PRIVMSG " (:chan imap) " :" (:msg imap)))))


(defn conn-handler [serv]
  (while (nil? (:exit serv))
    (let [msg (.readLine (:in serv))]
      (cond
       (re-find #"^Error :Closing Link:" msg)
       (dosync (alter serv merge {:exit true}))
       :else
       (cmd-)))))
