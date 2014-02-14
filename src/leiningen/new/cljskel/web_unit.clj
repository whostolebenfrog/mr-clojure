(ns {{name}}.unit.web
  (:require [cheshire.core :as json])
  (:use [{{name}}.web]
        [midje.sweet]))

(defn request [method resource]
  (routes {:request-method method
           :uri resource } ))

(fact-group
 :unit

  (fact "Ping returns a pong"
        (:body (request :get "/ping"))  => "pong" ))
