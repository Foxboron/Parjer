(ns parjer.parser
    (:gen-class))

(def evt-handler
  (atom {}))

(defn add-event [event f]
  (swap! evt-handler assoc event f))

(defn re-parse [x]
  (re-matches #"^(?:[:](\S+) )?(\S+)(?: (?!:)(.+?))?(?: [:](.+))?$" x))

(defn irc-parse [conn x]
  (let [msg (re-parse x)
        cmd (msg 2)]
    (if (contains? @evt-handler cmd)
      (future ((@evt-handler cmd) conn msg)))
    (println cmd) ;; cmd
    (println x))) ;; x
