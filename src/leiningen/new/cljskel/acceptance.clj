(ns {{name}}.acceptance
    (:require [{{name}}.test-common :refer :all]
              [clj-http.client :as http]
              [environ.core :refer [env]]
              [midje.sweet :refer :all]))

(fact-group
 :acceptance

 (fact "Ping resource returns 200 HTTP response"
       (let [response (http/get (url+ "/ping")  {:throw-exceptions false})]
         response => (contains {:status 200})))

 (fact "Healthcheck resource returns 200 HTTP response"
       (let [response (http/get (url+ "/healthcheck") {:throw-exceptions false})
             body (read-body response)]
         response => (contains {:status 200})
         body => (contains {:name "{{name}}"
                            :success true
                            :version truthy}))))
