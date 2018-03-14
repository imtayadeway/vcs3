(ns vcs3.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defonce context (new js/window.AudioContext))

(defn create-oscillator [type frequency]
  (let [oscillator (.createOscillator context)]
    (set! (.-type oscillator) type)
    (set! (.-value (.-frequency oscillator)) frequency)
    (.start oscillator)
    oscillator))

(defonce oscillator-1 (create-oscillator "sine" 0.6))
(defonce oscillator-2 (create-oscillator "square" 0.6))
(defonce oscillator-3 (create-oscillator "square" 0.015))
(defonce vcs3-data (atom {:oscillator-1 {:frequency 0.6}
                          :oscillator-2 {:frequency 0.6}
                          :oscillator-3 {:frequency 0.015}
                          :matrix {:oscillator-1 {:output-1 false}
                                   :oscillator-2 {:output-1 false}
                                   :oscillator-3 {:output-1 false}}}))

(add-watch vcs3-data :oscillator-1-watcher
           (fn [_ _ old-state new-state]
             (when (not= (:oscillator-1 old-state) (:oscillator-1 new-state))
               (set! (.-value (.-frequency oscillator-1)) (:frequency (:oscillator-1 new-state))))
             (when (not= (:oscillator-1 (:matrix old-state)) (:oscillator-1 (:matrix new-state)))
               (if (:output-1 (:oscillator-1 (:matrix new-state)))
                 (.connect oscillator-1 (.-destination context))
                 (.disconnect oscillator-1)))))

(add-watch vcs3-data :oscillator-2-watcher
           (fn [_ _ old-state new-state]
             (when (not= (:oscillator-2 old-state) (:oscillator-2 new-state))
               (set! (.-value (.-frequency oscillator-2)) (:frequency (:oscillator-2 new-state))))
             (when (not= (:oscillator-2 (:matrix old-state)) (:oscillator-2 (:matrix new-state)))
               (if (:output-1 (:oscillator-2 (:matrix new-state)))
                 (.connect oscillator-2 (.-destination context))
                 (.disconnect oscillator-2)))))

(add-watch vcs3-data :oscillator-3-watcher
           (fn [_ _ old-state new-state]
             (when (not= (:oscillator-3 old-state) (:oscillator-3 new-state))
               (set! (.-value (.-frequency oscillator-3)) (:frequency (:oscillator-3 new-state))))
             (when (not= (:oscillator-3 (:matrix old-state)) (:oscillator-3 (:matrix new-state)))
               (if (:output-1 (:oscillator-3 (:matrix new-state)))
                 (.connect oscillator-3 (.-destination context))
                 (.disconnect oscillator-3)))))

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
    [:h3 "Matrix Board"]
    [:table
     [:tbody
      [:tr {:style {:writing-mode "vertical-lr"}}
       [:td]
       [:th "Output Ch. 1"]]
      [:tr
       [:th "Oscillator 1 (sine)"]
       [:td [:input {:type "checkbox" :checked (:output-1 (:oscillator-1 (:matrix @vcs3-data))) :on-change #(swap! vcs3-data assoc-in [:matrix :oscillator-1 :output-1] (not (:output-1 (:oscillator-1 (:matrix @vcs3-data)))))}]]]
      [:tr
       [:th "Oscillator 2 (square)"]
       [:td [:input {:type "checkbox" :checked (:output-1 (:oscillator-2 (:matrix @vcs3-data))) :on-change #(swap! vcs3-data assoc-in [:matrix :oscillator-2 :output-1] (not (:output-1 (:oscillator-2 (:matrix @vcs3-data)))))}]]]
      [:tr
       [:th "Oscillator 3 (square)"]
       [:td [:input {:type "checkbox" :checked (:output-1 (:oscillator-3 (:matrix @vcs3-data))) :on-change #(swap! vcs3-data assoc-in [:matrix :oscillator-3 :output-1] (not (:output-1 (:oscillator-3 (:matrix @vcs3-data)))))}]]]]]]
   [:div
    [:img {:src "/images/vcs3.jpg" :alt "VCS3"}]]])

(reagent/render-component [vcs3]
                          (. js/document (getElementById "app")))

(defn on-js-reload [])
