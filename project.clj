(defproject parjer "1.33.7"
  :description "Clojure IRC Bot"
  :url "http://www.github.com/Foxboron/Parjer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                  [clojail "1.0.5"]
                  [enlive "1.0.1"]]
  :plugins [[lein-daemon "0.5.1"]]
  :main parjer.core
  :daemon {:parjer {:ns parjer.core
                    :pidfile "parjer.pid"}
           ":parjer" {:ns parjer.core
                        :pidfile "parjer.pid"}})
