# Tutorial: Protocol Buffer Basics

*Note: The content for this tutorial was taken from the
[official Google protobuf Java tutorial][java-tut].*


## Introduction

This tutorial provides a basic Clojure programmer's introduction to working with
protocol buffers. By walking through creating a simple example application, it
shows you how to:

* Define message formats in a `.proto` file.
* Use the protocol buffer compiler.
* Use the Clojure protocol buffer API to write and read messages.

This isn't a comprehensive guide to using protocol buffers in Clojure. For more
detailed reference information, see:

* the [Protocol Buffer Language Guide][protobuf-guide]
* the [Clojure API Reference][clojure-api-ref]
* the [Encoding Reference][enc-ref]

Additionally, you may find these useful:

* the [Java API Reference][java-api-ref]
* the [Java Generated Code Guide][java-generated]


## Why Use Protocol Buffers?

Protocol buffers solve the problem of efficiently serializing and
deserializing structured data in a language-agnostic manner, accessible a vast
array of applications (as long as the applications are written in a language
that has a protobuf library). We'll illustrate this with an example.

The example we're going to use is a very simple "address book" application
that can read and write people's contact details to and from a file. Each
person in the address book has a name, an ID, an email address, and a contact
phone number.

So, how might one serialize and retrieve structured data like this if protobuf
wasn't around? There are a few ways to solve this problem:

* Use Java Serialization. This is the default approach since it's built into the language, but it has a host of well-known problems (see Effective Java, by Josh Bloch pp. 213), and also doesn't work very well if you need to share data with applications written in C++ or Python.
* You can invent an ad-hoc way to encode the data items into a single string – such as encoding 4 ints as "12:3:-23:67". This is a simple and flexible approach, although it does require writing one-off encoding and parsing code, and the parsing imposes a small run-time cost. This works best for encoding very simple data.
* Serialize the data to XML. This approach can be very attractive since XML is (sort of) human readable and there are binding libraries for lots of languages. This can be a good choice if you want to share data with other applications/projects. However, XML is notoriously space intensive, and encoding/decoding it can impose a huge performance penalty on applications. Also, navigating an XML DOM tree is considerably more complicated than navigating simple fields in a class normally would be.

To avoid the problems mentioned in each of the above (as well as many others),
protocol buffers was created. They are the flexible, efficient, automated
solution.

With protocol buffers, you write a `.proto` description of the data structure
you wish to store. From that, the protocol buffer compiler creates compilable
source code that implements automatic encoding and parsing of the protocol
buffer data with an efficient binary format. The generated code provides
getters and setters for the fields that make up a protocol buffer and takes
care of the details of reading and writing the protocol buffer as a unit.
Importantly, the protocol buffer format supports the idea of extending the
format over time in such a way that the code can still read data encoded with
the old format.


## Defining Your Protocol Format

To create your address book application, you'll need to start with a `.proto`
file. The definitions in a `.proto` file are simple: you add a message for each
data structure you want to serialize, then specify a name and a type for each
field in the message. Here is the `.proto` file that defines your messages,
`addressbook.proto`:

```proto
syntax = "proto2";

package tutorial;

option java_package = "protobuf.examples.tutorial";
option java_outer_classname = "AddressBookProtos";

message Person {
  required string name = 1;
  required int32 id = 2;
  optional string email = 3;

  enum PhoneType {
    MOBILE = 0;
    HOME = 1;
    WORK = 2;
  }

  message PhoneNumber {
    required string number = 1;
    optional PhoneType type = 2 [default = HOME];
  }

  repeated PhoneNumber phones = 4;
}

message AddressBook {
  repeated Person people = 1;
}
```

(Additionally, this file has been made available for you in the
`resources/proto/examples` directory of the Clojure protobuf project.)

As you can see, the syntax is similar to C++ or Java. Let's go through each
part of the file and see what it does.

The `.proto` file starts with a package declaration, which helps to prevent
naming conflicts between different projects. In Java, the package name is used
as the Java package unless you have explicitly specified a `java_package`, as
we have here. Even if you do provide a `java_package`, you should still define
a normal package as well to avoid name collisions in the Protocol Buffers name
space as well as in non-Java languages.

