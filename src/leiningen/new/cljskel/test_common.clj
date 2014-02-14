(ns {{name}}.test-common
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [environ.core :refer [env]]))

(defn url+ [& suffix]
  (apply str (format (env :service-url) (env :service-port)) suffix))

(defn content-type
  [response]
  (if-let [ct ((:headers response) "content-type")]
    (first (str/split ct #";"))
    :none))

(defmulti read-body content-type)

(defmethod read-body "application/xml" [http-response]
  (-> http-response :body .getBytes java.io.ByteArrayInputStream. xml/parse zip/xml-zip))

(defmethod read-body "application/json" [http-response]
  (json/parse-string (:body http-response) true))

(defmethod read-body :none [http-response]
  (throw (Exception. (str "No content-type in response: " http-response))))
