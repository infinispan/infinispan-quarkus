package org.infininspan.quarkus.server;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTestResource(ServerResourceTestResource.class)
@QuarkusTest
public class ServerResourceTest {
   public static final int PORT = 8081;

   @Test
   public void testSimpleWriteAndRetrieve() {
      given()
            .when()
               .body("value-187")
               .post("/rest/v2/caches/quarkus-infinispan-server/key-5")
            .then()
               .body(is(""))
               .statusCode(204);

      given()
            .when()
               .get("/rest/v2/caches/quarkus-infinispan-server/key-5")
            .then()
               .body(is("value-187"))
               .statusCode(200);
   }

   @Test
   public void testGetProtobuf() {
      given()
            .when()
            .get("/rest/v2/caches/___protobuf_metadata?action=size")
            .then()
            .body(is("0"))
            .statusCode(200);
   }

   @Test
   public void testHotRodGetCacheNames() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(PORT);

      RemoteCacheManager remoteCacheManager = new RemoteCacheManager(builder.build());
      Set<String> cacheNames = remoteCacheManager.getCacheNames();
      assertNotNull(cacheNames);
      assertTrue(cacheNames.contains("quarkus-infinispan-server"));
   }

   @Test
   public void testHotRodGetCache() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(PORT);

      RemoteCacheManager remoteCacheManager = new RemoteCacheManager(builder.build());
      assertNotNull(remoteCacheManager.getCache("quarkus-infinispan-server"));
   }

   @Test
   public void testHotRodInternalCacheOperations() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(PORT);

      RemoteCacheManager remoteCacheManager = new RemoteCacheManager(builder.build());
      RemoteCache<Object, Object> cache = remoteCacheManager.getCache("___protobuf_metadata");
      assertNotNull(cache);
      cache.size();
   }

   @Test
   public void testHotRodCreateCache() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(PORT);

      RemoteCacheManager remoteCacheManager = new RemoteCacheManager(builder.build());
      XMLStringConfiguration config = new XMLStringConfiguration("<infinispan><cache-container><distributed-cache name=\"created-cache\"/></cache-container></infinispan>");
      RemoteCache<Object, Object> cache = remoteCacheManager
            .administration()
            .getOrCreateCache("created-cache", config);
      assertNotNull(cache);
      assertNotNull(remoteCacheManager.getCache("created-cache"));
   }
}
