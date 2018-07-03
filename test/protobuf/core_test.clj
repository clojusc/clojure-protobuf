(ns protobuf.core-test
  (:require
    [clojure.test :refer :all]
    [protobuf.core :as protobuf])
  (:import
    (protobuf.examples.tutorial AddressBookProtos$AddressBook)))

(deftest ->schema
  (let [AddressBook (protobuf/create AddressBookProtos$AddressBook)
        schema (protobuf/->schema AddressBook)]
    (is (= :home
           (get-in (protobuf/->schema AddressBook)
                   [:fields :people :values
                    :fields :phones :values
                    :fields :type :default])))))
