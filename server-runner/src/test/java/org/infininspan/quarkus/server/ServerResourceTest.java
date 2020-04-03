package org.infininspan.quarkus.server;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTestResource(ServerResourceTestResource.class)
@QuarkusTest
public class ServerResourceTest {
   @Test
   public void testSimpleWriteAndRetrieve() {
      given()
            .when()
               .body("value-187")
               .post("/rest/quarkus-infinispan-server/key-5")
            .then()
               .body(is(""))
               .statusCode(204);

      given()
            .when()
               .get("/rest/quarkus-infinispan-server/key-5")
            .then()
               .body(is("value-187"))
               .statusCode(200);
   }
}