
# Protocol Buffers Benchmarks

This directory contains benchmarking schemas and data sets that is
used to test a variety of performance scenarios against protobuf-java.
protobuf language runtime. This is specifically used to identify any
performance regressions.

If you are looking for performance numbers of officially support languages, see 
[here](https://github.com/google/protobuf/blob/master/docs/performance.md)

To run the tests:

```
$ lein bench
```

This will download additional big testing data which is not included in the
directory initially.


## Benchmark datasets

Each data set is in the format of benchmarks.proto:

1. name is the benchmark dataset's name.
2. message_name is the benchmark's message type full name (including package and message name)
3. payload is the list of raw data.

The schema for the datasets is described in `benchmarks.proto`.
