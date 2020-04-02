package org.infinispan.quarkus.server.runtime.graal;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.util.Util;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.container.impl.InternalEntryFactory;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.persistence.remote.LifecycleCallbacks;
import org.infinispan.persistence.remote.RemoteStore;
import org.infinispan.persistence.remote.configuration.RemoteStoreConfiguration;
import org.infinispan.persistence.remote.upgrade.MigrationTask;
import org.infinispan.persistence.remote.wrapper.HotRodEntryMarshaller;
import org.infinispan.persistence.spi.InitializationContext;
import org.infinispan.persistence.spi.PersistenceException;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.AlwaysInline;
import com.oracle.svm.core.annotate.AnnotateOriginal;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

// We need to remove all the MigrationTask and related classes as they load up JBoss Marshalling
public class SubstituteJBossMarshallingClasses {
}

@TargetClass(MigrationTask.class)
final class Target_MigrationTask {
   @Substitute
   public Integer apply(EmbeddedCacheManager embeddedCacheManager) {
      throw new UnsupportedOperationException("Migration Task is not supported in native runtime!");
   }
}

@TargetClass(LifecycleCallbacks.class)
final class Target_org_infinispan_persistence_remote_LifecycleCallbacks {
   @Substitute
   public void cacheManagerStarting(GlobalComponentRegistry gcr, GlobalConfiguration globalCfg) { }
}

@TargetClass(RemoteStore.class)
final class Target_RemoteStore {
   @Alias
   private RemoteStoreConfiguration configuration;
   @Alias
   private RemoteCacheManager remoteCacheManager;
   @Alias
   private RemoteCache<Object, Object> remoteCache;
   @Alias
   private InternalEntryFactory iceFactory;
   @Alias
   protected InitializationContext ctx;

   @Alias
   private ConfigurationBuilder buildRemoteConfiguration(RemoteStoreConfiguration configuration, Marshaller marshaller) {
      return null;
   }

   @Substitute
   public void start() throws PersistenceException {
      final Marshaller marshaller;
      if (configuration.marshaller() != null) {
         marshaller = Util.getInstance(configuration.marshaller(), ctx.getCache().getAdvancedCache().getClassLoader());
      } else if (configuration.hotRodWrapping()) {
         marshaller = new HotRodEntryMarshaller(ctx.getByteBufferFactory());
      } else if (configuration.rawValues()) {
         // Raw values required JBossMarshaller
         throw new UnsupportedOperationException("Raw values is not supported in native mode");
      } else {
         marshaller = ctx.getPersistenceMarshaller();
      }
      ConfigurationBuilder builder = buildRemoteConfiguration(configuration, marshaller);
      remoteCacheManager = new RemoteCacheManager(builder.build());

      if (configuration.remoteCacheName().isEmpty())
         remoteCache = remoteCacheManager.getCache();
      else
         remoteCache = remoteCacheManager.getCache(configuration.remoteCacheName());
      if (configuration.rawValues() && iceFactory == null) {
         iceFactory = ctx.getCache().getAdvancedCache().getComponentRegistry().getComponent(InternalEntryFactory.class);
      }
   }
}