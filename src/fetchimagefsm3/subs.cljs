(ns fetchimagefsm3.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))



(re-frame/reg-sub
 ::image
 (fn [db]
   (:image db))
 )
