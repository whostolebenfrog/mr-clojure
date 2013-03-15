(ns {{name}}.acceptance
  (:require [{{name}}.web :as web])

  (:require [clj-http.client :as client]
            [midje.sweet :refer :all]
            [clojure.test :refer :all]
            [clojure.data.json :as json]
            [clojure.data.zip.xml :as xml]
            [rest-cljer.core :refer [rest-driven]]
            [environ.core :refer [env]])

  (:import [java.util UUID]
           [com.github.restdriver.clientdriver ClientDriverFactory ClientDriverRule]
           [com.github.restdriver.clientdriver RestClientDriver ClientDriverRequest$Method]
           [java.util.regex Pattern]))

(defn url+ [& suffix] (apply str
                             (format (env :service-url) (env :service-port))
                             suffix))

(defn read-xml-body [http-response]
  "Reads the XML body from the HTTP response as a tree structure"
  (-> (.getBytes (get http-response :body))
         java.io.ByteArrayInputStream.
         clojure.xml/parse clojure.zip/xml-zip))



(deftest ^:acceptance tests
         (fact "Ping resource returns 200 HTTP response"
               (let [response (client/get (url+ "/ping")  {:throw-exceptions false})]
                 response => (contains {:status 200})))

         (fact "Status returns all required elements"
               (let [response (client/get (url+ "/status") {:throw-exceptions false})
                     body (read-xml-body response)]
                 response => (contains {:status 200})))



         )
