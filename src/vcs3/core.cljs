(ns vcs3.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defonce vcs3-data (atom {:oscillator-1 {:frequency {:value 1} :level-1 0 :level-2 0 :min 1 :max 10000}
                          :oscillator-2 {:frequency {:value 1} :level-1 0 :level-2 0 :min 1 :max 10000}
                          :oscillator-3 {:frequency {:value 0.025} :level-1 0 :level-2 0 :min 0.025 :max 500}
                          :matrix {:oscillator-1 {:shape-1 {:output-1 false} :shape-2 {:output-1 false}}
                                   :oscillator-2 {:shape-1 {:output-1 false} :shape-2 {:output-1 false}}
                                   :oscillator-3 {:shape-1 {:output-1 false} :shape-2 {:output-1 false}}}}))

(defonce context (new js/window.AudioContext))

(defn create-oscillator [shape-1 shape-2 frequency level-1 level-2]
  (let [output-1 (.createOscillator context)
        output-2 (.createOscillator context)]
    (set! (.-type output-1) shape-1)
    (set! (.-type output-2) shape-2)
    (set! (.-value (.-frequency output-1)) frequency)
    (set! (.-value (.-frequency output-2)) frequency)
    (.start output-1)
    (.start output-2)
    (.connect output-1 level-1)
    (.connect output-2 level-2)
    {:output-1 output-1 :output-2 output-2}))

(defn create-gain [value]
  (let [gain (.createGain context)]
    (set! (.-value (.-gain gain)) value)
    gain))

(defonce oscillator-1-level-1 (create-gain (get-in @vcs3-data [:oscillator-1 :level-1])))
(defonce oscillator-1-level-2 (create-gain (get-in @vcs3-data [:oscillator-1 :level-2])))
(defonce oscillator-1
  (let [frequency (get-in @vcs3-data [:oscillator-1 :frequency :value])]
    (create-oscillator "sine" "sawtooth" frequency oscillator-1-level-1 oscillator-1-level-2)))
(defonce oscillator-2-level-1 (create-gain (get-in @vcs3-data [:oscillator-2 :level-1])))
(defonce oscillator-2-level-2 (create-gain (get-in @vcs3-data [:oscillator-2 :level-2])))
(defonce oscillator-2
  (let [frequency (get-in @vcs3-data [:oscillator-2 :frequency :value])]
    (create-oscillator "square" "triangle" frequency oscillator-2-level-1 oscillator-2-level-2)))
(defonce oscillator-3-level-1 (create-gain (get-in @vcs3-data [:oscillator-3 :level-1])))
(defonce oscillator-3-level-2 (create-gain (get-in @vcs3-data [:oscillator-3 :level-2])))
(defonce oscillator-3
  (let [frequency (get-in @vcs3-data [:oscillator-3 :frequency :value])]
    (create-oscillator "square" "triangle" frequency oscillator-3-level-1 oscillator-3-level-2)))

(defn oscillator-watcher-fn
  [key oscillator level-1 level-2]
  (fn [_ _ old-state new-state]
    (let [changed (fn [& args] (apply not= (map #(get-in % args) [old-state new-state])))]
      (when (changed key :frequency :value)
        (set! (.-value (.-frequency (:output-1 oscillator))) (get-in new-state [key :frequency :value]))
        (set! (.-value (.-frequency (:output-2 oscillator))) (get-in new-state [key :frequency :value])))
      (when (changed :matrix key :shape-1 :output-1)
        (if (get-in new-state [:matrix key :shape-1 :output-1])
          (.connect level-1 (.-destination context))
          (.disconnect level-1 (.-destination context))))
      (when (changed :matrix key :shape-2 :output-1)
        (if (get-in new-state [:matrix key :shape-2 :output-1])
          (.connect level-2 (.-destination context))
          (.disconnect level-2 (.-destination context))))
      (when (changed key :level-1)
        (set! (.-value (.-gain level-1)) (get-in new-state [key :level-1])))
      (when (changed key :level-2)
        (set! (.-value (.-gain level-2)) (get-in new-state [key :level-2]))))))

(add-watch vcs3-data :oscillator-1-watcher (oscillator-watcher-fn :oscillator-1 oscillator-1 oscillator-1-level-1 oscillator-1-level-2))
(add-watch vcs3-data :oscillator-2-watcher (oscillator-watcher-fn :oscillator-2 oscillator-2 oscillator-2-level-1 oscillator-2-level-2))
(add-watch vcs3-data :oscillator-3-watcher (oscillator-watcher-fn :oscillator-3 oscillator-3 oscillator-3-level-1 oscillator-3-level-2))

(defn frequency [oscillator]
  [:div
   [:h6 "Frequency"]
   [:input {:type "range"
            :value (get-in @vcs3-data [oscillator :frequency :value])
            :min (get-in @vcs3-data [oscillator :min])
            :max (get-in @vcs3-data [oscillator :max])
            :style {:width "100%"}
            :on-change (fn [e] (swap! vcs3-data assoc-in [oscillator :frequency :value] (.. e -target -value)))}]])

(defn patch [from to]
  (let [checked (to (get-in (:matrix @vcs3-data) from))]
    [:input {:type "checkbox" :checked checked
             :on-change #(swap! vcs3-data assoc-in [:matrix (first from) (second from) to] (not checked))}]))

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
    [level :oscillator-1 :level-1]
    [:h6 "Level (ramp)"]
    [level :oscillator-1 :level-2]]
   [:div
    [:h3 "Oscillator 2"]
    [frequency :oscillator-2]
    [:h6 "Level (square)"]
    [level :oscillator-2 :level-1]
    [:h6 "Level (triangle)"]
    [level :oscillator-2 :level-2]]
   [:div
    [:h3 "Oscillator 3"]
    [frequency :oscillator-3]
    [:h6 "Level (square)"]
    [level :oscillator-3 :level-1]
    [:h6 "Level (triangle)"]
    [level :oscillator-3 :level-2]]
   [:div
    [:h3 "Matrix Board"]
    [:table
     [:tbody
      [:tr.matrix-outputs
       [:td]
       [:th "Output Ch. 1"]]
      [:tr
       [:th "Oscillator 1 (sine)"]
       [:td [patch [:oscillator-1 :shape-1] :output-1]]]
      [:tr
       [:th "Oscillator 1 (ramp)"]
       [:td [patch [:oscillator-1 :shape-2] :output-1]]]
      [:tr
       [:th "Oscillator 2 (square)"]
       [:td [patch [:oscillator-2 :shape-1] :output-1]]]
      [:tr
       [:th "Oscillator 2 (triangle)"]
       [:td [patch [:oscillator-2 :shape-2] :output-1]]]
      [:tr
       [:th "Oscillator 3 (square)"]
       [:td [patch [:oscillator-3 :shape-1] :output-1]]]
      [:tr
       [:th "Oscillator 3 (triangle)"]
       [:td [patch [:oscillator-3 :shape-2] :output-1]]]]]]
   [:div
    [:img {:src "/images/vcs3.jpg" :alt "VCS3"}]]])

(reagent/render-component [vcs3]
                          (. js/document (getElementById "app")))

(defn on-js-reload [])
