(ns protobuf.core
  (:require
    [protobuf.impl.flatland.core :as flatland])
  (:import
    (protobuf.impl.flatland.core FlatlandProtoBuf))
  (:refer-clojure :exclude [map? read]))

(defprotocol ProtoBufAPI
  (->bytes [this])
  (bytes-> [this bytes])
  (read [this in])
  (write [this out & args]))

(extend FlatlandProtoBuf
        ProtoBufAPI
        flatland/behaviour)

(def default-impl-name "flatland")

(defn get-impl
  []
  (keyword (or (System/getProperty "protobuf.impl")
               default-impl-name)))

(defn schema
  [protobuf-class]
  (let [impls {:flatland flatland/schema}
        impl (get-impl)]
    ((impl impls) protobuf-class)))

(defn create
  [protobuf-class data]
  (case (get-impl)
    :flatland (flatland/create protobuf-class data)))
