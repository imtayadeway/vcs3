(ns vcs3.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defn create-oscillator [context type]
  (let [oscillator (.createOscillator context)]
    (set! (.-type oscillator) type)
    (set! (.-value (.-frequency oscillator)) 440)
    (.connect oscillator (.-destination context))
    (.start oscillator)
    oscillator))

(defonce context (new js/window.AudioContext))
(defonce oscillator-1 (create-oscillator context "sine"))

(defonce vcs3-data
  (atom {:context context
         :oscillator-1 {:frequency 440 :shape "sine"}}))

(defn update-vcs3
  []
  (set! (.-value (.-frequency oscillator-1)) (:frequency (:oscillator-1 @vcs3-data))))

(defn turny-dial
  [component attribute value min max]
  [:input {:type "range" :value value :min min :max max
           :style {:width "100%"}
           :on-change (fn [e]
                        (swap! vcs3-data assoc-in [component attribute] (.. e -target -value))
                        (update-vcs3))}])

(defn vcs3 []
  [:div
   [:h3 "Oscillator 1"]
   [:p "Oscillator 1 is oscillating at " (:frequency (:oscillator-1 @vcs3-data)) " Hz."]
   [turny-dial :oscillator-1 :frequency (:frequency (:oscillator-1 @vcs3-data)) 1 10000]
   [:div
    [:img {:src "/images/vcs3.jpg" :alt "VCS3"}]]])

(reagent/render-component [vcs3]
                          (. js/document (getElementById "app")))

(defn on-js-reload [])
