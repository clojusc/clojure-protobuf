(defproject clojusc/protobuf "3.5.1-v1.0-SNAPSHOT"
  :description "A Clojure interface to Google's protocol buffers"
  :url "https://github.com/clojusc/protobuf"
  :license {
    :name "Eclipse Public License"
    :url "http://www.eclipse.org/legal/epl-v10.html"}
  :exclusions [
    [org.clojure/clojure]]
  :dependencies [
    [com.google.protobuf/protobuf-java "3.5.1"]
    [gloss "0.2.6"]
    [ordered-collections "0.4.2"]
    [org.clojure/clojure "1.8.0"]
    [org.flatland/io "0.3.0"]
    [org.flatland/schematic "0.1.5"]
    [org.flatland/useful "0.11.5"]]
  :java-source-paths ["src"]
  :profiles {
    :ubercompile {
      :aot :all}
    :dev {
      :source-paths ["dev-resources/src"]}
    :custom-repl {
      :repl-options {
        :init-ns protobuf.dev
        :prompt ~#(str "\u001B[35m[\u001B[34m"
                       %
                       "\u001B[35m]\u001B[33m λ\u001B[m=> ")}}
    :test {
      :plugins [
        [jonase/eastwood "0.2.5"]
        [lein-ancient "0.6.15"]
        [lein-shell "0.5.0"]]
      :java-source-paths ["target/test"]}
    :docs {
      :dependencies [
        [clojang/codox-theme "0.2.0-SNAPSHOT"]]
      :plugins [
        [lein-codox "0.10.3"]]
      :codox {
        :project {
          :name "protobuf"
          :description "A Clojure interface to Google's protocol buffers"}
        :namespaces [#"^protobuf\.(?!dev)"]
        :metadata {
          :doc/format :markdown
          :doc "Documentation forthcoming"}
        :themes [:clojang]
        :doc-paths ["resources/docs"]
        :output-path "docs/current"}}
    :1.5 {:dependencies [[org.clojure/clojure "1.5.0"]]}
    :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
    :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
    :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}}
  :aliases {
    "protoc-extension" [
      "shell"
      "bin/compile-protobuf-extension"]
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
    "ubercompile" [
      "with-profile"
      "+ubercompile"
      "compile"]
    "repl" [
      "with-profile"
      "+test,+custom-repl"
      "do"
      ["clean"]
      ["protoc-extension"]
      ["protoc-test"]
      ["repl"]]
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
      ["protoc-extension"]
      ["protoc-test"]
      ;["ubercompile"]
      ;["clean"]
      ["test-all"]]})
