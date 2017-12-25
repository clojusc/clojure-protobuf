# Extensions

The Clojure protobuf library supports extensions to protocol buffers which
provide sets and maps using repeated fields. Furthermore, the Clojure protobuf
extension allows you to provide metadata on protobuf fields using Clojure
syntax.

To use these, you must import the extension `.proto` file in the protobuf file
where you want to use our extensions and then include it when compiling.

For example:

```proto
import "protobuf/core/extensions.proto";

message Photo {
  required int32  id     = 1;
  required string path   = 2;
  repeated Label  labels = 3 [(set)    = true];
  repeated Attr   attrs  = 4 [(map)    = true];
  repeated Tag    tags   = 5 [(map_by) = "person_id"];

  message Label {
    required string item   = 1;
    required bool   exists = 2;
  }

  message Attr {
    required string key = 1;
    optional string val = 2;
  }

  message Tag {
    required int32 person_id = 1;
    optional int32 x_coord   = 2 [(meta) = "{:max 100.0 :min -100.0}"];
    optional int32 y_coord   = 3;
    optional int32 width     = 4;
    optional int32 height    = 5;
  }
}
```

Then you can access the extension fields in Clojure:

Start up the REPL and do the appropriate requires/imports:

```clj
[protobuf.dev] λ=> (require '[protobuf.core :as protobuf])
nil
[protobuf.dev] λ=> (import (protobuf.examples.photo Example$Photo Example$Photo$Tag))
protobuf.examples.photo.Example$Photo$Tag
```

Create our protobuf:

```clj
[protobuf.dev] λ=> (def Photo (protobuf/mapdef Example$Photo))
#'protobuf.dev/Photo
[protobuf.dev] λ=> (def Tag (protobuf/mapdef Example$Photo$Tag))
#'protobuf.dev/Tag
[protobuf.dev] λ=> (def p (protobuf/create
              #_=>          Photo
              #_=>          :id 7
              #_=>          :path "/photos/h2k3j4h9h23"
              #_=>          :labels #{"hawaii" "family" "surfing"}
              #_=>          :attrs {"dimensions" "1632x1224", "alpha" "no", "color space" "RGB"}
              #_=>          :tags  {4 {:person_id 4, :x_coord 607, :y_coord 813, :width 25, :height 27}}))
#'protobuf.dev/p
[protobuf.dev] λ=> p
{:id 7,
 :path "/photos/h2k3j4h9h23",
 :labels #{"family" "hawaii" "surfing"},
 :attrs {"alpha" "no", "color space" "RGB", "dimensions" "1632x1224"},
 :tags
 {4 {:person-id 4, :x-coord 607, :y-coord 813, :width 25, :height 27}}}
```

Convert to and from bytes:

```clj
[protobuf.dev] λ=> (def b (protobuf/->bytes p))
#'protobuf.dev/b
[protobuf.dev] λ=> b
#object["[B" 0x4455bb6e "[B@4455bb6e"]
[protobuf.dev] λ=> (protobuf/parse Photo b)
{:id 7,
 :path "/photos/h2k3j4h9h23",
 :labels #{"family" "hawaii" "surfing"},
 :attrs {"alpha" "no", "color space" "RGB", "dimensions" "1632x1224"},
 :tags
 {4 {:person-id 4, :x-coord 607, :y-coord 813, :width 25, :height 27}}}
```

Get the schema for a protobuf (or extract a part of it):

```clj
[protobuf.dev] λ=> (protobuf/mapdef->schema Tag)
 :name "protobuf.examples.photo.Photo.Tag",
 :fields
 {:person-id {:type :int},
  :x-coord {:type :int, :max 100.0, :min -100.0},
  :y-coord {:type :int},
  :width {:type :int},
  :height {:type :int}}}
[protobuf.dev] λ=> (get-in (protobuf/mapdef->schema Tag) [:fields :x-coord])
{:type :int, :max 100.0, :min -100.0}
```
