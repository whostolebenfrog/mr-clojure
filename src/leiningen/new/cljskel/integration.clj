(ns {{name}}.integration
  (:require [{{name}}.test-common :refer :all])
  (:require [clj-http.client :as http]
            [midje.sweet :refer :all]
            [environ.core :refer [env]])
  (:import [java.util UUID]))

(fact-group
 :integration

   (fact "Ping resource returns 200 HTTP response"
         (let [response (http/get (url+ "/ping")  {:throw-exceptions false})]
           response => (contains {:status 200})))

 (fact "Healthcheck resource returns 200 HTTP response"
       (let [response (http/get (url+ "/healthcheck") {:throw-exceptions false})]
         response => (contains {:status 200}))))
