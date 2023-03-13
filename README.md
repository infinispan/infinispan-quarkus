# infinispan-quarkus
Infinispan Quarkus based extensions and Server

# How to build

1. If you are using a non tagged version (e.g. main
   ), ensure you have built
the same version of Infinispan so that it is available in your maven repository.
2. Download and extract the Java 17 version of GraalVM (>= 22.3) from
https://github.com/graalvm/graalvm-ce-builds/releases
for your distribution.
3. Install `native-image` in the `bin` directory of the extracted graal
runtime
   * `gu install native-image`
4. Set the `GRAALVM_HOME` environment variable to the extracted
graal runtime
5. Build the project
   * `mvn clean install`
