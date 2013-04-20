(ns parjer.config
    (:gen-class))

(defn get-config []
  (read-string (slurp "setup")))

(def fetch-conf (memoize get-config))
