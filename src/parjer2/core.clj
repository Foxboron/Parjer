(ns parjer2.core
  (:require [parjer2.file :as file]
            [parjer2.server :as server :refer [connect]]
            [parjer2.network :as net])
  (:import [parjer2.server Server]))

(def server-list (atom []))

(def connected-server-list (atom []))

(def servers-list  (:servers file/fetch-conf))


(defn wrap-servers [servers]
  (into []
        (for [i servers
              :let [serv (Server. (:server i) (:port i) (:chans i))]]
          (do (swap! server-list conj serv)
              serv))))


(defn connect-servers [serv-list]
  (doseq [i serv-list]
    (let [conn (connect i)]
      (swap! connected-server-list conj conn))))

(defn -main [& args]
  (let [servs (wrap-servers servers-list)
        conns (connect-servers servs)]
    (doseq [i @connected-server-list]
      (future (net/conn-handler i)))))
