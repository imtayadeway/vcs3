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

(defonce vcs3-data
  (let [context (new js/window.AudioContext)]
    (atom {:context context
           :oscillator-1 (create-oscillator context "sine")})))

(defn turny-dial
  [component value min max]
  [:input {:type "range" :value value :min min :max max
           :style {:width "100%"}
           :on-change (fn [e]
                        (swap! vcs3-data assoc :rand (rand)) ;; ugh
                        (swap! vcs3-data (fn [data]
                                           (set! (.-value (.-frequency (component data))) (.. e -target -value))
                                           data)))}])

(defn vcs3 []
  [:div
   [:div [:h3 "Oscillator 1"]
    [:p "Oscillator 1 is oscillating at " (.-value (.-frequency (:oscillator-1 @vcs3-data))) " Hz."]
    [turny-dial :oscillator-1 (.-value (.-frequency (:oscillator-1 @vcs3-data))) 1 10000]]
   [:div
    [:img {:src "/images/vcs3.jpg" :alt "VCS3"}]]])

(reagent/render-component [vcs3]
                          (. js/document (getElementById "app")))

(defn on-js-reload [])
