(ns parjer2.parser
  (:gen-class))


(defn re-parse [x]
  (re-matches #"^(?:[:](\S+) )?(\S+)(?: (?!:)(.+?))?(?: [:](.+))?$" x))


(defn irc-parse [msg]
  (re-parse msg))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; (defn irc-parse [serv msg]                    ;;
;;   (let [msg (re-parse msg)                    ;;
;;         cmd (msg 2)]                          ;;
;;     (if (contains? @evt-handler cmd)          ;;
;;       (future ((@evt-handler cmd) serv msg))) ;;
;;     (println msg)))                           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
