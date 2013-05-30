(ns parjer2.events)


(def evt-handler
  (atom {}))


(defn add-event [event f]
  (swap! evt-handler assoc event f))


(def mark ((fetch-conf) :mark))


(def pat
  (re-pattern (str "[^" mark "][\\d\\w\\S]*")))


(defmacro event [e & args-body]
  `(add-event ~(str e) (fn ~@args-body)))


(event PART
       []
       (println "PART"))
