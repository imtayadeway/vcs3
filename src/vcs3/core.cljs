(ns vcs3.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defonce vcs3-data (atom {:oscillator-1 {:frequency 1 :level-1 0 :min 1 :max 10000}
                          :oscillator-2 {:frequency 1 :level-1 0 :min 1 :max 10000}
                          :oscillator-3 {:frequency 0.025 :level-1 0 :min 0.025 :max 500}
                          :matrix {:oscillator-1 {:output-1 false}
                                   :oscillator-2 {:output-1 false}
                                   :oscillator-3 {:output-1 false}}}))

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

(defonce oscillator-1-level-1 (create-gain (get-in @vcs3-data [:oscillator-1 :level-1])))
(defonce oscillator-1 (create-oscillator "sine" (get-in @vcs3-data [:oscillator-1 :frequency]) oscillator-1-level-1))
(defonce oscillator-2-level-1 (create-gain (get-in @vcs3-data [:oscillator-2 :level-1])))
(defonce oscillator-2 (create-oscillator "square" (get-in @vcs3-data [:oscillator-2 :frequency]) oscillator-2-level-1))
(defonce oscillator-3-level-1 (create-gain (get-in @vcs3-data [:oscillator-3 :level-1])))
(defonce oscillator-3 (create-oscillator "square" (get-in @vcs3-data [:oscillator-3 :frequency]) oscillator-3-level-1))

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

(defn frequency [oscillator]
  [:div
   [:h6 "Frequency"]
   [:input {:type "range"
            :value (get-in @vcs3-data [oscillator :frequency])
            :min (get-in @vcs3-data [oscillator :min])
            :max (get-in @vcs3-data [oscillator :max])
            :style {:width "100%"}
            :on-change (fn [e] (swap! vcs3-data assoc-in [oscillator :frequency] (.. e -target -value)))}]])

(defn patch [from to]
  (let [checked (to (from (:matrix @vcs3-data)))]
    [:input {:type "checkbox" :checked checked
             :on-change #(swap! vcs3-data assoc-in [:matrix from to] (not checked))}]))

(defn level [oscillator output]
  [:div
   [:input {:type "range"
            :value (get-in @vcs3-data [oscillator output])
            :min 0
            :max 10
            :step 0.1
            :on-change (fn [e] (swap! vcs3-data assoc-in [oscillator output] (.. e -target -value)))}]])

(defn vcs3 []
  [:div
   [:div
    [:h3 "Oscillator 1"]
    [frequency :oscillator-1]
    [:h6 "Level (sine)"]
    [level :oscillator-1 :level-1]]
   [:div
    [:h3 "Oscillator 2"]
    [frequency :oscillator-2]
    [:h6 "Level (square)"]
    [level :oscillator-2 :level-1]]
   [:div
    [:h3 "Oscillator 3"]
    [frequency :oscillator-3]
    [:h6 "Level (square)"]
    [level :oscillator-3 :level-1]]
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
