(ns protobuf.core-test
  (:require
    [clojure.test :refer :all]
    [protobuf.core :as protobuf])
  (:import
    (com.google.protobuf CodedInputStream)
    (java.io ByteArrayInputStream)
    (protobuf.examples.tutorial AddressBookProtos$Person$PhoneNumber)))

(deftest create-flatland
  (let [data {:number "555-1212" :type :home}
        sample-phone-number (protobuf/create :flatland
                             AddressBookProtos$Person$PhoneNumber
                             data)]
    (testing "map-based constructor ..."
      (is (= protobuf.impl.flatland.core.FlatlandProtoBuf
             (type sample-phone-number)))
      (is (= data
             (into {} sample-phone-number))))
    (testing "bytes-based constructor ..."
      (let [from-bytes (protobuf/create :flatland
                        AddressBookProtos$Person$PhoneNumber
                        (protobuf/->bytes sample-phone-number))]
        (is (= protobuf.impl.flatland.core.FlatlandProtoBuf
               (type from-bytes)))
        (is (= data
               (into {} from-bytes)))))
    (testing "Google CodedInputStream-based constructor ..."
      (let [from-stream (protobuf/create :flatland
                         AddressBookProtos$Person$PhoneNumber
                         (CodedInputStream/newInstance
                         (protobuf/->bytes sample-phone-number)))]
        (is (= protobuf.impl.flatland.core.FlatlandProtoBuf
               (type from-stream)))
        (is (= data
               (into {} from-stream)))))
    (testing "Java InputStream-based constructor ..."
      (let [from-stream (protobuf/create :flatland
                         AddressBookProtos$Person$PhoneNumber
                         (new ByteArrayInputStream
                              (protobuf/->bytes sample-phone-number)))]
        (is (= protobuf.impl.flatland.core.FlatlandProtoBuf
               (type from-stream)))
        (is (= data
               (into {} from-stream)))))))
