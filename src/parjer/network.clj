(ns parjer.network
  (:require [parjer.parser :refer (irc-parse)]
            [parjer.config :refer (fetch-conf)])
  (:import (java.net Socket)
           (java.io PrintWriter InputStreamReader BufferedReader)))


(def nick ((fetch-conf) :nick))

(def server (fetch-conf))

(defn conn-handler [c]
  (while (nil? (:exit @c))
    (let [msg (.readLine (:in @c))]
      (irc-parse c msg))))

(defn connect [serv]
  (let [sock (Socket. (:server server) (:port server))
        in (BufferedReader. (InputStreamReader. (.getInputStream sock)))
        out (PrintWriter. (.getOutputStream sock))
        conn (ref {:in in :out out})]
    (doto
        (Thread.
         #(conn-handler conn)) (.start))
    conn))

(defn write-to-out [c msg]
  (doto (:out @c)
    (.println (str msg "\r"))
    (.flush)))

(defn send-info [conn]
  (write-to-out conn (str "NICK " nick))
  (write-to-out conn (str "USER " nick " 0 * :" nick)))

(defn write-to-irc [imap msg]
  (write-to-out (imap :out) (str "PRIVMSG " (imap :chan) " :" msg)))

(defn join? [c chan]
  (write-to-out c (str "JOIN :" chan)))

(defn join-channels
  ([c] (doseq [i (server :chan)] (join? c i)))
  ([c chan] (join? c chan)))

(defn ccn []
  (let [irc (connect server)]
    (send-info irc)))