<blockquote>
After the package declaration, you can see two options that are Java-specific.
Thanks to Clojure's Java inter-op, we can take advantage of this directly,
without writing our own Clojure protobuf compiler or waiting for Google to
support Clojure.
</blockquote>

`java_package` specifies in what Java package name your generated classes
should live. If you don't specify this explicitly, it simply matches the
package name given by the package declaration, but these names usually aren't
appropriate Java package names (since they usually don't start with a domain
name).

The `java_outer_classname` option defines the class name which should contain
all of the classes in this file. If you don't give a `java_outer_classname`
explicitly, it will be generated by converting the file name to camel case.
For example, `my_proto.proto` would, by default, use `MyProto` as the outer
class name.

Next, you have your message definitions. A message is just an aggregate
containing a set of typed fields. Many standard simple data types are
available as field types, including `bool`, `int32`, `float`, `double`, and
`string.` You can also add further structure to your messages by using other
message types as field types – in the above example the `Person` message
contains `PhoneNumber` messages, while the `AddressBook` message contains
`Person` messages. You can even define message types nested inside other
messages – as you can see, the `PhoneNumber` type is defined inside `Person`. You
can also define `enum` types if you want one of your fields to have one of a
predefined list of values – here you want to specify that a phone number can
be one of `MOBILE`, `HOME`, or `WORK`.

The " = 1", " = 2" markers on each element identify the unique "tag" that
field uses in the binary encoding. Tag numbers 1-15 require one less byte to
encode than higher numbers, so as an optimization you can decide to use those
tags for the commonly used or repeated elements, leaving tags 16 and higher
for less-commonly used optional elements. Each element in a repeated field
requires re-encoding the tag number, so repeated fields are particularly good
candidates for this optimization.

Each field must be annotated with one of the following modifiers:

* `required`: a value for the field must be provided, otherwise the message will be considered "uninitialized". Trying to build an uninitialized message will throw a `RuntimeException`. Parsing an uninitialized message will throw an `IOException`. Other than this, a required field behaves exactly like an optional field.
* `optional`: the field may or may not be set. If an optional field value isn't set, a default value is used. For simple types, you can specify your own default value, as we've done for the phone number type in the example. Otherwise, a system default is used: zero for numeric types, the empty string for strings, `false` for bools. For embedded messages, the default value is always the "default instance" or "prototype" of the message, which has none of its fields set. Calling the accessor to get the value of an optional (or required) field which has not been explicitly set always returns that field's default value.
* `repeated`: the field may be repeated any number of times (including zero). The order of the repeated values will be preserved in the protocol buffer. Think of repeated fields as dynamically sized arrays.

<blockquote>
**Required Is Forever**

You should be very careful about marking fields as required. If at some point
you wish to stop writing or sending a required field, it will be problematic
to change the field to an optional field – old readers will consider messages
without this field to be incomplete and may reject or drop them
unintentionally. You should consider writing application-specific custom
validation routines for your buffers instead. Some engineers at Google have
come to the conclusion that using required does more harm than good; they
prefer to use only optional and repeated. However, this view is not universal.
</blockquote>

You'll find a complete guide to writing `.proto` files – including all the
possible field types – in the
[Protocol Buffer Language Guide][protobuf-guide]. Don't go looking for
facilities similar to class inheritance, though – protocol buffers don't do
that.


## Compiling Your Protocol Buffers

Now that you have a `.proto`, the next thing you need to do is generate the
classes you'll need to read and write `AddressBook` (and hence `Person` and
`PhoneNumber`) messages. To do this, you need to run the protocol buffer
compiler `protoc` on your `.proto`:

1. If you haven't installed `protoc` yet, now is the time. Your operating system's
   package manager very likely already provides a version for you -- search
   there first. If not, you can [download it][download] from Google.
