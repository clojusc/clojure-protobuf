(ns protobuf.examples.photo-test
  (:require
    [clojure.test :refer :all]
    [protobuf.core :as protobuf])
  (:import
    (com.google.protobuf ByteString)
    (protobuf.examples.photo Example$Photo)))

(def data {
  :id 7
  :path "/photos/h2k3j4h9h23"
  :labels #{"hawaii" "family" "surfing"},
  :attrs {"color space" "RGB" "dimensions" "1632x1224" "alpha" "no"},
  :type :jpeg
  :tags {
    4 {
      :person-id 4
      :x-coord 607
      :y-coord 813
      :width 25
      :height 27}}
  :image (ByteString/copyFrom
          (byte-array
           (map unchecked-byte [1 2 3 4 -1])))})

(deftest example-as-map-test
  (is (= data
         (into {} (protobuf/create Example$Photo data)))))

(deftest example-round-trip-test
  (let [p (protobuf/create Example$Photo data)
        b (protobuf/->bytes p)]
    (= p (protobuf/bytes-> p b))))
