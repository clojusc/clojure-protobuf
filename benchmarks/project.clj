(defproject clojusc/protobuf-java-benchmark "4.1.0-SNAPSHOT"
  :description "The benchmark tools for Protobuf Java"
  :dependencies [
    [com.google.caliper/caliper "1.0-beta-2"]
    [com.google.protobuf/protobuf-java "3.6.0"]
    [org.clojure/clojure "1.9.0"]]
  :plugins [
    [lein-ancient "0.6.15"]
    [lein-shell "0.5.0"]]
  :java-source-paths ["src/java"]
  :main com.google.protocolbuffers.ProtoBench
  :profiles {
    :ubercompile {
      :aot :all}}
  :aliases {
    ;; Dev tasks
    "ubercompile" [
      "with-profile"
      "+ubercompile"
      "compile"]
    ;; Protobuf compilation tasks
    "protoc-benchmarks" [
      "shell"
      "bin/compile-protobufs"]
    ;; Deps
    "check-deps" [
      "ancient"
      "check"
      ":all"]
    ;; Benchmarking
    "java-benchmark" [
      "shell"
      "bin/java-benchmark"]
    "bench" [
      "do"
        ["clean"]
        ["protoc-benchmarks"]
        ["uberjar"]
        ["java-benchmark"]]})
