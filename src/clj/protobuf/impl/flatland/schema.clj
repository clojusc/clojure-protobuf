(ns protobuf.impl.flatland.schema
  (:require
    [clojure.string :as string]
    [protobuf.util :as util])
  (:import
    (protobuf Extensions
              PersistentProtocolBufferMap
              PersistentProtocolBufferMap$Def )
    (com.google.protobuf Descriptors$Descriptor
                         Descriptors$FieldDescriptor
                         Descriptors$FieldDescriptor$Type)))

(defn extension
  [ext ^Descriptors$FieldDescriptor field]
  (-> (.getOptions field)
      (.getExtension ext)
      (util/fix string? not-empty)))

(defn field-type
  [field]
  (condp instance? field
    Descriptors$FieldDescriptor
    (if (.isRepeated ^Descriptors$FieldDescriptor field)
      (condp extension field
        (Extensions/counter)    :counter
        (Extensions/succession) :succession
        (Extensions/map)        :map
        (Extensions/mapBy)      :map-by
        (Extensions/set)        :set
        :list)
      :basic)
    Descriptors$Descriptor
    :struct))

(defmulti field-schema
  (fn [field _ & _] (field-type field)))

(defn struct-schema
  [^Descriptors$Descriptor struct
   ^PersistentProtocolBufferMap$Def map-def
   & [parents]]
  (let [struct-name (.getFullName struct)]
    (into {:type :struct
           :name struct-name}
          (when (not-any? (partial = struct-name) parents)
            {:fields (into {}
                           (for [^Descriptors$FieldDescriptor field (.getFields struct)]
                             [(.intern map-def (.getName field))
                              (field-schema field map-def (conj parents struct-name))]))}))))

(defn basic-schema
  [^Descriptors$FieldDescriptor field
   ^PersistentProtocolBufferMap$Def map-def
   & [parents]]
  (let [java-type   (keyword (string/lower-case (.name (.getJavaType field))))
        meta-string (extension (Extensions/meta) field)]
    (merge (case java-type
             :message (struct-schema (.getMessageType field) map-def parents)
             :enum    {:type   :enum
                       :values (set (map #(.clojureEnumValue map-def %)
                                         (.. field getEnumType getValues)))}
             {:type java-type})
           (when (.hasDefaultValue field)
             {:default (.getDefaultValue field)})
           (when meta-string
             (read-string meta-string)))))

(defn subfield
  [^Descriptors$FieldDescriptor field field-name]
  (.findFieldByName (.getMessageType field) (name field-name)))

(defmethod field-schema :basic
  [field map-def & [parents]]
  (basic-schema field map-def parents))

(defmethod field-schema :list
  [field map-def & [parents]]
  {:type   :list
   :values (basic-schema field map-def parents)})

(defmethod field-schema :succession
  [field map-def & [parents]]
  (assoc (basic-schema field map-def parents)
    :succession true))

(defmethod field-schema :counter
  [field map-def & [parents]]
  (assoc (basic-schema field map-def parents)
    :counter true))

(defmethod field-schema :set
  [field map-def & [parents]]
  {:type   :set
   :values (field-schema (subfield field :item) map-def parents)})

(defmethod field-schema :map
  [field map-def & [parents]]
  {:type   :map
   :keys   (field-schema (subfield field :key) map-def parents)
   :values (field-schema (subfield field :val) map-def parents)})

(defmethod field-schema :map-by
  [field map-def & [parents]]
  (let [map-by (extension (Extensions/mapBy) field)]
    {:type   :map
     :keys   (field-schema (subfield field map-by) map-def parents)
     :values (basic-schema field map-def parents)}))

(defmethod field-schema :struct
  [field map-def & [parents]]
  (struct-schema field map-def parents))
