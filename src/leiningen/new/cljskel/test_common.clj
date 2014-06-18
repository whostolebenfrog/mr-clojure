(ns {{name}}.test-common
    (:require [cheshire.core :as json]
              [clojure.string :as str]
              [environ.core :refer [env]]))

(defn url+ [& suffix]
  (apply str (format (env :service-url) (env :service-port)) suffix))
