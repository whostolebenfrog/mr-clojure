(ns leiningen.new.cljskel
  (:use [leiningen.new.templates :only [renderer name-to-path ->files year]]))

(def render (renderer "cljskel"))

(defn cap [s]
  (str (.toUpperCase (subs s 0 1)) (subs s 1)))

(defn cljskel
  "Skeleton Clojure project"
  [name]
  (let [data {:name name
              :upper-name (cap name)
              :lower-name (.toLowerCase name)
              :sanitized (name-to-path name)
              :year (year)}]
    (->files data
             ["project.clj" (render "project.clj" data)]
             ["all" (render "all" data) :executable true]
             ["test_helper.sh" (render "test_helper.sh" data)]
 	     ["release.sh" (render "release.sh" data) :executable true]
             ["acceptance" (render "acceptance" data) :executable true]
             ["integration" (render "integration" data) :executable true]
             [".gitignore" (render ".gitignore" data)]
             ["src/{{sanitized}}/setup.clj" (render "setup.clj" data)]
             ["src/{{sanitized}}/web.clj" (render "web.clj" data)]
             ["resources/logback.xml" (render "logback.xml" data)]

             ["test/{{sanitized}}/unit/web.clj" (render "web_unit.clj" data)]
             ["test/{{sanitized}}/acceptance.clj" (render "acceptance.clj" data)]
             ["test/{{sanitized}}/integration.clj" (render "integration.clj" data)]
             [".midje.clj" (render ".midje.clj" data)]

             ["scripts/bin/start.sh" (render "start.sh" data)]
             ["scripts/bin/stop.sh" (render "stop.sh" data)]
             ["scripts/dmt/post_install" (render "post_install" data)]
             ["scripts/rpm/postinstall.sh" (render "postinstall.sh" data)]
             ["scripts/rpm/postremove.sh" (render "postremove.sh" data)]
             ["scripts/rpm/preinstall.sh" (render "preinstall.sh" data)]
             ["scripts/rpm/preremove.sh" (render "preremove.sh" data)]
             ["scripts/service/{{lower-name}}" (render "jetty" data)])))
