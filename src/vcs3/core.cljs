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
(defonce vcs3-data (atom {:oscillator-1 {:frequency 1}}))

(add-watch vcs3-data :oscillator-1-watcher
           (fn [key atom old-state new-state]
             (when (not= (:oscillator-1 old-state) (:oscillator-1 new-state))
               (set! (.-value (.-frequency oscillator-1)) (:frequency (:oscillator-1 new-state))))))

(defn oscillator-1-frequency []
  [:div
   [:h6 "Frequency"]
   [:p "Oscillator 1 is oscillating at " (:frequency (:oscillator-1 @vcs3-data)) " Hz."]
   [:input {:type "range" :value (:frequency (:oscillator-1 @vcs3-data)) :min 1 :max 10000
            :style {:width "100%"}
            :on-change (fn [e] (swap! vcs3-data assoc-in [:oscillator-1 :frequency] (.. e -target -value)))}]])

(defn vcs3 []
  [:div
   [:div
    [:h3 "Oscillator 1"]
    [oscillator-1-frequency]]
   [:div
    [:img {:src "/images/vcs3.jpg" :alt "VCS3"}]]])

(reagent/render-component [vcs3]
                          (. js/document (getElementById "app")))

(defn on-js-reload [])
