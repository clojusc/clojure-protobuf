(ns protobuf.dev
  (:require
    [clojure.pprint :refer [print-table]]
    [clojure.reflect :refer [reflect]]
    [protobuf.impl.flatland.codec :as codec]
    [protobuf.impl.flatland.core :as core]
    [protobuf.impl.flatland.schema :as schema])
  (:import
    (protobuf.testing Codec$Foo)))

(defn show-methods
  "Display a Java object's public methods."
  [obj]
  (print-table
    (sort-by :name
      (filter (fn [x]
                (contains? (:flags x) :public))
              (:members (reflect obj))))))
