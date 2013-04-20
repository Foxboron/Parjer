(ns parjer.core
    (:gen-class)
  (:require [parjer.network :as rt]
            [parjer.commands]
            [parjer.events]))


(defn -main [& args]
  (rt/ccn))
