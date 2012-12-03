(defproject {{name}} "1.0.0-SNAPSHOT"
  :description "{{upper-name}} service"
  :url "http://wikis.in.nokia.com/NokiaMusicArchitecture/{{upper-name}}"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.3"]
                 [ring-middleware-format "0.1.1"]
                 [org.clojure/data.xml "0.0.4"]
                 [org.clojure/data.json "0.1.2"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.slf4j/slf4j-api "1.6.4"]
                 [org.slf4j/jul-to-slf4j "1.6.0"]
                 [ch.qos.logback/logback-classic "1.0.3"]
                 [com.ovi.common.logging/logback-appender "0.0.32"]
                 [com.yammer.metrics/metrics-logback "2.1.1"]
                 [com.ovi.common.metrics/metrics-graphite "2.1.12"]
                 [clj-http "0.5.2"]
                 [cheshire "4.0.1"]
                 [clj-time "0.4.3"]
                 [midje "1.4.0"]
                 [environ "0.3.0"]
                 [nokia/ring-utils "0.1.2"]
                 [rest-cljer "0.1.2"]
                 [ring/ring-jetty-adapter "1.1.0"]]

  :profiles {:dev {:dependencies [[com.github.rest-driver/rest-client-driver "1.1.22" :exclusions [org.slf4j/slf4j-nop]]
                                  [junit "4.10"]
                                  [clj-http-fake "0.4.1"]]
                   :plugins [[lein-rpm "0.0.4"]
                             [lein-midje "2.0.0-SNAPSHOT"]
                             [jonase/kibit "0.0.4"]]}}
  :plugins [[lein-ring "0.7.3"]
            [environ/environ.lein "0.3.0"]
            [lein-release "1.0.73"]]

  :hooks [environ.leiningen.hooks]

  ;; development token values
  :env {
        :environment-name "Dev"
        :service-name {{name}}
        :service-port "3000"
        :service-url "http://localhost:%s/1.x/"
        :restdriver-port "8081"
        :environment-entertainment-graphite-host "graphite.brislabs.com"
        :environment-entertainment-graphite-port "8080"
        :service-graphite-post-interval "1"
        :service-graphite-post-unit "MINUTES"
        :service-graphite-enabled "ENABLED"
        :service-production "false"
        }

  :test-selectors {:default :unit
                   :unit :unit
                   :acceptance :acceptance
                   :integration :integration}

  :lein-release {:release-tasks [:clean :uberjar :pom :rpm]
                 :clojars-url "clojars@clojars.mobile.lnx.nokia.com:"}

  :ring {:handler {{name}}.web/app
         :main {{name}}.web
         :port ~(Integer.  (get (System/getenv) "SERVICE_PORT" "3000"))
         :init {{name}}.setup/setup}

  :repositories {"internal-clojars"
                 "http://clojars.mobile.lnx.nokia.com/repo"
                 "rm.brislabs.com"
                 "http://rm.brislabs.com/nexus/content/groups/all-releases"}

  :uberjar-name "{{upper-name}}.jar"

  :rpm {:name "{{upper-name}}1"
        :summary "RPM for {{upper-name}} service"
        :copyright "Nokia {{year}}"
        :preinstall {:scriptFile "scripts/rpm/preinstall.sh"}
        :postinstall {:scriptFile "scripts/rpm/postinstall.sh"}
        :preremove {:scriptFile "scripts/rpm/preremove.sh"}
        :postremove {:scriptFile "scripts/rpm/postremove.sh"}
        :requires ["jdk >= 2000:1.6.0_31-fcs"]
        :mappings [{:directory "/usr/local/jetty"
                    :filemode "444"
                    :username "jetty"
                    :groupname "jetty"
                    :sources {:source [{:location "target/{{upper-name}}.jar"}]}}
                   {:directory "/usr/local/jetty/bin"
                    :filemode "744"
                    :username "jetty"
                    :groupname "jetty"
                    :sources {:source [{:location "scripts/bin"}]}}
                   {:directory "/usr/local/deployment/{{upper-name}}1/bin"
                    :filemode "744"
                    :sources {:source [{:location "scripts/dmt"}]}}
                   {:directory "/etc/rc.d/init.d"
                    :filemode "744"
                    :username "jetty"
                    :groupname "jetty"
                    :sources {:source [{:location "scripts/service/jetty"}]}}]}
  :main {{name}}.web

  )
