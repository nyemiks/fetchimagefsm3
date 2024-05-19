(ns fetchimagefsm3.events
  (:require
   [re-frame.core :as re-frame :refer [debug]]
   [fetchimagefsm3.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
  ; [ajax.core :as ajax]    ;; so you can use this in the response-format below
   [ajax.protocols :refer [-body]]
    [re-statecharts.core :as rs]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))



(re-frame/reg-event-db
 ::update-image
 (fn-traced [db [_ url]]
              (.info js/console "image updated. url " url)        
          
            (assoc db :image url)
            )
 )



(re-frame/reg-event-fx                             ;; note the trailing -fx
  ::get-image   [debug]                  ;; usage:  (dispatch [::events/search-recipe])
  (fn [{:keys [db]} [_ uri ]]                    ;; the first param will be "world"
    (.info js/console "get image uri: " uri )
    {:db   (assoc db :show-twirly false)   ;; causes the twirly-waiting-dialog to show??
     :http-xhrio {
                  :method          :get
                  :uri             uri
                 ; :params          query-params
                  :timeout         8000                                           ;; optional see API docs
                ;  :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                  :response-format {
                        :content-type "image/png" 
                        :type :blob 
                        :description "PNG file" 
                        :read -body
                       }
                  :on-success      [::fetch-image-success]
                  :on-failure      [::fetch-image-failure]
                  }
     }
    )
 )



(re-frame/reg-event-fx
  ::fetch-image-success
  (fn [{:keys [db]} [_ result]]
    (.info js/console "image results: " result)

    (let [
          url (.createObjectURL js/URL result)
       ]
        {:db (assoc db :image url)
         :dispatch  [::rs/transition :imageFetcher :fetchimagefsm3.views/success-load]
         }
      )
   
   
    )
 )


(re-frame/reg-event-db
  ::fetch-image-failure
  (fn [db [_ result]]
    ;; result is a map containing details of the failure   
      (.info js/console "fetch image failed: " result)  
     (assoc db :recipe-info-error result :food-id  nil)
    )
 )
