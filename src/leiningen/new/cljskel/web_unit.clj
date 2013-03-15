(ns {{name}}.unit.web
  (:require [cheshire.core :as json])
  (:use [{{name}}.web]
        [midje.sweet]))

(defn request
  [method resource & {:as others}]
  (routes (merge {:request-method method
                  :uri resource} (update-in others [:body]
                                         #(java.io.ByteArrayInputStream.
                                           (.getBytes (json/generate-string %)))))))

(defn request [method resource]
  (routes {:request-method method
           :uri resource } ))

(fact-group :unit
  (fact "Ping returns a pong"
        (:body (request :get "/1.x/ping"))  => "pong" )
)
