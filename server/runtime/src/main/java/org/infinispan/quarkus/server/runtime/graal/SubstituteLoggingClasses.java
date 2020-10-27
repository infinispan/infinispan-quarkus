package org.infinispan.quarkus.server.runtime.graal;

import org.infinispan.rest.RestServer;
import org.infinispan.rest.framework.ResourceManager;
import org.infinispan.rest.resources.LoggingResource;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

public class SubstituteLoggingClasses {
}

@TargetClass(RestServer.class)
final class Target_RestServer {
   @Substitute
   private void registerLoggingResource(ResourceManager resourceManager, String restContext) {
      // Do nothing
   }
}

@Delete
@TargetClass(LoggingResource.class)
final class Target_LoggingResource {

}
