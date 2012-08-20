(defproject {{name}} "1.0.0-SNAPSHOT"
  :description "Service {{name}}"
  :url "Fix me"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.0"]
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
                 [com.ovi.common.metrics/metrics-graphite "2.1.4"]
                 [com.novemberain/monger "1.1.0"]
                 [clj-http "0.4.2"]
                 [com.github.rest-driver/rest-client-driver "1.1.22" :exclusions [org.slf4j/slf4j-nop]]
                 [junit "4.10"]
                 [midje "1.4.0"]
                 [ring/ring-jetty-adapter "1.1.0"]]

  :profiles {:dev {:plugins [[lein-rpm "0.0.4"]]}}
  :plugins [[lein-ring "0.7.0"]
            [lein-embongo "0.1.1"]
            [lein-release "1.0.73"]
            [lein-midje "2.0.0-SNAPSHOT"]]
  :jvm-opts ["-Dconfig=./resources/local.properties"]

  :test-selectors {:default :unit
                   :unit :unit
                   :acceptance :acceptance
                   :all (constantly true)}

  :lein-release {:release-tasks [:clean :uberjar :pom :rpm]
                 :clojars-url "clojars@clojars.mobile.lnx.nokia.com:"}

  :mongo-port ~(Integer. (get (System/getenv) "MONGO_PORT" 27017))
  :ring {:handler {{name}}.web/app
         :main {{name}}.web
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
