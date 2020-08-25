package org.infinispan.quarkus.server.runtime.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.logging.log4j.core.jmx.Server;

@TargetClass(Server.class)
final class SubstituteJmxServer {

    @Substitute
    public static void unregisterLoggerContext(final String loggerContextName) {
        // No-op
    }

    @Substitute
    public static void reregisterMBeansAfterReconfigure() {
        // No-op
    }

    @Substitute
    public static void unregisterMBeans() {
        // No-op
    }

}

public class SubstituteLog4jClasses
{
}
