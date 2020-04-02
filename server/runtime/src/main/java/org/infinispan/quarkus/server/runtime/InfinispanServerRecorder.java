package org.infinispan.quarkus.server.runtime;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class InfinispanServerRecorder {

    public void configureRuntimeProperties(InfinispanServerRuntimeConfig infinispanServerRuntimeConfig) {
        Arc.container().instance(InfinispanServerProducer.class).get().setRuntimeConfig(infinispanServerRuntimeConfig);
    }
}
