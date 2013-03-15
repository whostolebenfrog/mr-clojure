(ns {{name}}.acceptance
  (:require [{{name}}.web :as web]
            [clj-http.client :as client]
            [midje.sweet :refer :all]
            [cheshire.core :as json]
            [clojure.data.zip.xml :as xml]
            [environ.core :refer [env]])
  (:import [java.util UUID]))

(defn url+ [& suffix] (apply str
                             (format (env :service-url) (env :service-port))
                             suffix))

(defn content-type
  [response]
  (if-let [ct ((:headers response) "content-type")]
    (first (clojure.string/split ct #";"))
    :none))

(defmulti read-body content-type)

(defmethod read-body "application/xml" [http-response]
  (-> http-response
      :body
      .getBytes
      java.io.ByteArrayInputStream.
      clojure.xml/parse clojure.zip/xml-zip))

(defmethod read-body "application/json" [http-response]
  (json/parse-string (:body http-response) true))


(fact-group :acceptance
   (fact "Ping resource returns 200 HTTP response"
         (let [response (client/get (url+ "/ping")  {:throw-exceptions false})]
           response => (contains {:status 200})))

   (fact "Status returns all required elements"
         (let [response (client/get (url+ "/status") {:throw-exceptions false})
               body (read-body response)]
           response => (contains {:status 200})))



         )
