(ns protobuf.core-test
  (:require
    [clojure.test :refer :all]
    [protobuf.core :as protobuf])
  (:import
    (com.google.protobuf CodedInputStream)
    (java.io ByteArrayInputStream)
    (protobuf.examples.photo3 Example3$Photo)
    (protobuf.examples.tutorial AddressBookProtos$Person$PhoneNumber)))

(def sample-data {:number "555-1212" :type :home})

(deftest create-flatland
  (let [sample-phone-number (protobuf/create :flatland
                             AddressBookProtos$Person$PhoneNumber
                             sample-data)]
    (testing "map-based constructor ..."
      (is (= protobuf.impl.flatland.core.FlatlandProtoBuf
             (type sample-phone-number)))
      (is (= sample-data
             (into {} sample-phone-number))))
    (testing "bytes-based constructor ..."
      (let [from-bytes (protobuf/create :flatland
                        AddressBookProtos$Person$PhoneNumber
                        (protobuf/->bytes sample-phone-number))]
        (is (= protobuf.impl.flatland.core.FlatlandProtoBuf
               (type from-bytes)))
        (is (= sample-data
               (into {} from-bytes)))))
    (testing "Google CodedInputStream-based constructor ..."
      (let [from-stream (protobuf/create :flatland
                         AddressBookProtos$Person$PhoneNumber
                         (CodedInputStream/newInstance
                         (protobuf/->bytes sample-phone-number)))]
        (is (= protobuf.impl.flatland.core.FlatlandProtoBuf
               (type from-stream)))
        (is (= sample-data
               (into {} from-stream)))))
    (testing "Java InputStream-based constructor ..."
      (let [from-stream (protobuf/create :flatland
                         AddressBookProtos$Person$PhoneNumber
                         (new ByteArrayInputStream
                              (protobuf/->bytes sample-phone-number)))]
        (is (= protobuf.impl.flatland.core.FlatlandProtoBuf
               (type from-stream)))
        (is (= sample-data
               (into {} from-stream)))))))

(deftest syntax
  (is (= :proto2
         (protobuf/syntax
          (protobuf/create :flatland
           AddressBookProtos$Person$PhoneNumber
           sample-data))))
  (is (= :proto3
         (protobuf/syntax
          (protobuf/create :flatland
           Example3$Photo {})))))
