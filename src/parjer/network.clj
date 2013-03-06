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

(defn write-to-irc [c chan msg]
  (write-to-out c (str "PRIVMSG " chan " :" msg)))

(defn join-channel
  ([c] (write-to-out c (str "JOIN " (server :chan))))
  ([c chan] (write-to-out c (str "JOIN " chan))))

(defn ccn []
  (let [irc (connect server)]
    (send-info irc)))
