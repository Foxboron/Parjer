(ns parjer.config)

(defn get-config []
  (read-string (slurp "setup")))

(def fetch-conf (memoize get-config))
