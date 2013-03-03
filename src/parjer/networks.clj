(ns parjer.networks
  (:use [clojure.java.io :only [reader writer]]
        [parjer.parser :as par])
  (:import (java.net Socket)
           (java.io PrintWriter InputStreamReader BufferedReader)))

(def nick "Cjoey")

(def live? (atom false))

(def server {:server "irc.codetalk.io"
             :port 6667
             :chan "#lobby"})

(defn conn-handler [c]
  (while (nil? (:exit @c))
    (let [msg (.readLine (:in @c))]
      (println msg)
      (par/irc-parse msg))))

(defn connect [serv]
  (let [sock (Socket. (:server server) (:port server))
        in (BufferedReader. (InputStreamReader. (.getInputStream sock)))
        out (PrintWriter. (.getOutputStream sock))
        conn (ref {:in in :out out})]
    (doto
        (Thread.
         #(conn-handler conn)) (.start))
    conn))


(defn writeToOut [c msg]
  (doto (:out @c)
    (.println (str msg "\r"))
    (.flush)))

(defn sendinfo [conn]
  (writeToOut conn (str "NICK " nick))
  (writeToOut conn (str "USER " nick " 0 * : " nick)))


(defn join [x]
  nil)

(defn ccn []
  (let [irc (connect server)]
    (sendinfo irc)))
