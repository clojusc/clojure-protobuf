# Basic Usage


## Creating a `.proto` File

In the `resources/proto/examples` directory, the Clojure protobuf project
provides an example "person" protobuf:

```proto
package protobuf.examples.person;

option java_outer_classname = "Example";

message Person {
  required int32 id = 1;
  required string name = 2;
  optional string email = 3;
  repeated string likes = 4;
}
```


## Compiling `.proto` to `.java` and then to `.class`

A convenience script is provided to compile this:
`bin/compile-example-protobufs`. This will create a `.java` file which will be
saved in `target/examples`.

The `:test` profile in the `project.clj` file has added `target/examples` as
one of its `:java-source-paths` -- as such, lein will compile the `.java` files
in `target/examples` to `.class` so that they can be called from Clojure.

If you started the project REPL with the command `lein repl`, then all of this
has already been done for you.


## Using the Clojure API

From the dev REPL, pull in the core API and the compiled protobuf code:

```clj
[protobuf.dev] λ=> (require '[protobuf.core :as protobuf])
nil
[protobuf.dev] λ=> (import '(protobuf.examples.person Example$Person))
protobuf.examples.person.Example$Person
```

Now we can create the Java wrapper for our protocol (see
`protobuf.PersistentProtocolBufferMap` for more details):

```clj
[protobuf.dev] λ=> (def Person (protobuf/mapdef Example$Person))
#'protobuf.dev/Person
```

And with this in hand, we can finally create an instance of the protobuf:

```clj
[protobuf.dev] λ=> (def p (protobuf/create
                            Person
                            :id 4
                            :name "Alice"
                            :email "alice@example.com"))
#'protobuf.dev/p
[protobuf.dev] λ=> p
{:id 4, :name "Alice", :email "alice@example.com"}
```

With our person data in place, we can now do the usual Clojure operations:

```clj
[protobuf.dev] λ=> (assoc p :name "Alice B. Carol")
{:id 4, :name "Alice B. Carol", :email "alice@example.com"}
[protobuf.dev] λ=> (assoc p :likes ["climbing" "running" "jumping"])
{:id 4, :name "Alice", :email "alice@example.com", :likes ["climbing" "running" "jumping"]}
```

Additionally, converting between protobuf bytes and Clojure data is trivial:

```clj
[protobuf.dev] λ=> (def b (protobuf/->bytes p))
#'protobuf.dev/b
[protobuf.dev] λ=> b
#object["[B" 0x7e3a40eb "[B@7e3a40eb"]
[protobuf.dev] λ=> (protobuf/parse Person b)
{:id 4, :name "Alice", :email "alice@example.com"}
```
