(ns protobuf.core
  (:require
    [clojure.java.io :as io]
    [protobuf.schema :as protobuf-schema]
    [protobuf.util :as util])
  (:import
    (clojure.lang Reflector)
    (com.google.protobuf CodedInputStream
                         Descriptors$Descriptor
                         GeneratedMessage)
    (java.io InputStream OutputStream)
    (protobuf Extensions
              PersistentProtocolBufferMap
              PersistentProtocolBufferMap$Def
              PersistentProtocolBufferMap$Def$NamingStrategy))
  (:refer-clojure :exclude [map?]))

(defn map?
  "Is the given object a `PersistentProtocolBufferMap?`"
  [obj]
  (instance? PersistentProtocolBufferMap obj))

(defn mapdef?
  "Is the given object a `PersistentProtocolBufferMap$Def?`"
  [obj]
  (instance? PersistentProtocolBufferMap$Def obj))

;; rename to mapdef
(defn ^PersistentProtocolBufferMap$Def protodef
  "Create a protodef from a string or protobuf class."
  ([map-def]
     (if (or (mapdef? map-def) (nil? map-def))
       map-def
       (protodef map-def {})))
  ([map-def opts]
     (when map-def
       (let [{:keys [^PersistentProtocolBufferMap$Def$NamingStrategy naming-strategy
                     size-limit]
              :or {naming-strategy PersistentProtocolBufferMap$Def/convertUnderscores
                   size-limit 67108864}} opts ;; 64MiB
             ^Descriptors$Descriptor descriptor
             (if (instance? Descriptors$Descriptor map-def)
               map-def
               (Reflector/invokeStaticMethod ^Class map-def "getDescriptor" (to-array nil)))]
         (PersistentProtocolBufferMap$Def/create descriptor naming-strategy size-limit)))))

;; rename to create
(defn protobuf
  "Construct a protobuf of the given map-def."
  ([^PersistentProtocolBufferMap$Def map-def]
     (PersistentProtocolBufferMap/construct map-def {}))
  ([^PersistentProtocolBufferMap$Def map-def m]
     (PersistentProtocolBufferMap/construct map-def m))
  ([^PersistentProtocolBufferMap$Def map-def k v & kvs]
     (PersistentProtocolBufferMap/construct map-def (apply array-map k v kvs))))

;; rename to mapdef->schema
(defn protobuf-schema
  "Return the schema for the given protodef."
  [& args]
  (let [^PersistentProtocolBufferMap$Def map-def (apply protodef args)]
    (protobuf-schema/field-schema (.getMessageType map-def) map-def)))

;; rename to parse; change to multimethod
(defn protobuf-load
  "Load a protobuf of the given map-def from an array of bytes."
  ([^PersistentProtocolBufferMap$Def map-def ^bytes data]
     (when data
       (PersistentProtocolBufferMap/create map-def data)))
  ([^PersistentProtocolBufferMap$Def map-def ^bytes data ^Integer offset ^Integer length]
     (when data
       (let [^CodedInputStream in (CodedInputStream/newInstance data offset length)]
         (PersistentProtocolBufferMap/parseFrom map-def in)))))

(defn protobuf-load-stream
  "Load a protobuf of the given map-def from an InputStream."
  [^PersistentProtocolBufferMap$Def map-def ^InputStream stream]
  (when stream
    (let [^CodedInputStream in (CodedInputStream/newInstance stream)]
      (PersistentProtocolBufferMap/parseFrom map-def in))))

;; rename to ->bytes
(defn ^"[B" protobuf-dump
  "Return the byte representation of the given protobuf."
  ([^PersistentProtocolBufferMap p]
     (.toByteArray p))
  ([^PersistentProtocolBufferMap$Def map-def m]
     (protobuf-dump (PersistentProtocolBufferMap/construct map-def m))))

;; rename to read
(defn protobuf-seq
  "Lazily read a sequence of length-delimited protobufs of the specified map-def
  from the given input stream."
  [^PersistentProtocolBufferMap$Def map-def in]
  (lazy-seq
   (io!
    (let [^InputStream in (io/input-stream in)]
      (if-let [p (PersistentProtocolBufferMap/parseDelimitedFrom map-def in)]
        (cons p (protobuf-seq map-def in))
        (.close in))))))

;; rename to write
(defn protobuf-write
  "Write the given protobufs to the given output stream, prefixing each with
  its length to delimit them."
  [out & ps]
  (io!
   (let [^OutputStream out (io/output-stream out)]
     (doseq [^PersistentProtocolBufferMap p ps]
       (.writeDelimitedTo p out))
     (.flush out))))

(extend-protocol util/Combiner
  PersistentProtocolBufferMap
  (combine-onto [^PersistentProtocolBufferMap this other]
    (.append this other)))

;; TODO make this nil-safe? Or just delete it?
(defn get-raw
  "Get value at key ignoring extension fields."
  [^PersistentProtocolBufferMap p key]
  (.getValAt p key false))

;;; Aliases

(def ^{:doc "Backwards-compatible alias for `protobuf.core/map?`"}
  protobuf? #'map?)

(def ^{:doc "Backwards-compatible alias for `mapdef?`"}
  protodef? #'mapdef?)
