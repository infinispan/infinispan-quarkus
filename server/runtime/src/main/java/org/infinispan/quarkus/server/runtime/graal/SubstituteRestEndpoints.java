package org.infinispan.quarkus.server.runtime.graal;

import org.infinispan.rest.InvocationHelper;
import org.infinispan.rest.resources.LoggingResource;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

class SubstituteRestEndpoints {
}

@Substitute
@TargetClass(LoggingResource.class)
final class Target_LoggingResource {

   @Substitute
   public Target_LoggingResource(InvocationHelper invocationHelper) {}
}

@Delete
@TargetClass(LoggingResource.Log4j2AppenderSerializer.class)
final class Target_Log4j2AppenderSerializer {
}

@Delete
@TargetClass(LoggingResource.Log4j2LoggerConfigSerializer.class)
final class Target_Log4j2LoggerConfigSerializer {
}