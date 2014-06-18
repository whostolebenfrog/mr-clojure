(ns {{name}}.unit.web
    (:require [{{name}}.web]
              [cheshire.core :as json]
              [midje.sweet]))

(defn app [method resource]
  (routes {:request-method method
           :uri resource } ))

(fact-group
 :unit

 (fact "Ping returns a pong"
       (:body (request :get "/ping"))  => "pong" ))
