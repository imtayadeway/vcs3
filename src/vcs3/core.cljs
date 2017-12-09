(ns vcs3.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defonce app-state (atom {}))

(defn oscillator []
  (let [context (new js/window.AudioContext)
        oscillator (.createOscillator context)]
    (set! (.-type oscillator) "square")
    (set! (.-value (.-frequency oscillator)) 440)
    (.connect oscillator (.-destination context))
    (.start oscillator)))

(defn hello-world []
  [:div {:on-click #(oscillator)}
   [:img {:src "/images/vcs3.jpg" :alt "VCS3"}]])

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

(defn on-js-reload [])
