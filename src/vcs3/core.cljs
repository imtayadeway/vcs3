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
           :oscillator-1 {:frequency 440}})))

(defn oscillator-1-inner []
  (let [osc (atom (create-oscillator (new js/window.AudioContext), "sine"))
        update (fn [comp]
                 (let [{frequency :frequency} (reagent/props comp)]
                   (set! (.-value (.-frequency @osc)) frequency)))]

    (reagent/create-class
     {:reagent-render (fn [])
      :component-will-mount (fn [comp] (update comp))
      :component-did-update update
      :display-name "oscillator-1-inner"})))

(defn oscillator-1-outer []
  (let [data (atom {:frequency 1})]
    (fn []
      [:div
       [:h3 "Oscillator 1"]
       [:p "Oscillator 1 is oscillating at " (:frequency @data) " Hz."]
       [:input {:type "range" :value (:frequency @data) :min 1 :max 10000
                :style {:width "100%"}
                :on-change (fn [e] (swap! data assoc :frequency (.. e -target -value)))}]
       [oscillator-1-inner @data]])))

(defn vcs3 []
  [:div
   [oscillator-1-outer]
   [:div
    [:img {:src "/images/vcs3.jpg" :alt "VCS3"}]]])

(reagent/render-component [vcs3]
                          (. js/document (getElementById "app")))

(defn on-js-reload [])
