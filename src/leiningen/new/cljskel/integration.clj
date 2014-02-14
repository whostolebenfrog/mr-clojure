(ns {{name}}.integration
  (:require [{{name}}.test-common :refer :all])
  (:require [clj-http.client :as client]
            [midje.sweet :refer :all]
            [environ.core :refer [env]])
  (:import [java.util UUID]))

(fact-group
 :integration

   (fact "Ping resource returns 200 HTTP response"
         (let [response (client/get (url+ "/ping")  {:throw-exceptions false})]
           response => (contains {:status 200})))

 (fact "Healthcheck resource returns 200 HTTP response"
       (let [response (client/get (url+ "/healthcheck") {:throw-exceptions false})]
         response => (contains {:status 200}))))
