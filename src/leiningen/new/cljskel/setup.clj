(ns {{name}}.setup
  ;;Monger
  (:require [monger.core :as mc :only (mongo-options set-db! get-db server-address)])
  (:require [monger.collection :as mco :only (insert)])
  (:require [monger.command :as cmd])
  (:import [com.mongodb MongoOptions ServerAddress WriteConcern MongoException])

  ;;Graphite
  (:import java.lang.Integer)
  (:import java.lang.Throwable)
  (:import java.util.logging.LogManager)
  (:import com.yammer.metrics.Metrics)
  (:import com.yammer.metrics.core.MetricName)
  (:import com.ovi.common.metrics.graphite.GraphiteReporterFactory)
  (:import com.ovi.common.metrics.graphite.GraphiteName)
  (:import com.ovi.common.metrics.graphite.ReporterState)
  (:import com.ovi.common.metrics.HostnameFactory)
  (:import org.slf4j.bridge.SLF4JBridgeHandler)
  (:import java.util.concurrent.TimeUnit)


  ;;Utils
  (:use [clojure.string :as cs :only (split)])
  (:use [clojure.tools.logging :only (info warn error)])
  (:use [clojure.java.io :as io]))

(def default-props
  {"host.name" (HostnameFactory/getHostname)
   "service.port" "8080"})

(defn read-file-to-properties [file-name]
  (with-open [^java.io.Reader reader (io/reader file-name)]
        (let [props (java.util.Properties.)]
          (.load props reader)
          (into {} (for [[k v] props] [k v])))))

(defn load-props
  ([] (load-props (System/getProperty "config")))
  ([file-name]
     (merge
      default-props
      (read-file-to-properties file-name)
      (System/getProperties))))


(defn write-concern [w timeout]
  (WriteConcern. (Integer/parseInt w) (Integer/parseInt timeout)))

(def desired-concern (atom nil))
(def required-concern (atom nil))

(defn build-server-addresses [comma-sep-hosts]
  (map (fn [[h p]] (mc/server-address h (Integer/parseInt p)))
       (map #(cs/split % #":") (cs/split comma-sep-hosts #","))))

(defn configure-logging []
  (.reset (LogManager/getLogManager))
  ;Route all java.util.logging log statements to slf4j, ie mongo client logs
  (SLF4JBridgeHandler/install))

(defn start-graphite-reporting [props]
  (let [graphite-prefix (new GraphiteName
                             (into-array Object
                                         [(props "environment.name")
                                          "{{name}}"
                                          (props "host.name")]))]
    (GraphiteReporterFactory/create
     (props "environment.entertainment.graphite.host")
     (Integer/parseInt (props "environment.entertainment.graphite.port"))
     graphite-prefix
     (Integer/parseInt (props "service.graphite.post.interval"))
     (TimeUnit/valueOf (props "service.graphite.post.unit"))
     (ReporterState/valueOf (props "service.graphite.enabled")))))

(defn configure-mongo-conn-pool [props]
  (let [^MongoOptions opts (mc/mongo-options
                             :threads-allowed-to-block-for-connection-multiplier 10
                             :connections-per-host (Integer/parseInt (props "mongo.connections.max"))
                             :max-wait-time 120000
                             :connect-timeout 30000
                             :socket-timeout 10000
                             :socket-keep-alive false)
         sa (build-server-addresses (props "mongo.hosts"))]
    (monger.core/connect! sa opts)))

(defn setup-mongo-required-elements [props]
  (mc/use-db! "{{name}}")
  (mco/ensure-index "profiles"
                    {"userid" 1 "relationtype" 1 "entitytype" 1 "entityid" 1}
                    {:name "uniqueRelationships" :unique true})
  (mco/ensure-index "profiles" {"entityid" 1} {:name "entityid"})

  (reset! desired-concern
         (write-concern (props "mongo.writeconcern.desired")
                        (props "mongo.writeconcern.desired.timeout")))
  (reset! required-concern
         (write-concern (props "mongo.writeconcern.required")
                        (props "mongo.writeconcern.required.timeout"))))

(def properties (atom {}))
(def service-port (atom nil))

(defn extract-version [path]
  (let [pom-props (read-file-to-properties path)]
    (pom-props "version")))

(defn version []
  (if-let [path (.getResource (ClassLoader/getSystemClassLoader) "META-INF/maven/{{name}}/{{name}}/pom.properties")]
    (extract-version path)
    "localhost"))

(defn persist-random-properties [props]
  (reset! service-port (Integer/parseInt (props "service.port")))
  (reset! properties props))

(defn setup []
  (let [props (load-props)]
    (configure-logging)
    (start-graphite-reporting props)
    (configure-mongo-conn-pool props)
    (setup-mongo-required-elements props)
    (persist-random-properties props)))


(defn check-mongo-connectivity []
  (try
    (cmd/db-stats)
    true
    (catch Exception e (warn e) false)))

(defn get-last-error [db concern]
  (let [last-error (.getLastError (mc/get-db) concern)]
    {:server-used (.toString (.get last-error "serverUsed"))
     :ok (.ok last-error)
     :updated-existing (.get last-error "updatedExisting")
     :n (.get last-error "n")
     :connection-id (.get last-error "connectionId")
     :wtime (.get last-error "wtime")
     :error-message (.getErrorMessage last-error)
     :exception (.getException last-error)}))

(defn write-error-message [concern error]
  (format "Write concern %s was not met within %dms, mongo error message: %s"
           (.getWString concern) (.getWtimeout concern) (.getErrorMessage error)))

(defn throw-mongo-excep [error-msg]
  (throw (MongoException. error-msg)))

(defn check-write-concern [concern fail-func]
  (let [last-error (get-last-error (mc/get-db) concern)]
    (if (:ok last-error)
      last-error
      (fail-func (write-error-message concern last-error)))))

(defn check-write-concerns []
  (if-let [first-success (check-write-concern @desired-concern #(warn %))]
    first-success
    (if-let [second-success (check-write-concern @required-concern throw-mongo-excep)]
      second-success)))

(defn perform-safe [mongo-call]
  (let [db (monger.core/get-db)]
     (try
       (do (.requestEnsureConnection db)
           (mongo-call)
           (check-write-concerns))
       (finally (.requestDone db)))))
