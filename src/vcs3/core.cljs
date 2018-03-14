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
(defonce oscillator-2 (create-oscillator context "square"))
(defonce oscillator-3 (create-oscillator context "square"))
(defonce vcs3-data (atom {:oscillator-1 {:frequency 1}
                          :oscillator-2 {:frequency 1}
                          :oscillator-3 {:frequency 0.05}}))

(add-watch vcs3-data :oscillator-1-watcher
           (fn [_ _ old-state new-state]
             (when (not= (:oscillator-1 old-state) (:oscillator-1 new-state))
               (set! (.-value (.-frequency oscillator-1)) (:frequency (:oscillator-1 new-state))))))

(add-watch vcs3-data :oscillator-2-watcher
           (fn [_ _ old-state new-state]
             (when (not= (:oscillator-2 old-state) (:oscillator-2 new-state))
               (set! (.-value (.-frequency oscillator-2)) (:frequency (:oscillator-2 new-state))))))

(add-watch vcs3-data :oscillator-3-watcher
           (fn [_ _ old-state new-state]
             (when (not= (:oscillator-3 old-state) (:oscillator-3 new-state))
               (set! (.-value (.-frequency oscillator-3)) (:frequency (:oscillator-3 new-state))))))

(defn oscillator-1-frequency []
  [:div
   [:h6 "Frequency"]
   [:p "Oscillator 1 is oscillating at " (:frequency (:oscillator-1 @vcs3-data)) " Hz."]
   [:input {:type "range" :value (:frequency (:oscillator-1 @vcs3-data)) :min 1 :max 10000
            :style {:width "100%"}
            :on-change (fn [e] (swap! vcs3-data assoc-in [:oscillator-1 :frequency] (.. e -target -value)))}]])

(defn oscillator-2-frequency []
  [:div
   [:h6 "Frequency"]
   [:p "Oscillator 2 is oscillating at " (:frequency (:oscillator-2 @vcs3-data)) " Hz."]
   [:input {:type "range" :value (:frequency (:oscillator-2 @vcs3-data)) :min 1 :max 10000
            :style {:width "100%"}
            :on-change (fn [e] (swap! vcs3-data assoc-in [:oscillator-2 :frequency] (.. e -target -value)))}]])

(defn oscillator-3-frequency []
  [:div
   [:h6 "Frequency"]
   [:p "Oscillator 3 is oscillating at " (:frequency (:oscillator-3 @vcs3-data)) " Hz."]
   [:input {:type "range" :value (:frequency (:oscillator-3 @vcs3-data)) :min 0.05 :max 500
            :style {:width "100%"}
            :on-change (fn [e] (swap! vcs3-data assoc-in [:oscillator-3 :frequency] (.. e -target -value)))}]])

(defn vcs3 []
  [:div
   [:div
    [:h3 "Oscillator 1"]
    [oscillator-1-frequency]]
   [:div
    [:h3 "Oscillator 2"]
    [oscillator-2-frequency]]
   [:div
    [:h3 "Oscillator 3"]
    [oscillator-3-frequency]]
   [:div
    [:img {:src "/images/vcs3.jpg" :alt "VCS3"}]]])

(reagent/render-component [vcs3]
                          (. js/document (getElementById "app")))

(defn on-js-reload [])
