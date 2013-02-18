(ns {{name}}.web
    (:require [{{name}}.setup :as setup]
              )
    (:require [compojure.core :refer [defroutes GET PUT POST DELETE]]
              [compojure.route :as route]
              [compojure.handler :as handler]
              [nokia.adapter.instrumented-jetty :as instrumented]
              [ring.middleware.format-response :refer [wrap-restful-response]]
              [ring.middleware.params :refer [wrap-params]]
              [ring.middleware.keyword-params :refer [wrap-keyword-params]]
              [clojure.data.xml :refer [element emit-str]]
              [clojure.string :refer [split]]
              [clojure.tools.logging :refer [info warn error]]
              [environ.core :refer [env]]
              [nokia.ring-utils.error :as error-utils]
              [nokia.ring-utils.metrics :as metrics-utils])
  (:gen-class))


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
                                :version @setup/version
                                :success true}))})

(defroutes routes

  (GET "/1.x/ping"
      [] "pong")

  (GET "/1.x/status"
      [] (status))

  (route/not-found (error-response "Page not found")))


(def app
  (-> (error-utils/error-handling-middleware routes)
      wrap-keyword-params
      wrap-params
      wrap-restful-response
      metrics-utils/per-resource-metrics-middleware))

(def server (atom nil))

(defn start-server []
  (instrumented/run-jetty #'app {:port (Integer. (env :service-port))
                                 :join? false
                                 :stacktraces? (not (boolean (Boolean. (env :service-production))))
                                 :auto-reload? (not (boolean (Boolean. (env :service-production))))}))

(defn start []
  (do
    (setup/setup)
    (reset! server (start-server))))

(defn stop [] (if-let [server @server] (.stop server)))

(defn -main [& args] (start))
