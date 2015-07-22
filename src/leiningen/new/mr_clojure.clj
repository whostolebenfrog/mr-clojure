(ns leiningen.new.mr-clojure
  (:use [leiningen.new.templates :only [renderer name-to-path ->files year]]
        [leiningen.core.main :as main])
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def render (renderer "mr-clojure"))

(defn cap [s]
  (str (.toUpperCase (subs s 0 1)) (subs s 1)))

(def valid-opts
  ["--reagent-webapp"])

(defn opt->kw
  [opt]
  (-> opt
      (str/replace #"^--" "")
      (str "?")
      keyword))

(defn parse-opts
  [opts]
  (->> (for [valid-opt valid-opts
             :let [valid-opt? (boolean (some #(= valid-opt %) opts))]]
         [(opt->kw valid-opt) valid-opt?])
       (into {})))

(defn build-data
  [name opts]
  (let [{:keys [reagent-webapp?] :as parsed-opts} (parse-opts opts)
        cljs-app? reagent-webapp?]
    (merge {:name name
            :upper-name (cap name)
            :lower-name (.toLowerCase name)
            :sanitized (name-to-path name)
            :year (year)}
           parsed-opts
           {:cljs-app? cljs-app?
            :clj-path (if cljs-app? "clj/" "")})))

(defn mr-clojure
  "Skeleton Clojure project"
  [name & opts]
  (let [{:keys [cljs-app? clj-path cljs-path] :as data} (build-data name opts)]
    (->> (cond-> [["project.clj" (render "project.clj" data)]
                  ["all" (render "all" data) :executable true]
                  ["test_helper.sh" (render "test_helper.sh" data)]
                  ["acceptance" (render "acceptance" data) :executable true]
                  ["integration" (render "integration" data) :executable true]
                  [".gitignore" (render ".gitignore" data)]
                  ["src/{{clj-path}}{{sanitized}}/setup.clj" (render "setup.clj" data)]
                  ["src/{{clj-path}}{{sanitized}}/web.clj" (render "web.clj" data)]
                  ["resources/logback.xml" (render "logback.xml" data)]

                  ["test/{{sanitized}}/unit/web.clj" (render "web_unit.clj" data)]
                  ["test/{{sanitized}}/test_common.clj" (render "test_common.clj" data)]
                  ["test/{{sanitized}}/acceptance.clj" (render "acceptance.clj" data)]
                  ["test/{{sanitized}}/integration.clj" (render "integration.clj" data)]
                  [".midje.clj" (render ".midje.clj" data)]
          
                  ["scripts/bin/start.sh" (render "start.sh" data)]
                  ["scripts/bin/stop.sh" (render "stop.sh" data)]
                  ["scripts/rpm/postinstall.sh" (render "postinstall.sh" data)]
                  ["scripts/rpm/postremove.sh" (render "postremove.sh" data)]
                  ["scripts/rpm/preinstall.sh" (render "preinstall.sh" data)]
                  ["scripts/rpm/preremove.sh" (render "preremove.sh" data)]
                  ["scripts/service/{{lower-name}}" (render "jetty" data)]]

           cljs-app? (concat [["env/dev/cljs/{{sanitized}}/dev.cljs" (render "dev.cljs" data)]
                              ["env/prod/cljs/{{sanitized}}/prod.cljs" (render "prod.cljs" data)]
                              ["src/clj/{{sanitized}}/page_frame.clj" (render "page_frame.clj" data)]
                              ["src/cljs/{{sanitized}}/core.cljs" (render "core.cljs" data)]
                              ["resources/public/css/site.css" (render "site.css" data)]
                              ["resources/public/img/favicon.ico" (render "favicon.ico")]]))

         (remove nil?)
         (apply ->files data))))
