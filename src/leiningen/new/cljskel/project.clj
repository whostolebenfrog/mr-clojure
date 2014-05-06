(defproject {{name}} "1.0.0-SNAPSHOT"
  :description "{{upper-name}} service"
  :url "http://wikis.in.nokia.com/NokiaMusicArchitecture/{{upper-name}}"

  :dependencies [[ch.qos.logback/logback-classic "1.1.2"]
                 [cheshire "5.3.1"]
                 [clj-http "0.7.9"]
                 [clj-time "0.7.0"]
                 [com.ovi.common.logging/logback-appender "0.0.45"]
                 [com.ovi.common.metrics/metrics-graphite "2.1.23"]
                 [com.yammer.metrics/metrics-logback "2.2.0"]
                 [compojure "1.1.6" :exclusions [javax.servlet/servlet-api]]
                 [environ "0.5.0"]
                 [metrics-clojure "1.0.1"]
                 [metrics-clojure-ring "1.0.1"]
                 [nokia/instrumented-ring-jetty-adapter "0.1.8"]
                 [nokia/ring-utils "1.2.1"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/data.json "0.2.4"]
                 [org.clojure/data.xml "0.0.7"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.eclipse.jetty/jetty-server "8.1.15.v20140411"]
                 [org.slf4j/slf4j-api "1.7.7"]
                 [org.slf4j/jcl-over-slf4j "1.7.7"]
                 [org.slf4j/jul-to-slf4j "1.7.7"]
                 [org.slf4j/log4j-over-slf4j "1.7.7"]
                 [ring-middleware-format "0.3.2"]
                 [ring/ring-jetty-adapter "1.2.2" :exclusions [org.eclipse.jetty/jetty-server]]]

 :exclusions [commons-logging
              log4j]

  :profiles {:dev {:dependencies [[com.github.rest-driver/rest-client-driver "1.1.36"
                                   :exclusions [org.slf4j/slf4j-nop
                                                javax.servlet/servlet-api
                                                org.eclipse.jetty.orbit/javax.servlet]]
                                  [junit "4.11"]
                                  [midje "1.6.3"]
                                  [rest-cljer "0.1.11"]]
                   :plugins [[lein-rpm "0.0.5"]
                             [lein-midje "3.1.3"]
                             [jonase/kibit "0.0.8"]]}}

  :plugins [[lein-ring "0.8.10"]
            [lein-environ "0.5.0"]
            [lein-release "1.0.73"]]

  ;; development token values
  :env {:environment-name "poke"
        :service-name "{{name}}"
        :service-port "8080"
        :service-url "http://localhost:%s"
        :restdriver-port "8081"
        :environment-entertainment-graphite-host "graphite.brislabs.com"
        :environment-entertainment-graphite-port "8080"
        :service-graphite-post-interval "1"
        :service-graphite-post-unit "MINUTES"
        :service-graphite-enabled "ENABLED"
        :service-production "false"}

  :lein-release {:release-tasks [:clean :uberjar :pom :rpm]
                 :clojars-url "clojars@clojars.brislabs.com:"}

  :ring {:handler {{name}}.web/app
         :main {{name}}.setup
         :port ~(Integer.  (get (System/getenv) "SERVICE_PORT" "8080"))
         :init {{name}}.setup/setup
         :browser-uri "/healthcheck"
         :nrepl {:start? true}}

  :repositories {"internal-clojars"
                 "http://clojars.brislabs.com/repo"
                 "rm.brislabs.com"
                 "http://rm.brislabs.com/nexus/content/groups/all-releases"}

  :uberjar-name "{{lower-name}}.jar"

  :rpm {:name "{{lower-name}}"
        :summary "RPM for {{upper-name}} service"
        :copyright "Nokia {{year}}"
        :preinstall {:scriptFile "scripts/rpm/preinstall.sh"}
        :postinstall {:scriptFile "scripts/rpm/postinstall.sh"}
        :preremove {:scriptFile "scripts/rpm/preremove.sh"}
        :postremove {:scriptFile "scripts/rpm/postremove.sh"}
        :requires ["jdk >= 2000:1.7.0_25-fcs"]
        :mappings [{:directory "/usr/local/{{lower-name}}"
                    :filemode "444"
                    :username "{{lower-name}}"
                    :groupname "{{lower-name}}"
                    :sources {:source [{:location "target/{{lower-name}}.jar"}]}}
                   {:directory "/usr/local/{{lower-name}}/bin"
                    :filemode "744"
                    :username "{{lower-name}}"
                    :groupname "{{lower-name}}"
                    :sources {:source [{:location "scripts/bin"}]}}
                   {:directory "/etc/rc.d/init.d"
                    :filemode "744"
                    :username "{{lower-name}}"
                    :groupname "{{lower-name}}"
                    :sources {:source [{:location "scripts/service/{{lower-name}}"}]}}]}

  :main {{name}}.setup)
