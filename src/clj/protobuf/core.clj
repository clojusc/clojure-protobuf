(ns protobuf.core
  "This is the public API for working with protocol buffers."
  (:require
    [protobuf.impl.flatland.core :as flatland])
  (:import
    (protobuf.impl.flatland.core FlatlandProtoBuf))
  (:refer-clojure :exclude [map? read]))

(defprotocol ProtoBufAPI
  (->bytes [this])
  (->schema [this])
  (bytes-> [this bytes])
  (read [this in])
  (write [this out]))

(extend FlatlandProtoBuf
        ProtoBufAPI
        flatland/behaviour)

(def default-impl-name "flatland")

(defn get-impl
  "Get the currently configured protobuf implementation. If not defined,
  used the hard-coded default value (see `default-impl-name`).

  Note that the protobuf backend implementation is configured using
  JVM system properties (i.e., the `-D` option). Projects that use `lein`
  may set this with `:jvm-opts`
  (e.g, `:jvm-opts [\"-Dprotobuf.impl=flatland\"]`)."
  []
  (keyword (or (System/getProperty "protobuf.impl")
               default-impl-name)))

(defn schema
  "This function is designed to be called against compiled Java protocol
  buffer classes. To get the schema of a Clojure protobuf instance, you'll
  want to use the `->schema` method."
  [protobuf-class]
  (case (get-impl)
    :flatland (FlatlandProtoBuf/schema protobuf-class)))

(defn create
  [protobuf-class data]
  (case (get-impl)
    :flatland (new FlatlandProtoBuf protobuf-class data)))
