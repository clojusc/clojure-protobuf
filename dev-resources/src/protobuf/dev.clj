(ns protobuf.dev
  (:require
    [clojure.pprint :refer [print-table]]
    [clojure.reflect :refer [reflect]]
    [protobuf.core :as protobuf]
    [protobuf.impl.flatland.codec :as f-codec]
    [protobuf.impl.flatland.core :as f-core]
    [protobuf.impl.flatland.map :as f-map]
    [protobuf.impl.flatland.mapdef :as f-mapdef]
    [protobuf.impl.flatland.schema :as f-schema])
  (:import
    (protobuf.examples.person Example$Person)
    (protobuf.examples.photo Example$Photo)
    (protobuf.testing Codec$Foo)))

(defn show-methods
  "Display a Java object's public methods."
  [obj]
  (print-table
    (sort-by :name
      (filter (fn [x]
                (contains? (:flags x) :public))
              (:members (reflect obj))))))
