# protobuf

[![Build Status][travis-badge]][travis][![Clojars Project][clojars-badge]][clojars][![Releases][tag-badge]][tag][![Clojure version][clojure-v]](project.clj)

*A Clojure interface to Google's protocol buffers*

[![][logo]][logo-large]

This project provides a Clojure interface to Google's
[protocol buffers](http://code.google.com/p/protobuf). Protocol buffers can be
used to communicate with other languages over the network, and they are WAY
faster to serialize and deserialize than standard Clojure objects.


## Getting started

Add the dependency to your `project.clj`:

[![Clojars Project][clojars-badge]][clojars]

Then, given a project with the following in `resources/proto/your/namespace/person.proto`:

```proto
package your.namespace.person;

option java_outer_classname = "Example";

message Person {
  required int32  id    = 1;
  required string name  = 2;
  optional string email = 3;
  repeated string likes = 4;
}
```

you can compile the proto using the protobuf compiler and include the resulting
`.java` code in your project:

```shell
protoc \
  -I=/usr/include \
  -I=/usr/local/include \
  -I=resources/proto \
  --java_out=$OUT_DIR \
  resources/proto/your/namespace/*.proto
```

Note that, at this point, the files are `.java` source files, not `.class`
files; as such, you will still need to compile them.

We've found a clean way to do this (and how we set up the tests) is to:

* put these `.java` files in an isolated directory
* add that directory to a `:java-source-paths` entry in the `project.clj`
* place that in an appropriate `project.clj` profile


## Usage

Now you can use the protocol buffer in Clojure:

```clojure
(require '[protobuf.core :as protobuf])
(import (your.namespace.person Example$Person)

(def alice (protobuf/create Example$Person
                            {:id 108
                             :name "Alice"
                             :email "alice@example.com"}))
```

Makes some changes to the data and serialize to bytes:

```clj
(def b (-> alice
           (assoc :name "Alice B. Carol")
           (assoc :likes ["climbing" "running" "jumping"])
           (protobuf/->bytes)))
```

Round-trip the bytes back to a probuf object:

```clj
(protobuf/bytes-> alice b)
```

Which gives us:

```clj
{:id 108,
 :name "Alice B. Carol",
 :email "alice@example.com",
 :likes ["climbing" "running" "jumping"]}
```

The data stored in the `:instance` key is a protocol buffer map and is
immutable just like other clojure objects. It is similar to a struct-map,
except that you cannot insert fields that aren't specified in the `.proto`
file.

(For instance, if you do a round trip with the data like we did above, but use
`:dislikes` -- not in the protobuf definition -- instead of `:likes`,
converting from bytes back to the protobuf instance will result in the
`:dislikes` key and associated value being dropped.)


## Documentation

The above usage is a quick taste; for more examples as well as the current and
previous reference documentation, visit the
[Clojure protobuf documentation][docs].


<!-- Named page links below: /-->

[travis]: https://travis-ci.org/clojusc/protobuf
[travis-badge]: https://travis-ci.org/clojusc/protobuf.png?branch=master
[deps]: http://jarkeeper.com/clojusc/protobuf
[deps-badge]: http://jarkeeper.com/clojusc/protobuf/status.svg
[logo]: ux-resources/images/google-protocol-buffer-small.png
[logo-large]: ux-resources/images/google-protocol-buffer.png
[tag-badge]: https://img.shields.io/github/tag/clojusc/protobuf.svg
[tag]: https://github.com/clojusc/protobuf/tags
[clojure-v]: https://img.shields.io/badge/clojure-1.8.0-blue.svg
[clojars]: https://clojars.org/clojusc/protobuf
[clojars-badge]: https://img.shields.io/clojars/v/clojusc/protobuf.svg
[docs]: https://clojusc.github.io/protobuf
