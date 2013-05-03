(ns parjer.network
  (:gen-class)
  (:require [parjer.parser :refer (irc-parse)]
            [parjer.config :refer (fetch-conf)]
            [clojure.java.io :as io])
  (:import (java.net Socket)))


(def nick ((fetch-conf) :nick))

(defn write-to-out [c msg]
 (binding [*out* (:out @c)]
      (println msg)))


(defn send-info [conn]
  (write-to-out conn (str "NICK " nick))
  (write-to-out conn (str "USER " nick " 0 * :" nick)))

(defn conn-handler [c]
  (send-info c)
  (while (nil? (:exit @c))
    (let [msg (.readLine (:in @c))]
      (cond
       (re-find #"^ERROR :Closing Link:" msg)
         (dosync (alter c merge {:exit true}))
       :else (irc-parse c msg)))))

(defn connect* [sock chans]
  (let [in (io/reader sock)
        out (io/writer sock)
        conn (ref {:in in :out out :chans chans})]
    (future (conn-handler conn))))

(defn connect [servs]
  (doseq [i servs]
    (let [s (Socket. (i :server) (i :port))]
      (connect* s (i :chans)))))

(defn write-to-irc [imap msg]
  (write-to-out (imap :out) (str "PRIVMSG " (imap :chan) " :" msg)))

(defn join! [c chan]
  (write-to-out c (str "JOIN :" chan)))

(defn join-channels
  ([c chans] (doseq [i chans] (join! c i))))

(defn ccn []
  (connect servers))
