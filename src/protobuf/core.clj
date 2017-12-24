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
              PersistentProtocolBufferMap$Def$NamingStrategy)))

;;; XXX Create aliases with def docstrings for all the renames below!

;; rename to map?
(defn protobuf?
  "Is the given object a PersistentProtocolBufferMap?"
  [obj]
  (instance? PersistentProtocolBufferMap obj))

;; rename to mapdef?
(defn protodef?
  "Is the given object a PersistentProtocolBufferMap$Def?"
  [obj]
  (instance? PersistentProtocolBufferMap$Def obj))

;; rename to mapdef
(defn ^PersistentProtocolBufferMap$Def protodef
  "Create a protodef from a string or protobuf class."
  ([mapdef]
     (if (or (protodef? mapdef) (nil? mapdef))
       mapdef
       (protodef mapdef {})))
  ([mapdef opts]
     (when mapdef
       (let [{:keys [^PersistentProtocolBufferMap$Def$NamingStrategy naming-strategy
                     size-limit]
              :or {naming-strategy PersistentProtocolBufferMap$Def/convertUnderscores
                   size-limit 67108864}} opts ;; 64MiB
             ^Descriptors$Descriptor descriptor
             (if (instance? Descriptors$Descriptor mapdef)
               mapdef
               (Reflector/invokeStaticMethod ^Class mapdef "getDescriptor" (to-array nil)))]
         (PersistentProtocolBufferMap$Def/create descriptor naming-strategy size-limit)))))

;; rename to create
(defn protobuf
  "Construct a protobuf of the given mapdef."
  ([^PersistentProtocolBufferMap$Def mapdef]
     (PersistentProtocolBufferMap/construct mapdef {}))
  ([^PersistentProtocolBufferMap$Def mapdef m]
     (PersistentProtocolBufferMap/construct mapdef m))
  ([^PersistentProtocolBufferMap$Def mapdef k v & kvs]
     (PersistentProtocolBufferMap/construct mapdef (apply array-map k v kvs))))

;; rename to mapdef->schema
(defn protobuf-schema
  "Return the schema for the given protodef."
  [& args]
  (let [^PersistentProtocolBufferMap$Def mapdef (apply protodef args)]
    (protobuf-schema/field-schema (.getMessageType mapdef) mapdef)))

;; rename to parse; change to multimethod
(defn protobuf-load
  "Load a protobuf of the given mapdef from an array of bytes."
  ([^PersistentProtocolBufferMap$Def mapdef ^bytes data]
     (when data
       (PersistentProtocolBufferMap/create mapdef data)))
  ([^PersistentProtocolBufferMap$Def mapdef ^bytes data ^Integer offset ^Integer length]
     (when data
       (let [^CodedInputStream in (CodedInputStream/newInstance data offset length)]
         (PersistentProtocolBufferMap/parseFrom mapdef in)))))

(defn protobuf-load-stream
  "Load a protobuf of the given mapdef from an InputStream."
  [^PersistentProtocolBufferMap$Def mapdef ^InputStream stream]
  (when stream
    (let [^CodedInputStream in (CodedInputStream/newInstance stream)]
      (PersistentProtocolBufferMap/parseFrom mapdef in))))

;; rename to ->bytes
(defn ^"[B" protobuf-dump
  "Return the byte representation of the given protobuf."
  ([^PersistentProtocolBufferMap p]
     (.toByteArray p))
  ([^PersistentProtocolBufferMap$Def mapdef m]
     (protobuf-dump (PersistentProtocolBufferMap/construct mapdef m))))

;; rename to read
(defn protobuf-seq
  "Lazily read a sequence of length-delimited protobufs of the specified mapdef
  from the given input stream."
  [^PersistentProtocolBufferMap$Def mapdef in]
  (lazy-seq
   (io!
    (let [^InputStream in (io/input-stream in)]
      (if-let [p (PersistentProtocolBufferMap/parseDelimitedFrom mapdef in)]
        (cons p (protobuf-seq mapdef in))
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
(def ^{:doc "Backwards-compatible alias for `util/combine-onto`"}
  adjoin-onto #'util/combine-onto)
