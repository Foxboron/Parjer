(ns parjer2.server
  (:require [parjer2.network :as network]
            [clojure.java.io :as io])
  (:import  [java.net Socket]))


(defrecord Server [server port chans])
(defrecord ServerConnected [server port chans in out])

(defprotocol ServerHandler
  "Handels Servers."
  (connect [_] "connects too the server")
  (join [server chan] "Join a server.")
  (part [server chan] "parts a channel")
  (quit [server] "Quits a channel."))

(extend-protocol ServerHandler
  Server
  (connect [server]
    (let [s (Socket. (:server server) (:port server))
          in (io/reader s)
          out (io/writer s)]
      (network/write-out {:msg (str "NICK Test") :out out})
      (network/write-out {:msg (str "USER Test 0 * :User Test") :out out})
      (ServerConnected. (:server server) (:port server) (:chans server) in out)))

  ServerConnected
  (join [server chan]
    (network/write-out {:raw (str "JOIN :" chan) :out (:out server)}))
  (part [server chan]
    {:raw (str "PART :" chan) :out (:out server)})
  (quit [server]
    {:raw "QUIT :bye!" :out (:out server)}))
