(ns {{name}}.web
  (:require [{{name}}.setup :as setup]
        [{{name}}.core :as core]
        [{{name}}.persistence :as persist])
  (:require [compojure.core]
        [compojure.route :as route]
        [ring.adapter.jetty :as jetty :only [run-jetty]]
        [ring.middleware.format-params :only [wrap-restful-params]]
        [ring.middleware.format-response :only [wrap-restful-response]]
        [ring.middleware.params :only [wrap-params]]
        [clojure.data.xml :only [element emit-str]]
        [clojure.string :only (split)]
        [clojure.data.json :as json :only [json-str]]
        [clojure.tools.logging :only (info warn error)])
  (:import java.io.StringWriter)
  (:import java.io.PrintWriter)
  (:gen-class))

(defn response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body data})

(defn error-response  [msg & [status]]
  (let [s (or status 404)]
    (response {:message msg :status s} s)))

(defn excep-response
  [^Exception e]
  (let [sw (new StringWriter)]
    (.printStackTrace e (new PrintWriter sw))
    (error-response (.toString sw) 500)))

(def default-paging-params {:start-index "0"
                            :items-per-page "10"})

(defn paging [start-index items-per-page]
  {:start-index (Integer/parseInt
                 (or start-index (:start-index default-paging-params)))
   :items-per-page (Integer/parseInt
                    (or items-per-page (:items-per-page default-paging-params)))})

(defroutes handler
  (GET "/1.x/ping" []
       (let [status 200]
         {:status status
          :headers {"Content-Type" "text/plain"}
          :body "pong"}))

  (GET "/1.x/status" []
       (let [status 200, mongo-check (setup/check-mongo-connectivity)]
         {:status status
          :headers {"Content-Type" "application/xml"}
          :body
          (emit-str
           (element :status {:serviceName "{{name}}":version (setup/version) :success mongo-check}
                    (element :statusItem {:success mongo-check :name "MongoHealthCheck"})))}))

  (route/not-found (error-response "Page not found")))

(defn error-handling [handler]
  (fn [request]
    (try
      (handler request)
      (catch Throwable e
        (error e)
        (excep-response e)))))

(def app
  (->  (error-handling handler) wrap-restful-params wrap-params wrap-restful-response) )

(def server (atom nil))

(defn start-server []
  (jetty/run-jetty #'app {:port @setup/service-port :join? false}))


(defn start []
  (do
    (setup/setup)
    (reset! server (start-server))))

(defn stop [] (if-let [server @server] (.stop server)))

(defn -main [& args] (start))
