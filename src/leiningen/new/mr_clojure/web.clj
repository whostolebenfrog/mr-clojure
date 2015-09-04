(ns {{name}}.web
  (:require [compojure
             [core :refer [defroutes GET]]
             [route :as route]]
            [environ.core :refer [env]]
            [metrics.ring
             [expose :refer [expose-metrics-as-json]]
             [instrument :refer [instrument]]]
            {{#cljs-app?}}
            [{{name}}.page-frame :refer [page-frame]]
            [prone.middleware :refer [wrap-exceptions]]
            {{/cljs-app?}}
            [radix
             [error :refer [error-response wrap-error-handling]]
             [ignore-trailing-slash :refer [wrap-ignore-trailing-slash]]
             [reload :refer [wrap-reload]]
             [setup :as setup]]
            [ring.middleware
             [format-params :refer [wrap-json-kw-params]]
             [json :refer [wrap-json-response]]
             [params :refer [wrap-params]]]))

(def version
  (setup/version "{{name}}"))

{{#cljs-app?}}
(def dev-mode?
  (boolean (env :dev-mode false)))
{{/cljs-app?}}

(defn healthcheck
  []
  (let [body {:name "{{name}}"
              :version version
              :success true
              :dependencies []}]
    {:headers {"content-type" "application/json"}
     :status (if (:success body) 200 500)
     :body body}))

(defroutes routes

  (GET "/healthcheck"
       [] (healthcheck))

  (GET "/ping"
       [] "pong")

  {{#cljs-app?}}
  (GET "/"
       [] (page-frame dev-mode?))

  (route/resources "/")
  {{/cljs-app?}}
  
  (route/not-found (error-response "Resource not found" 404)))

(def app
  (-> routes
      {{#cljs-app?}}
      (cond-> dev-mode? wrap-exceptions)
      {{/cljs-app?}}
      (wrap-reload)
      (instrument)
      (wrap-error-handling)
      (wrap-ignore-trailing-slash)
      (wrap-json-response)
      (wrap-json-kw-params)
      (wrap-params)
      (expose-metrics-as-json)))
