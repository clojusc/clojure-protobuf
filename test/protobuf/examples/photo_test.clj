(ns protobuf.examples.photo-test
  (:require
    [clojure.test :refer :all]
    [protobuf.impl.flatland.mapdef :as protobuf])
  (:import
    (com.google.protobuf ByteString)
    (protobuf.examples.photo Example$Photo)))

(def Photo (protobuf/mapdef Example$Photo))

(def data {:id 7
           :path "/photos/h2k3j4h9h23"
           :labels #{"hawaii" "family" "surfing"},
           :attrs {"color space" "RGB" "dimensions" "1632x1224" "alpha" "no"},
           :tags {4 {:person-id 4
                     :x-coord 607
                     :y-coord 813
                     :width 25
                     :height 27}}
           :image (ByteString/copyFrom (byte-array (map unchecked-byte [1 2 3 4 -1])))})

(deftest example-test
  (is (= data (apply protobuf/create Photo (apply concat data)))))
