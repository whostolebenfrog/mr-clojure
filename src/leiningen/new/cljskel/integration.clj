(ns {{name}}.integration
    (:require [{{name}}.test-common :refer :all]
              [clj-http.client :as http]
              [environ.core :refer [env]]
              [midje.sweet :refer :all]))

(fact-group
 :integration

 (fact "Ping resource returns 200 HTTP response"
       (let [response (http/get (url+ "/ping")  {:throw-exceptions false})]
         response => (contains {:status 200})))

 (fact "Healthcheck resource returns 200 HTTP response"
       (let [response (http/get (url+ "/healthcheck") {:throw-exceptions false})]
         response => (contains {:status 200}))))
