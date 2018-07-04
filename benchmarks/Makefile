compile-protobufs:
	./bin/compile-protobufs

compile: compile-protobufs
	lein uberjar

java-benchmark:
	./bin/java-benchmark

bench: compile java-benchmark

bench-big: resources/datasets/bigdata bench

resources/datasets/bigdata:
	$(MAKE) -C $@

clean:
	-rm -rv resources/datasets/bigdata/*.pb resources/datasets/bigdata/*.tar.gz

.PHONY: resources/datasets/bigdata
