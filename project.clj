(defproject clojusc/protobuf "3.4.0-v0.3-SNAPSHOT"
  :description "A Clojure interface to Google's protocol buffers"
  :url "https://github.com/clojusc/protobuf"
  :license {
    :name "Eclipse Public License"
    :url "http://www.eclipse.org/legal/epl-v10.html"}
  :exclusions [
    [org.clojure/clojure]]
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [com.google.protobuf/protobuf-java "3.4.0"]
    [org.flatland/useful "0.11.5"]
    [org.flatland/schematic "0.1.5"]
    [org.flatland/io "0.3.0"]
    [ordered-collections "0.4.2"]
    [gloss "0.2.1"]]
  :java-source-paths ["src"]
  :profiles {
    :test {
      :plugins [[lein-shell "0.5.0"]]
      :java-source-paths ["target/test"]}
    :1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
    :1.5 {:dependencies [[org.clojure/clojure "1.5.0"]]}}
  :aliases {
    "protoc-test" [
       "with-profile"
       "+test"
       "shell"
       "bin/compile-test-protobufs"]
    "test-all" [
      "with-profile"
      "+1.3,+1.5,+default"
      "test"]
    "build-test" [
      "do"
        ["clean"]
        ["protoc-test"]
        ["test-all"]]})
