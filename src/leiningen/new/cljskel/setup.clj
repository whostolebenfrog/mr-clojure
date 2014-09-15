(ns {{name}}.setup
  (:require [{{name}}.web :as web]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [nokia.adapter.instrumented-jetty :as instrumented])
  (:import [com.ovi.common.metrics.graphite GraphiteReporterFactory GraphiteName ReporterState]
           [com.ovi.common.metrics HostnameFactory]
           [java.util.concurrent TimeUnit]
           [java.util.logging LogManager]
           [org.slf4j.bridge SLF4JBridgeHandler])
  (:gen-class))

(defn read-file-to-properties [file-name]
  (with-open [^java.io.Reader reader (io/reader file-name)]
    (let [props (java.util.Properties.)]
      (.load props reader)
      (into {} (for [[k v] props] [k v])))))

(defn configure-logging []
  (.reset (LogManager/getLogManager))
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
  (delay (if-let [path (.getResource (ClassLoader/getSystemClassLoader)
                                     "META-INF/maven/{{name}}/{{name}}/pom.properties")]
           ((read-file-to-properties path) "version")
           "localhost")))

(defn setup []
  (web/set-version! @version)
  (configure-logging)
  (start-graphite-reporting))

(def server (atom nil))

(defn configure-server [server]
  (doto server
    (.setStopAtShutdown true)
    (.setGracefulShutdown (Integer/valueOf (env :service-jetty-gracefulshutdown-millis 5000)))))

(defn start-server []
  (instrumented/run-jetty #'web/app {:port (Integer. (env :service-port))
                                     :max-threads (Integer. (env :service-jetty-threads "254"))
                                     :join? false
                                     :stacktraces? (not (Boolean/valueOf (env :service-production)))
                                     :auto-reload? (not (Boolean/valueOf (env :service-production)))
                                     :configurator configure-server}))

(defn start []
  (setup)
  (reset! server (start-server)))

(defn -main [& args]
  (start))
