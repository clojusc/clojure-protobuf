(ns protobuf.impl.google.core
  (:refer-clojure :exclude [read]))

(defn ->bytes
  [this]
  :not-implemented)

(defn ->schema
  [this]
  :not-implemented)

(defn bytes->
  [this]
  :not-implemented)

(defn read
  [this]
  :not-implemented)

(defn write
  [this]
  :not-implemented)

(defn schema
  [protobuf-class]
  :not-implemented)

(def behaviour
  {:->bytes ->bytes
   :->schema ->schema
   :bytes-> bytes->
   :read read
   :write write})

(defn create
  [protobuf-class data]
  :not-implemented)
