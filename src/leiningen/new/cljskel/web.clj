(ns {{name}}.web
    (:require [compojure.core :refer [defroutes context GET PUT POST DELETE]]
              [compojure.route :as route]
              [compojure.handler :as handler]
              [ring.middleware.format-response :refer [wrap-restful-response]]
              [ring.middleware.params :refer [wrap-params]]
              [ring.middleware.keyword-params :refer [wrap-keyword-params]]
              [clojure.data.xml :refer [element emit-str]]
              [clojure.string :refer [split]]
              [clojure.tools.logging :refer [info warn error]]
              [environ.core :refer [env]]
              [nokia.ring-utils.error :as error-utils]
              [nokia.ring-utils.metrics :as metrics-utils]))

(def ^:dynamic *version* "none")
(defn set-version! [version]
  (alter-var-root #'*version* (fn [_] version)))

(defn response [data content-type & [status]]
  {:status (or status 200)
   :headers {"Content-Type" content-type}
   :body data})

(defn error-response  [msg & [status]]
  (let [s (or status 404)]
    (response {:message msg :status s} "application/json" s)))

(defn status
  []
  {:headers {"Content-Type" "application/xml"}
   :body    (emit-str (element :status
                               {:serviceName "{{name}}"
                                :version *version*
                                :success true}))})

(defroutes routes
  (context
   "/1.x" []

   (GET "/ping"
        [] "pong")

   (GET "/status"
        [] (status)))

  (route/not-found (error-response "Page not found")))


(def app
  (-> (error-utils/error-handling-middleware routes)
      wrap-keyword-params
      wrap-params
      wrap-restful-response
      metrics-utils/per-resource-metrics-middleware))
