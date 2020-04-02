package org.infinispan.quarkus.server.runtime;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.infinispan.commons.CacheConfigurationException;
import org.infinispan.server.Server;

@ApplicationScoped
public class InfinispanServerProducer {
   private InfinispanServerRuntimeConfig infinispanServerRuntimeConfig;

   public void setRuntimeConfig(InfinispanServerRuntimeConfig infinispanServerRuntimeConfig) {
      this.infinispanServerRuntimeConfig = infinispanServerRuntimeConfig;
   }

   @Produces
   Server server() {
      Properties properties = new Properties(System.getProperties());
      properties.put(Server.INFINISPAN_SERVER_CONFIG_PATH, infinispanServerRuntimeConfig.configPath);
      properties.put(Server.INFINISPAN_SERVER_DATA_PATH, infinispanServerRuntimeConfig.dataPath);
      properties.put(Server.INFINISPAN_BIND_PORT, infinispanServerRuntimeConfig.bindPort);

      File serverRootDir = new File(infinispanServerRuntimeConfig.serverPath);
      if (serverRootDir.exists() && !serverRootDir.isDirectory()) {
         throw new CacheConfigurationException("The path for: " + serverRootDir + " is not a directory!");
      }
      File configurationFile = new File(infinispanServerRuntimeConfig.configFile);
      File configurationFileToTest;
      if (configurationFile.isAbsolute()) {
         configurationFileToTest = configurationFile;
      } else {
         Path configPath = Paths.get(infinispanServerRuntimeConfig.serverPath, infinispanServerRuntimeConfig.configPath,
               infinispanServerRuntimeConfig.configFile);
         configurationFileToTest = configPath.toFile();
      }
      if (!configurationFileToTest.exists()) {
         throw new CacheConfigurationException("Configuration file: " + configurationFileToTest + " doesn't exist!");
      }
      if (!configurationFileToTest.isFile()) {
         throw new CacheConfigurationException("The path for : " + configurationFileToTest + " is not a file!");
      }

      return new Server(serverRootDir, configurationFile, properties);
   }
}
