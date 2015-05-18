(ns {{name}}.setup
    (:require [{{name}}.web :as web]
              [clojure.string :as str]
              [environ.core :refer [env]]
              [metrics.core :refer [default-registry]]
              [mixradio.instrumented-jetty :refer [run-jetty]]
              [radix.setup :as setup])
    (:import [ch.qos.logback.classic Logger]
             [com.codahale.metrics.logback InstrumentedAppender]
             [mixradio.metrics GraphiteReporterFilter]
             [org.slf4j LoggerFactory])
    (:gen-class))

(defonce server
  (atom nil))

(defn configure-server
  [server]
  (doto server
    (.setStopAtShutdown true)
    (.setStopTimeout setup/shutdown-timeout)))

(defn start-server
  []
  (run-jetty #'web/app {:port setup/service-port
                        :max-threads setup/threads
                        :join? false
                        :stacktraces? (not setup/production?)
                        :auto-reload? (not setup/production?)
                        :configurator configure-server
                        :send-server-version false}))

(defn- configure-graphite-appender
  []
  (let [factory (LoggerFactory/getILoggerFactory)
        root (.getLogger factory Logger/ROOT_LOGGER_NAME)
        appender (InstrumentedAppender. default-registry)]

    (.setContext appender (.getLoggerContext root))
    (.addFilter appender (GraphiteReporterFilter.))
    (.start appender)
    (.addAppender root appender)))

(defn start
  []
  (setup/configure-logging)
  (configure-graphite-appender)
  (setup/start-graphite-reporting {:graphite-prefix (str/join "." [(env :environment-name) (env :service-name) (env :box-id setup/hostname)])})
  (reset! server (start-server)))

(defn stop
  []
  (when-let [s @server]
    (.stop s)
    (reset! server nil))
  (shutdown-agents))

(defn -main
  [& args]
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop))
  (start))
