(ns parjer.parser
  (:use [parjer.networks :as net]))

(def evt-handler
  (atom {}))

(defn add-event [event f]
  (swap! evt-handler assoc event f))

(defn re-parse [x]
  (re-matches #"^(?:[:](\S+) )?(\S+)(?: (?!:)(.+?))?(?: [:](.+))?$" x))

(defn irc-parse [x]
  (let [msg (re-parse x)
        cmd (msg 2)]
    ((@evt-handler cmd) msg)))
