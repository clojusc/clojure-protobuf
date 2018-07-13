all: protobufs

protobufs: extensions examples testing

protoc:
	@mkdir -p $(OUT_DIR)
	@protoc \
	  -I=/usr/include \
	  -I=/usr/local/include \
	  -I=resources \
	  --java_out=$(OUT_DIR) \
	  $(IN_FILES)

examples: IN_FILES=resources/protobuf/examples/*.proto
examples: OUT_DIR=target/examples
examples:
	IN_FILES=$(IN_FILES) OUT_DIR=$(OUT_DIR) $(MAKE) protoc

extensions: IN_FILES=resources/protobuf/extensions.proto
extensions: OUT_DIR=target/extensions
extensions:
	IN_FILES=$(IN_FILES) OUT_DIR=$(OUT_DIR) $(MAKE) protoc

testing: IN_FILES=resources/protobuf/testing/*.proto
testing: OUT_DIR=target/testing
testing:
	IN_FILES=$(IN_FILES) OUT_DIR=$(OUT_DIR) $(MAKE) protoc
