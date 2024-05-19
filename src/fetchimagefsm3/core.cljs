(ns fetchimagefsm3.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [fetchimagefsm3.events :as events]
   [fetchimagefsm3.views :as views]
   [fetchimagefsm3.config :as config] 
   [day8.re-frame.http-fx]
   [re-statecharts.core]
   ))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
  ;  (rdom/render [views/main-panel] root-el)
     (rdom/render [views/image-viewer] root-el)
    )
  )

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
