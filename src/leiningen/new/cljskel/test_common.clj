(ns {{name}}.test-common
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [environ.core :refer [env]]))

(defn url+ [& suffix]
  (apply str (format (env :service-url) (env :service-port)) suffix))

(defn json-body
  "Reads the body of the request as json and parses it into a map with keywords.

   Fails pre-condition if content type is not application/json."
  [resp]
  {:pre [(re-matches #"application/(.+)?json.+" (get-in resp [:headers "content-type"]))]}
  (json/parse-string (:body resp) true))
