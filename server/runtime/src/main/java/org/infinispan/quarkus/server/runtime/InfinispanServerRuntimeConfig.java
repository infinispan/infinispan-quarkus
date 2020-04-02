package org.infinispan.quarkus.server.runtime;

import org.infinispan.server.Server;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

/**
 * @author wburns
 */
@ConfigRoot(name = "infinispan-server", phase = ConfigPhase.RUN_TIME)
public class InfinispanServerRuntimeConfig {

    /**
     * The root directory of the server, where other things are based
     */
    @ConfigItem(defaultValue = Server.DEFAULT_SERVER_ROOT_DIR)
    String serverPath;
    /**
     * The name of the configuration file. May be an absolute file name or relative to the server configuration directory
     */
    @ConfigItem(defaultValue = Server.DEFAULT_CONFIGURATION_FILE)
    String configFile;

    /**
     * The path of the server configuration directory, relative to the server root directory
     */
    @ConfigItem(defaultValue = Server.DEFAULT_SERVER_CONFIG)
    String configPath;
    /**
     * The data path of the server, where things such as persisted data is stored
     */
    @ConfigItem(defaultValue = Server.DEFAULT_SERVER_DATA)
    String dataPath;
    /**
     * Sets the port to bind the single port on
     */
    @ConfigItem(defaultValue = "11222")
    String bindPort;

    @Override
    public String toString() {
        return "InfinispanServerRuntimeConfig{" +
              "serverPath='" + serverPath + '\'' +
              ", configFile='" + configFile + '\'' +
              ", configPath='" + configPath + '\'' +
              ", dataPath='" + dataPath + '\'' +
              ", bindPort='" + bindPort + '\'' +
              '}';
    }
}
