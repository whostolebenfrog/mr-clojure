(ns {{name}}.unit.web
  (:require [{{name}}.web :refer :all]
            [cheshire.core :as json]
            [midje.sweet :refer :all])
  (:import [java.io ByteArrayInputStream InputStream]))

(defn request
  "Creates a compojure request map and applies it to our routes.
   Accepets method, resource and optionally an extended map"
  [method resource & [{:keys [params body content-type]
                       :or {:params {}}}]]
  (let [{:keys [body] :as res}
        (app (merge {:request-method method
                     :uri resource
                     :params params}
                    (when body {:body (-> body json/generate-string .getBytes ByteArrayInputStream.)})
                    (when content-type {:content-type content-type})))]
    (cond-> res
            (instance? InputStream body)
            (assoc :body (json/parse-string (slurp body) true)))))

(fact-group
 :unit

 (fact "Ping returns a pong"
       (:body (request :get "/ping"))  => "pong"))
