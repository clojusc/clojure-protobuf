(ns protobuf.codec
  (:require
    [clojure.java.io :as io]
    ;; flatland.io extends Seqable so we can concat InputStream from
    ;; ByteBuffer sequences.
    [flatland.io.core]
    [gloss.core :as gloss]
    [gloss.core.formats :as gloss-formats]
    [gloss.core.protocols :as gloss-protocols]
    [protobuf.core :as protobuf]
    [protobuf.util :as util]))

(declare protobuf-codec)

(def ^{:private true} len-key :proto_length)
(def ^{:private true} reset-key :codec_reset)

(defn length-prefix [proto]
  (let [proto (protobuf/mapdef proto)
        min   (alength (protobuf/->bytes proto {len-key 0}))
        max   (alength (protobuf/->bytes proto {len-key Integer/MAX_VALUE}))]
    (letfn [(check [test msg]
              (when-not test
                (throw (Exception. (format "In %s: %s %s"
                                           (.getFullName proto) (name len-key) msg)))))]
      (check (pos? min)
             "field is required for repeated protobufs")
      (check (= min max)
             "must be of type fixed32 or fixed64"))
    (gloss/compile-frame (gloss/finite-frame max (protobuf-codec proto))
                         #(hash-map len-key %)
                         len-key)))

(defn protobuf-codec [proto & {:keys [validator repeated]}]
  (let [proto (protobuf/mapdef proto)]
    (-> (reify
          ;; Reader method
          gloss-protocols/Reader
          (read-bytes [this buf-seq]
            [true (protobuf/parse proto (io/input-stream buf-seq)) nil])
          ;; Writer method
          gloss-protocols/Writer
          (sizeof [this] nil)
          (write-bytes [this _ val]
            (when (and validator (not (validator val)))
              (throw (IllegalStateException. "Invalid value in protobuf-codec")))
            (gloss-formats/to-buf-seq
             (protobuf/->bytes
              (if (protobuf/map? val)
                val
                (protobuf/create proto val))))))
        (util/fix repeated
             #(gloss/repeated (gloss/finite-frame (length-prefix proto) %)
                              :prefix :none)))))

(defn codec-schema [proto]
  (util/dissoc-fields (protobuf/mapdef->schema proto)
                        len-key reset-key))
