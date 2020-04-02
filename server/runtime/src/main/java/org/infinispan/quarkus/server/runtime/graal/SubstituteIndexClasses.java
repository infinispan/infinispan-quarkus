package org.infinispan.quarkus.server.runtime.graal;

import static org.infinispan.configuration.cache.IndexingConfiguration.INDEX;

import java.util.Collections;
import java.util.Set;

import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.configuration.cache.Index;
import org.infinispan.configuration.cache.IndexingConfiguration;
import org.infinispan.configuration.cache.IndexingConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.lucene.LifecycleCallbacks;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.impl.massindex.IndexWorker;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

class SubstituteIndexClasses {
}

@TargetClass(IndexWorker.class)
final class Target_IndexWorker {
   @Substitute
   public Void apply(EmbeddedCacheManager embeddedCacheManager) {
      throw new UnsupportedOperationException("Indexing is currently disabled in native mode");
   }
}

//@TargetClass(LifecycleManager.class)
//final class Target_org_infinispan_query_impl_LifecycleManager {
//   @Substitute
//   public void cacheStarted(ComponentRegistry cr, String cacheName) {
//      // Do nothing - this method used to setup indexing parts and JMX (neither which are supported)
//   }
//
//   @Substitute
//   public void cacheStarting(ComponentRegistry cr, Configuration cfg, String cacheName) {
//      InternalCacheRegistry icr = cr.getGlobalComponentRegistry().getComponent(InternalCacheRegistry.class);
//      if (!icr.isInternalCache(cacheName) || icr.internalCacheHasFlag(cacheName, InternalCacheRegistry.Flag.QUERYABLE)) {
//         AdvancedCache<?, ?> cache = cr.getComponent(Cache.class).getAdvancedCache();
//
//         ReflectionMatcher reflectionMatcher = new ReflectionMatcher(cr.getGlobalComponentRegistry().getGlobalConfiguration().classLoader());
//
//         cr.registerComponent(reflectionMatcher, ReflectionMatcher.class);
//
//         cr.registerComponent(new QueryEngine<>(cache, false), EmbeddedQueryEngine.class);
//      }
//   }
//}

@TargetClass(LifecycleCallbacks.class)
final class Target_org_infinispan_lucene_LifecycleCallbacks {
   @Substitute
   public void cacheManagerStarting(GlobalComponentRegistry gcr, GlobalConfiguration globalCfg) {
      // Do noting - don't support lucene
   }

   @Substitute
   public static void registerSerializationContext(GlobalComponentRegistry gcr) {
      // Do noting - don't support lucene
   }
}

@TargetClass(Index.class)
final class Target_Index {
   @Substitute
   public boolean isEnabled() {
      // Indexing is always currently disabled
      return false;
   }
}

@TargetClass(IndexingConfiguration.class)
final class Target_IndexingConfiguration {
   @Substitute
   public Set<Class<?>> indexedEntities() {
      return Collections.emptySet();
   }
}

@TargetClass(IndexingConfigurationBuilder.class)
final class Target_IndexingConfigurationBuilder {
   @Alias
   private AttributeSet attributes;

   @Substitute
   public Object index(Index index) {
      if (index != Index.NONE) {
         throw new UnsupportedOperationException("Indexing is currently disabled in native mode");
      }
      attributes.attribute(INDEX).set(index);
      return this;
   }

   @Substitute
   public Object addIndexedEntity(Class<?> indexedEntity) {
      throw new UnsupportedOperationException("Indexing is currently disabled in native mode");
   }

   @Substitute
   private Set<Class<?>> indexedEntities() {
      return Collections.emptySet();
   }
}
