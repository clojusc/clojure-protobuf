(ns protobuf.syntax
  (:require
    [clojure.string :as string]
    [protobuf.common :as common])
  (:import
    (clojure.lang Keyword)
    (com.google.protobuf Descriptors$FileDescriptor
                         Descriptors$FileDescriptor$Syntax)))

(defn lookup
  [^Keyword syntax]
  (-> syntax
      name
      string/upper-case
      Descriptors$FileDescriptor$Syntax/valueOf))

(defn format
  [^Descriptors$FileDescriptor$Syntax syntax]
  (-> syntax
      str
      string/lower-case
      keyword))
