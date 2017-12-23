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
    [com.google.protobuf/protobuf-java "3.5.1"]
    [org.flatland/useful "0.11.5"]
    [org.flatland/schematic "0.1.5"]
    [org.flatland/io "0.3.0"]
    [ordered-collections "0.4.2"]
    [gloss "0.2.6"]]
  :java-source-paths ["src"]
  :profiles {
    :test {
      :dependencies [
        [clojusc/ltest "0.3.0-SNAPSHOT"]]
      :plugins [
        [jonase/eastwood "0.2.5"]
        [lein-ancient "0.6.15"]
        [lein-shell "0.5.0"]]
      :java-source-paths ["target/test"]}
    :1.5 {:dependencies [[org.clojure/clojure "1.5.0"]]}
    :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
    :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
    :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
    :docs {
      :dependencies [
        [clojang/codox-theme "0.2.0-SNAPSHOT"]]
      :plugins [
        [lein-codox "0.10.3"]]
      :codox {
        :project {
          :name "protobuf"
          :description "A Clojure interface to Google's protocol buffers"}
        :namespaces [#"^flatland\.protobuf\.(?!dev)"]
        :metadata {
          :doc/format :markdown
          :doc "Documentation forthcoming"}
        :themes [:clojang]
        :doc-paths ["resources/docs"]
        :output-path "docs/current"}}}
  :aliases {
    "protoc-test" [
       "with-profile"
       "+test"
       "shell"
       "bin/compile-test-protobufs"]
    "check-deps" [
      "with-profile"
      "+test"
      "ancient"
      "check"
      ":all"]
    "lint" [
      "with-profile"
      "+test"
      "eastwood"
      "{:namespaces [:source-paths] :source-paths [\"src\"]}"]
    "docs" [
      "with-profile"
      "+docs"
      "codox"]
    "test-all" [
      "with-profile"
      "+1.5:+1.6:+1.7:+1.9:+default"
      "test"]
    "build-test" [
      "do"
      ["clean"]
      ["lint"]
      ["protoc-test"]
      ["test-all"]]})
