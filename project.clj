(defproject parjer2 "0.2.0-SNAPSHOT"
  :description "Rewrite of parjer"
  :url "http://github.com/Foxboron"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main parjer2.core
  :plugins [[lein-daemon "0.5.1"]]
  :daemon {:parjer {:ns parjer2.core
                    :pidfile "parjer.pid"}
           ":parjer" {:ns parjer2.core
                      :pidfile "parjer.pid"}}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clojail "1.0.5"]
                 [enlive "1.0.1"]])
