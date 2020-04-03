package org.infininspan.quarkus.server;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class ServerResourceTestResource implements QuarkusTestResourceLifecycleManager {
   @Override
   public Map<String, String> start() {
      Map<String, String> settings = new HashMap<>();
      // Dump the data into target so it is ignored
      settings.put("quarkus.infinispan-server.data-path", "target/data");
      // Make server path the same as root directory
      settings.put("quarkus.infinispan-server.server-path", ".");
      settings.put("quarkus.infinispan-server.config-file", "infinispan-local.xml");
      settings.put("quarkus.infinispan-server.config-path",  Paths.get("src", "test", "resources", "conf").toString());
      settings.put("quarkus.infinispan-server.bind-port", "8081");

      // Enable the following for tracing
      settings.put("quarkus.log.console.level", "INFO");
      settings.put("quarkus.log.category.\"org.infinispan\".level", "TRACE");
      settings.put("quarkus.log.file.enable", "true");
      return settings;
   }

   @Override
   public void stop() {

   }
}