1. Now run the compiler, specifying:
   * the source directory (where your application's source code lives – the
     current directory is used if you don't provide a value)
   * the destination directory (where you want the generated code to go; often
     the same as $SRC_DIR), and
   * the path to your `.proto.`

In this case:

```bash
$ protoc -I=resources --java_out=target/examples \
  resources/protobuf/examples/tutorial.proto
```

This gives us:

```bash
$ ls -1 target/examples/protobuf/examples/tutorial/
AddressBookProtos.java
```

To have `lein` compile protobuf `.java` source files to `.class` files that
can be used from Clojure, we need to make sure that `lein` knows about our
Java sources -- with do this with the `:java-source-paths` key in the
`project.clj` file, either at the top-level or in an appropriate profile.

For example, this is what the Clojure protobuf project has set, using the
`:test` profile:

```clj
:test {
  :java-source-paths [
    "target/examples"
    "target/testing"]}
```

<blockquote>
Fore convenience, the Clojure protobuf project always compiles the examples
and the tests before starting up the REPL, so they are always freshly available
to developers.
</blockquote>


## The Protocol Buffer API

Let's fire up the REPL and see how to use the compiled code from Clojure:

```bash
$ lein repl
```

### Setup

First we'll pull in the Clojure API:

```clj
[protobuf.dev] λ=> (require '[protobuf.core :as protobuf])
nil
```

Then we'll import the generated Java classes we want to use:

```clj
[protobuf.dev] λ=> (import '(protobuf.examples.tutorial AddressBookProtos$Person
              #_=>                                      AddressBookProtos$Person$PhoneNumber
              #_=>                                      AddressBookProtos$AddressBook))
protobuf.examples.tutorial.AddressBookProtos$AddressBook
```

Note that for nested inner classes, we simply keep using the inner class
separator `$` of Clojure's Java inter-op.

Next we'll use a Java wrapper that let's us treat these like maps:

```clj
[protobuf.dev] λ=> (def Person (protobuf/mapdef AddressBookProtos$Person))
#'protobuf.dev/Person
[protobuf.dev] λ=> (def PhoneNumber (protobuf/mapdef AddressBookProtos$Person$PhoneNumber))
#'protobuf.dev/PhoneNumber
[protobuf.dev] λ=> (def AddressBook (protobuf/mapdef AddressBookProtos$AddressBook))
#'protobuf.dev/AddressBook
```

We can view the full, nested data schema in Clojure data:

```clj
[protobuf.dev] λ=> (pprint (protobuf/mapdef->schema AddressBook))
{:type :struct,
 :name "tutorial.AddressBook",
 :fields
 {:people
  {:type :list,
   :values
   {:type :struct,
    :name "tutorial.Person",
    :fields
    {:name {:type :string},
     :id {:type :int},
     :email {:type :string},
     :phones
     {:type :list,
      :values
      {:type :struct,
       :name "tutorial.Person.PhoneNumber",
       :fields
       {:number {:type :string},
        :type
        {:type :enum,
         :values #{:home :work :mobile},
         :default
         #object[com.google.protobuf.Descriptors$EnumValueDescriptor 0x1df8368f "HOME"]}}}}}}}}}
nil
```

<blockquote>
There's currently a bug/missing feature where the default value of an `enum` is
not converted to Clojure data. For current status on this issue, see:
<a href="https://github.com/clojusc/protobuf/issues/22">https://github.com/clojusc/protobuf/issues/22</a>
</blockquote>

## Message Functions

TBD

## Parsing and Serialization

* `protobuf/parse` - Given an array or stream of bytes, we can parse these
  to a Clojure protobuf data structure
* `protobuf/->bytes` - Given a Java protobuf wrapper, we can convert this to
  an array of raw bytes

## Writing a Message

TBD

## Reading a Message

TBD

## Extending a Protocol

TBD

## Advanced Usage

TBD


<!-- Named page links below: /-->

[java-tut]: https://developers.google.com/protocol-buffers/docs/javatutorial
[protobuf-guide]: https://developers.google.com/protocol-buffers/docs/proto
[clojure-api-ref]: https://clojusc.github.io/protobuf
[java-api-ref]: https://developers.google.com/protocol-buffers/docs/reference/java/index.html
[java-generated]: https://developers.google.com/protocol-buffers/docs/reference/java-generated
[enc-ref]: https://developers.google.com/protocol-buffers/docs/encoding
[download]: https://developers.google.com/protocol-buffers/docs/downloads.html
