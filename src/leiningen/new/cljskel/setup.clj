(ns {{name}}.setup
    (:require [{{name}}.web :as web]
              [environ.core :refer [env]]
              [clojure.string :as cs :only (split)]
              [clojure.tools.logging :refer (info warn error)]
              [clojure.java.io :as io]
              [nokia.adapter.instrumented-jetty :as instrumented])
    (:import (java.lang Integer Throwable)
             (java.util.logging LogManager)
             (com.yammer.metrics Metrics)
             (com.yammer.metrics.core MetricName)
             (com.ovi.common.metrics.graphite GraphiteReporterFactory GraphiteName ReporterState)
             (com.ovi.common.metrics HostnameFactory)
             (org.slf4j.bridge SLF4JBridgeHandler)
             (java.util.concurrent TimeUnit))
    (:gen-class))

(defn read-file-to-properties [file-name]
  (with-open [^java.io.Reader reader (io/reader file-name)]
    (let [props (java.util.Properties.)]
      (.load props reader)
      (into {} (for [[k v] props] [k v])))))

(defn configure-logging []
  (.reset (LogManager/getLogManager))
  ;Route all java.util.logging log statements to slf4j
  (SLF4JBridgeHandler/install))

(defn start-graphite-reporting []
  (let [graphite-prefix (new GraphiteName
                             (into-array Object
                                         [(env :environment-name)
                                          (env :service-name)
                                          (HostnameFactory/getHostname)]))]
    (GraphiteReporterFactory/create
     (env :environment-entertainment-graphite-host)
     (Integer/parseInt (env :environment-entertainment-graphite-port))
     graphite-prefix
     (Integer/parseInt (env :service-graphite-post-interval))
     (TimeUnit/valueOf (env :service-graphite-post-unit))
     (ReporterState/valueOf (env :service-graphite-enabled)))))

(def version
  (delay (if-let [path (.getResource (ClassLoader/getSystemClassLoader) "META-INF/maven/{{name}}/{{name}}/pom.properties")]
           ((read-file-to-properties path) "version")
           "localhost")))

(defn setup []
  (web/set-version! @version)
  (configure-logging)
  (start-graphite-reporting))

(def server (atom nil))

(defn start-server []
  (instrumented/run-jetty #'web/app {:port (Integer. (env :service-port))
                                     :max-threads (Integer. (env :service-jetty-threads "254"))
                                     :join? false
                                     :stacktraces? (not (Boolean/valueOf (env :service-production)))
                                     :auto-reload? (not (Boolean/valueOf (env :service-production)))}))

(defn start []
  (do
    (setup)
    (reset! server (start-server))))

(defn stop [] (if-let [server @server] (.stop server)))

(defn -main [& args]
  (start))
