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

(defn vcs3 []
  [:div {:on-click #(oscillator)}
   [:img {:src "/images/vcs3.jpg" :alt "VCS3"}]])

(reagent/render-component [vcs3]
                          (. js/document (getElementById "app")))

(defn on-js-reload [])
