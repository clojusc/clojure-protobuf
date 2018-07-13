(ns protobuf.syntax-test
  (:require
    [clojure.test :refer :all]
    [protobuf.syntax :as syntax])
  (:import
    (com.google.protobuf Descriptors$FileDescriptor$Syntax)))

(deftest lookup
  (let [result (syntax/lookup :proto2)]
    (is (= com.google.protobuf.Descriptors$FileDescriptor$Syntax
           (type result)))
    (is (= "PROTO2" (str result))))
  (let [result (syntax/lookup :proto3)]
    (is (= com.google.protobuf.Descriptors$FileDescriptor$Syntax
           (type result)))
    (is (= "PROTO3" (str result))))
  (let [result (syntax/lookup :unknown)]
    (is (= com.google.protobuf.Descriptors$FileDescriptor$Syntax
           (type result)))
    (is (= "UNKNOWN" (str result)))))
