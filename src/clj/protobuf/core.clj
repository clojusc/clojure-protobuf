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

(defn get-impl
  []
  (keyword (System/getProperty "protobuf.impl")))

(defn create
  [protobuf-class data]
  (case (get-impl)
    :flatland (flatland/create protobuf-class data)))
