(ns fetchimagefsm3.views
  (:require
  ; [reagent.core :as r]
   [re-frame.core :as re-frame]
   [fetchimagefsm3.subs :as subs]
   [fetchimagefsm3.events :as events]
   [ajax.protocols :refer [-body]]
    [re-statecharts.core :as rs]
   ; [statecharts.core :as fsm :refer [assign]] 
   [statecharts.integrations.re-frame :as fsm.rf]
   )
  )


(def image-url "https://picsum.photos/200/300")




(defn load-image [state data]
   (.info js/console " -- load image -- ")
  (.info js/console " context:  " state)
   (.info js/console " data:  " data)
  

  (.info js/console " call-fx ... ")

 ;  (re-frame/dispatch [::events/get-image image-url ])
  
    (fsm.rf/call-fx
   {:http-xhrio
    {
     :uri image-url
     :method :get
     :response-format {
                        :content-type "image/png" 
                        :type :blob 
                        :description "PNG file" 
                        :read -body
                       }
     :on-failure [::rs/transition :imageFetcher ::fail-load]
      :on-success [::rs/transition :imageFetcher ::success-load]
  
     }
    }
   )

  )



(defn update-image [data]
  (.info js/console " -- update image -- ")
  (let  
      [  
       url (.createObjectURL js/URL data) 
       ] 
    (re-frame/dispatch [::events/update-image url])  ;;  
    )
  )


(def fetch-image-fsm 
   {
     :id :imageFetcher,
     :initial ::ready,
     :context {
       :image nil
     },
   :entry   (fn [& _]
                           (.info js/console "fm initialized ! just before ready state ")
                            )
    :states {
       ::ready {
                   :entry  (fn [& _]
                             (.info js/console "now in ready state ! ")
                             )
                   :on {
                          ::BUTTON_CLICKED {
                                            :target ::fetching
                                            :actions (fn [& _]
                                                        (.info js/console "transition to fetching state ! ")
                                                       )
                                            }
                        }
                },
       ::fetching {
                  :entry  load-image
                                                                             
                  :on  {
                          ::success-load {                                        
                                          :target  ::success

                                          ; 20240518 does not seem to work with re-statechart library. 
                                          ; especially if transition was triggered programmatically 
                                          ; e.g by callfx. instead implement the logic as part of the 
                                          ;action of the new state in this case logic  was implemented at success state
                                           
                                           ; :actions (assign on-image-loaded)  
                                       }
                           ::fail-load    {
                                          
                                            :target  ::error
                                           
                                            ; 20240518 does not seem to work with re-statechart library. 
                                          ; especially if transition was triggered programmatically 
                                          ; e.g by callfx. instead implement the logic as part of the 
                                          ;action of the new state in this case logic  should be implemented at error state
                                         
                                            ; :actions on-image-load-failed
                                          }
                        }
			      }, 
              ::success {
                         :entry  (fn [state evt]
                             (.info js/console "now in success state ! ")
                                   (.info js/console "state: " state)
                                   (.info js/console "evt: " evt)

                                   (let 
                                        [
                                           {:keys [data]} evt
                                        ]
                                        (update-image data)  
                                     )
                             )

              },
              ::error {
                        :entry  (fn [state evt]
                                    (.info js/console "now in error state ! ")
                                   (.info js/console "state: " state)
                                   (.info js/console "evt: " evt)
                                  (println "error: " evt)

                                  )
              }
             }    
       
     }
  
  )




(defn image-viewer []
  (let
       [
         name (re-frame/subscribe [::subs/name])
           _     (re-frame/dispatch [::rs/start fetch-image-fsm])
          
         state (re-frame/subscribe [::rs/state :imageFetcher])
       
         image (re-frame/subscribe [::subs/image])
        
        ]
      (.info js/console "current state" state)
      (fn []
       [:section
        [:h1 @name]
     
        (when (= @state ::fetching) [:p "loading..."])
        (when (= @state ::success)  [:div [:img {
                                   :src @image
                                   :alt ""
                                   :style {
                                           :height "150px"
                                           :width "150px"
                                           :border "solid gray 3px" 
                                           :border-radius "10px"
                                           }
                                   }
                             ]
                       ]
              )
        (when (= @state ::error) [:p "An error occured"])
        [:button {
                  :on-click (fn []
                              (.info js/console "get image ... ")
                                   
                                   (if (= @state ::ready) 
                                     (re-frame/dispatch [::rs/transition :imageFetcher ::BUTTON_CLICKED])                                                         
                                    (do  ; (.info js/console "evt: " evt)
                                      (re-frame/dispatch [::rs/restart (:id fetch-image-fsm) ])
                                       (re-frame/dispatch [::rs/transition :imageFetcher ::BUTTON_CLICKED])                                   
                                      )
                                     )
                                 
                               ;  (re-frame/dispatch [::rs/transition :imageFetcher ::BUTTON_CLICKED])   ; original code                                                      
        
                              )
                  } "Get Image"]
       ])
      ; (finally (re-frame/dispatch [::rs/stop (:id fetch-image-fsm)]))
       )
  )




(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1
      "Hello from " @name]
     ])
  )
