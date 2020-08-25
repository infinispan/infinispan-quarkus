package org.infinispan.quarkus.server.runtime.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.infinispan.rest.resources.MetricsResource;

@TargetClass(MetricsResource.class)
final class SubstituteMetricsResource {

    @Substitute
    private void registerBaseMetrics() {
        // No-op
    }

}

public class SubstituteMetrics
{
}
