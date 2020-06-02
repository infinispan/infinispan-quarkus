package org.infinispan.quarkus.server.runtime.graal;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.infinispan.server.context.ServerInitialContextFactoryBuilder;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

public class SubstituteDnsLookup {
}

@TargetClass(ServerInitialContextFactoryBuilder.class)
final class Target_ServerInitialContextFactoryBuilder {
   @Substitute
   public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException {
         throw new NamingException("Native Infinispan Server does not support JNDI!");
   }
}