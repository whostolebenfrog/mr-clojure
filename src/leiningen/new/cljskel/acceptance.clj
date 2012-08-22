(ns {{name}}.acceptance
  (:use [{{name}}.web :as web])
  (:use midje.sweet)
  (:use clojure.test)
  (:use [clojure.data.json :as json])
  (:require [clj-http.client :as client])
  (:require [clojure.data.zip.xml :as xml])

  (:import java.util.UUID))

(def host (get (System/getenv) "HOST" "localhost"))
(def port (Integer. (get (System/getenv) "JETTY_PORT" 8080)))
(def base-url (System/getProperty "service.url" (str "http://localhost:" port "/1.x/")))

(defn url+ [& suffix] (apply str base-url suffix))

(defn new-user-id [] (.toString (UUID/randomUUID)))
(defn extract-body [response]
  (json/read-json (:body response)))

(defn read-xml-body [http-response]
  "Reads the XML body from the HTTP response as a tree structure"
  (-> (.getBytes (get http-response :body))
         java.io.ByteArrayInputStream.
         clojure.xml/parse clojure.zip/xml-zip))

(defn put [& urls]
  (last (doall (for [url urls] (client/put url {:throw-exceptions false})))))

(defn ping []
  (client/get (url+ "ping")  {:throw-exceptions false}))

(def started? (atom false))

(defn service-alive []
  (try
    (if-let [resp (ping)]
      true
      false)
    (catch Exception e false)))

(defn start-service []
  (if (not (service-alive))
    (do (prn (str "Starting Service - {{name}} Host: " host))
     (web/start)
     (reset! started? true))))

(defn stop-service []
  (if @started?
    (web/stop)))

(against-background
 [(before :contents (start-service) :after (stop-service))]

 (fact "Ping resource returns 200 HTTP response"
       (let [response (ping)]
         response => (contains {:status 200})))

 (fact "Status returns all required elements"
       (let [response (client/get (url+ "status") {:throw-exceptions false})
             body (read-xml-body response)]
         response => (contains {:status 200}))))
