(ns protobuf.impl.flatland.core
  "This implementation takes its name from the original code for this project
  that was done under the flatland Github org and which pulled in several
  flatland libraries as dependencies."
  (:require
    [protobuf.impl.flatland.map :as protobuf-map]
    [protobuf.impl.flatland.mapdef :as protobuf]))

(defrecord FlatlandProtoBuf
  [protobuf-class
   java-wrapper
   init-data
   instance])

(defn create
  [protobuf-class data]
  (let [wrapper (protobuf/mapdef protobuf-class)]
    (map->FlatlandProtoBuf
      {:protobuf-class protobuf-class
       :java-wrapper wrapper
       :instance (protobuf/create wrapper data)})))

(defn schema
  [protobuf-class]
  (protobuf/mapdef->schema
   (protobuf/mapdef protobuf-class)))

(def behaviour
  {:->bytes (fn [this]
             (protobuf-map/->bytes (:instance this)))
   :->schema (fn [this & args]
              (apply protobuf/mapdef->schema args))
   :bytes-> (fn [this bytes]
             (create
              (:protobuf-class this)
              (protobuf/parse (:java-wrapper this) bytes)))
   :read (fn [this in]
          (protobuf/read (:java-wrapper this) in))
   :write (fn [this out & protobufs]
           (apply protobuf/write (concat [out] protobufs)))})
