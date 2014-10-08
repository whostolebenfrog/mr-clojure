(defproject {{name}} "1.0.0-SNAPSHOT"
  :description "{{upper-name}} service"

  :dependencies [[ch.qos.logback/logback-classic "1.1.2"]
                 [cheshire "5.3.1"]
                 [clj-http "0.7.9"]
                 [clj-time "0.8.0"]
                 [compojure "1.2.0"]
                 [environ "1.0.0"]
                 [mixradio/instrumented-ring-jetty-adapter "1.0.0"]
                 [mixradio/radix "1.0.2"]
                 [net.logstash.logback/logstash-logback-encoder "3.2"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ring-middleware-format "0.4.0"]]

  :exclusions [commons-logging
               log4j
               org.clojure/clojure]

  :profiles {:dev {:dependencies [[com.github.rest-driver/rest-client-driver "1.1.36"
                                   :exclusions [org.slf4j/slf4j-nop
                                                javax.servlet/servlet-api
                                                org.eclipse.jetty.orbit/javax.servlet]]
                                  [junit "4.11"]
                                  [midje "1.6.3"]
                                  [rest-cljer "0.1.11"]]
                   :plugins [[lein-rpm "0.0.5"]
                             [lein-midje "3.1.3"]
                             [lein-kibit "0.0.8"]]}}

  :plugins [[lein-ring "0.8.12"]
            [lein-environ "1.0.0"]
            [lein-release "1.0.73"]]

  :env {:environment-name "poke"
        :service-name "{{name}}"
        :service-port "8080"
        :service-url "http://localhost:%s"
        :restdriver-port "8081"
        :environment-entertainment-graphite-host ""
        :environment-entertainment-graphite-port "8080"
        :service-graphite-post-interval "1"
        :service-graphite-post-unit "MINUTES"
        :service-graphite-enabled "DISABLED"
        :service-production "false"
        :service-jetty-gracefulshutdown-millis "5000"}

  :lein-release {:release-tasks [:clean :uberjar :pom :rpm]}

  :ring {:handler {{name}}.web/app
         :main {{name}}.setup
         :port ~(Integer/valueOf (get (System/getenv) "SERVICE_PORT" "8080"))
         :init {{name}}.setup/setup
         :browser-uri "/healthcheck"
         :nrepl {:start? true}}

  :uberjar-name "{{lower-name}}.jar"

  :rpm {:name "{{lower-name}}"
        :summary "RPM for {{upper-name}} service"
        :copyright "Microsoft {{year}}"
        :preinstall {:scriptFile "scripts/rpm/preinstall.sh"}
        :postinstall {:scriptFile "scripts/rpm/postinstall.sh"}
        :preremove {:scriptFile "scripts/rpm/preremove.sh"}
        :postremove {:scriptFile "scripts/rpm/postremove.sh"}
        :requires ["jdk >= 2000:1.7.0_55-fcs"]
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
