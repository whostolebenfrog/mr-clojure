(ns {{name}}.persistence
  (:require [{{name}}.setup :as setup])
  (:require [monger.collection :as mc :only [insert find-maps]]
        [monger.query :as mq :only [skip limit]]))
