package org.infininspan.quarkus.server;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.NativeImageTest;
import io.quarkus.test.junit.QuarkusTest;

@NativeImageTest
@QuarkusTestResource(ServerResourceTestResource.class)
public class NativeServerResourceIT extends ServerResourceTest {

    // Execute the same tests but in native mode.
}