(ns vcs3.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defonce context (new js/window.AudioContext))

(defn create-oscillator [type frequency gain]
  (let [oscillator (.createOscillator context)]
    (set! (.-type oscillator) type)
    (set! (.-value (.-frequency oscillator)) frequency)
    (.start oscillator)
    (.connect oscillator gain)
    oscillator))

(defn create-gain [value]
  (let [gain (.createGain context)]
    (set! (.-value (.-gain gain)) value)
    gain))

(defonce oscillator-1-level (create-gain 0))
(defonce oscillator-1 (create-oscillator "sine" 0.6 oscillator-1-level))
(defonce oscillator-2-level (create-gain 0))
(defonce oscillator-2 (create-oscillator "square" 0.6 oscillator-2-level))
(defonce oscillator-3-level (create-gain 0))
(defonce oscillator-3 (create-oscillator "square" 0.015 oscillator-3-level))
(defonce vcs3-data (atom {:oscillator-1 {:frequency 0.6 :level-1 0}
                          :oscillator-2 {:frequency 0.6 :level-1 0}
                          :oscillator-3 {:frequency 0.015 :level-1 0}
                          :matrix {:oscillator-1 {:output-1 false}
                                   :oscillator-2 {:output-1 false}
                                   :oscillator-3 {:output-1 false}}}))

(defn oscillator-watcher-fn
  [key oscillator level]
  (fn [_ _ old-state new-state]
    (let [changed (fn [& args] (apply not= (map #(get-in % args) [old-state new-state])))]
      (when (changed key :frequency)
        (set! (.-value (.-frequency oscillator)) (->> new-state key :frequency)))
      (when (changed :matrix key :output-1)
        (if (->> new-state :matrix key :output-1)
          (.connect level (.-destination context))
          (.disconnect level (.-destination context))))
      (when (changed key :level-1)
        (set! (.-value (.-gain level)) (get-in new-state [key :level-1]))))))

(add-watch vcs3-data :oscillator-1-watcher (oscillator-watcher-fn :oscillator-1 oscillator-1 oscillator-1-level))
(add-watch vcs3-data :oscillator-2-watcher (oscillator-watcher-fn :oscillator-2 oscillator-2 oscillator-2-level))
(add-watch vcs3-data :oscillator-3-watcher (oscillator-watcher-fn :oscillator-3 oscillator-3 oscillator-3-level))

(defn oscillator-1-frequency []
  [:div
   [:h6 "Frequency"]
   [:p "Oscillator 1 is oscillating at " (:frequency (:oscillator-1 @vcs3-data)) " Hz."]
   [:input {:type "range" :value (:frequency (:oscillator-1 @vcs3-data)) :min 0.6 :max 16750
            :style {:width "100%"}
            :on-change (fn [e] (swap! vcs3-data assoc-in [:oscillator-1 :frequency] (.. e -target -value)))}]])

(defn oscillator-2-frequency []
  [:div
   [:h6 "Frequency"]
   [:p "Oscillator 2 is oscillating at " (:frequency (:oscillator-2 @vcs3-data)) " Hz."]
   [:input {:type "range" :value (:frequency (:oscillator-2 @vcs3-data)) :min 0.6 :max 16750
            :style {:width "100%"}
            :on-change (fn [e] (swap! vcs3-data assoc-in [:oscillator-2 :frequency] (.. e -target -value)))}]])

(defn oscillator-3-frequency []
  [:div
   [:h6 "Frequency"]
   [:p "Oscillator 3 is oscillating at " (:frequency (:oscillator-3 @vcs3-data)) " Hz."]
   [:input {:type "range" :value (:frequency (:oscillator-3 @vcs3-data)) :min 0.015 :max 500
            :style {:width "100%"}
            :on-change (fn [e] (swap! vcs3-data assoc-in [:oscillator-3 :frequency] (.. e -target -value)))}]])

(defn patch [from to]
  (let [checked (to (from (:matrix @vcs3-data)))]
    [:input {:type "checkbox" :checked checked
             :on-change #(swap! vcs3-data assoc-in [:matrix from to] (not checked))}]))

(defn oscillator-1-level-sine []
  [:div
   [:h6 "Level (sine)"]
   [:input {:type "range"
            :value (get-in @vcs3-data [:oscillator-1 :level-1])
            :min 0
            :max 10
            :step 0.1
            :on-change (fn [e] (swap! vcs3-data assoc-in [:oscillator-1 :level-1] (.. e -target -value)))}]])

(defn oscillator-2-level-square []
  [:div
   [:h6 "Level (square)"]
   [:input {:type "range"
            :value (get-in @vcs3-data [:oscillator-2 :level-1])
            :min 0
            :max 10
            :step 0.1
            :on-change (fn [e] (swap! vcs3-data assoc-in [:oscillator-2 :level-1] (.. e -target -value)))}]])

(defn oscillator-3-level-square []
  [:div
   [:h6 "Level (square)"]
   [:input {:type "range"
            :value (get-in @vcs3-data [:oscillator-3 :level-1])
            :min 0
            :max 10
            :step 0.1
            :on-change (fn [e] (swap! vcs3-data assoc-in [:oscillator-3 :level-1] (.. e -target -value)))}]])

(defn vcs3 []
  [:div
   [:div
    [:h3 "Oscillator 1"]
    [oscillator-1-frequency]
    [oscillator-1-level-sine]]
   [:div
    [:h3 "Oscillator 2"]
    [oscillator-2-frequency]
    [oscillator-2-level-square]]
   [:div
    [:h3 "Oscillator 3"]
    [oscillator-3-frequency]
    [oscillator-3-level-square]]
   [:div
    [:h3 "Matrix Board"]
    [:table
     [:tbody
      [:tr.matrix-outputs
       [:td]
       [:th "Output Ch. 1"]]
      [:tr
       [:th "Oscillator 1 (sine)"]
       [:td [patch :oscillator-1 :output-1]]]
      [:tr
       [:th "Oscillator 2 (square)"]
       [:td [patch :oscillator-2 :output-1]]]
      [:tr
       [:th "Oscillator 3 (square)"]
       [:td [patch :oscillator-3 :output-1]]]]]]
   [:div
    [:img {:src "/images/vcs3.jpg" :alt "VCS3"}]]])

(reagent/render-component [vcs3]
                          (. js/document (getElementById "app")))

(defn on-js-reload [])
