(ns {{name}}.page-frame
  (:require [hiccup
             [core :refer [html]]
             [page :refer [include-css include-js]]]))

(defn page-frame
  [dev-mode?]
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     [:link {:rel "icon"
             :type "image/png"
             :href "img/favicon.ico"}]
     (include-css (if dev-mode? "css/site.css" "css/site.min.css"))]
    [:body
     [:div#app
      [:p "Loading... "]]
     (include-js "js/app.js")]]))
