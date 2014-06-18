(ns {{name}}.unit.web
  (:require [{{name}}.web :refer :all]
            [cheshire.core :as json]
            [midje.sweet :refer :all])
  (:import [java.io ByteArrayInputStream InputStream]))

(defn- json-response?
  [res]
  (when-let [content-type (get-in res [:headers "Content-Type"])]
    (re-find #"^application/(..+)?json.+" content-type)))

(defn request
  "Creates a compojure request map and applies it to our routes.
   Accepets method, resource and optionally an extended map"
  [method resource & [{:keys [params body content-type headers]
                       :or {:params {}
                            :headers {}}}]]
  (let [{:keys [body] :as res}
        (app (merge {:request-method method
                     :uri resource
                     :params params
                     :headers headers}
                    (when body {:body (-> body json/generate-string .getBytes ByteArrayInputStream.)})
                    (when content-type {:content-type content-type})))]
    (cond-> res
            (instance? InputStream body)
            (update-in [:body] slurp)

            (json-response? res)
            (update-in [:body] #(json/parse-string % true)))))

(fact-group
 :unit

 (fact "Ping returns a pong"
       (:body (request :get "/ping"))  => "pong")

 (fact "Healthcheck returns OK"
       (let [resp (request :get "/healthcheck")]
         (:status resp) => 200
         (get-in resp [:body :name]) => "{{name}}")))
