(ns protobuf.impl.flatland.map
  (:import
    (protobuf PersistentProtocolBufferMap))
  (:refer-clojure :exclude [map?]))

(defn map?
  "Is the given object a `PersistentProtocolBufferMap?`"
  [obj]
  (instance? PersistentProtocolBufferMap obj))

(defn ^"[B" ->bytes
  "Return the byte representation of the given protobuf."
  [^PersistentProtocolBufferMap p]
  (.toByteArray p))

;; TODO make this nil-safe? Or just delete it?
(defn get-raw
  "Get value at key ignoring extension fields."
  [^PersistentProtocolBufferMap p key]
  (.getValAt p key false))
