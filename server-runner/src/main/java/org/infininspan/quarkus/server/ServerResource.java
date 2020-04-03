package org.infininspan.quarkus.server;


import java.util.concurrent.CompletableFuture;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.infinispan.server.ExitStatus;
import org.infinispan.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

public class ServerResource {
    private static final Logger LOGGER = LoggerFactory.getLogger("ListenerBean");

    private CompletableFuture<ExitStatus> serverFuture;

    @Inject
    Server server;

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("The application is starting...");
        serverFuture = server.run();
        serverFuture.whenComplete((ignore, t) -> {
            if (t != null) {
              LOGGER.error("Error encountered with server startup!", t);
            }
        });
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOGGER.info("The application is stopping...");
        if (serverFuture != null) {
            serverFuture.complete(ExitStatus.SERVER_SHUTDOWN);
        }
    }
}