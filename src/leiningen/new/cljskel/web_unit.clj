(ns {{name}}.unit.web
    (:require [{{name}}.web :refer :all]
              [cheshire.core :as json]
              [midje.sweet :refer :all]))

(defn request [method resource]
  (app {:request-method method
        :uri resource}))

(fact-group
 :unit

 (fact "Ping returns a pong"
       (:body (request :get "/ping"))  => "pong"))
