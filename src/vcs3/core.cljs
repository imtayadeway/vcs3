(ns vcs3.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defonce app-state (atom {}))

(defn hello-world []
  [:div
   [:img {:src "/images/vcs3.jpg" :alt "VCS3"}]])

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

(defn on-js-reload [])
