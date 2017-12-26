(ns protobuf.impl.flatland.codec-test
  (:require
    [clojure.test :refer :all]
    [protobuf.impl.flatland.codec :as protobuf-codec])
  (:import
    (java.nio ByteBuffer)
    (protobuf.testing Codec$Foo)))

(deftest protobuf-codec-test
  (let [codec (protobuf-codec/create Codec$Foo)]
    (testing "decode an encoded data structure"
      (let [val {:foo 1 :bar 2}]
        (is (= val (protobuf-codec/decode codec (protobuf-codec/encode codec val))))))

    (testing "append two simple encoded data structures"
      (let [data1 (protobuf-codec/encode codec {:foo 1 :bar 2})
            data2 (protobuf-codec/encode codec {:foo 4 :baz 8})]
        (is (= {:foo 4 :bar 2 :baz 8}
               (protobuf-codec/decode codec (concat data1 data2))))))

    (testing "concat lists when appending"
      (let [data1 (protobuf-codec/encode codec {:tags ["foo" "bar"] :foo 1})
            data2 (protobuf-codec/encode codec {:tags ["baz" "foo"] :foo 2})]
        (is (= {:foo 2 :tags ["foo" "bar" "baz" "foo"]}
               (protobuf-codec/decode codec (concat data1 data2))))))

    (testing "merge maps when appending"
      (let [data1 (protobuf-codec/encode codec {:num-map {1 "one" 3 "three"}})
            data2 (protobuf-codec/encode codec {:num-map {2 "dos" 3 "tres"}})
            data3 (protobuf-codec/encode codec {:num-map {3 "san" 6 "roku"}})]
        (is (= {:num-map {1 "one" 2 "dos" 3 "san" 6 "roku"}}
               (protobuf-codec/decode codec (concat data1 data2 data3))))))

    (testing "merge sets when appending"
      (let [data1 (protobuf-codec/encode codec {:tag-set #{"foo" "bar"}})
            data2 (protobuf-codec/encode codec {:tag-set #{"baz" "foo"}})]
        (is (= {:tag-set #{"foo" "bar" "baz"}}
               (protobuf-codec/decode codec (concat data1 data2))))))

    (testing "support set deletion using existence map"
      (let [data1 (protobuf-codec/encode codec {:tag-set #{"foo" "bar" "baz"}})
            data2 (protobuf-codec/encode codec {:tag-set {"baz" false "foo" true "zap" true "bam" false}})]
        (is (= {:tag-set #{"foo" "bar" "zap"}}
               (protobuf-codec/decode codec (concat data1 data2))))))

    (testing "merge and append nested data structures when appending"
      (let [data1 (protobuf-codec/encode codec {:nested {:foo 1 :tags ["bar"] :nested {:tag-set #{"a" "c"}}}})
            data2 (protobuf-codec/encode codec {:nested {:foo 4 :tags ["baz"] :bar 3}})
            data3 (protobuf-codec/encode codec {:nested {:baz 5 :tags ["foo"] :nested {:tag-set {"b" true "c" false}}}})]
        (is (= {:nested {:foo 4 :bar 3 :baz 5 :tags ["bar" "baz" "foo"] :nested {:tag-set #{"a" "b"}}}}
               (protobuf-codec/decode codec (concat data1 data2 data3))))))))

(deftest repeated-protobufs
  (let [len (protobuf-codec/length-prefix Codec$Foo)
        codec (protobuf-codec/create Codec$Foo :repeated true)]
    (testing "length-prefix"
      (doseq [i [0 10 100 1000 10000 100000 Integer/MAX_VALUE]]
        (is (= i (protobuf-codec/decode len (protobuf-codec/encode len i))))))
    (testing "repeated"
      (let [data1 (protobuf-codec/encode codec [{:foo 1 :bar 2}])
            data2 (protobuf-codec/encode codec [{:foo 4 :baz 8}])]
        (is (= [{:foo 1 :bar 2} {:foo 4 :baz 8}]
               (protobuf-codec/decode codec (concat data1 data2))))))))
