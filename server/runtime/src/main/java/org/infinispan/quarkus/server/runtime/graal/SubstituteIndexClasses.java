package org.infinispan.quarkus.server.runtime.graal;

import static org.infinispan.configuration.cache.IndexingConfiguration.INDEX;

import java.util.Collections;
import java.util.Set;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.Index;
import org.infinispan.configuration.cache.IndexingConfiguration;
import org.infinispan.configuration.cache.IndexingConfigurationBuilder;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.objectfilter.impl.ql.PropertyPath;
import org.infinispan.objectfilter.impl.syntax.parser.ReflectionEntityNamesResolver;
import org.infinispan.quarkus.embedded.runtime.Util;
import org.infinispan.query.dsl.embedded.impl.ObjectReflectionMatcher;
import org.infinispan.query.dsl.embedded.impl.QueryEngine;
import org.infinispan.query.dsl.embedded.impl.SearchQueryMaker;
import org.infinispan.query.impl.LifecycleManager;
import org.infinispan.query.impl.massindex.IndexWorker;
import org.infinispan.registry.InternalCacheRegistry;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

//import org.infinispan.lucene.LifecycleCallbacks;

class SubstituteIndexClasses {
}

@TargetClass(IndexWorker.class)
final class Target_IndexWorker {
   @Substitute
   public Void apply(EmbeddedCacheManager embeddedCacheManager) {
      throw Util.unsupportedOperationException("Indexing");
   }
}

@TargetClass(LifecycleManager.class)
final class Target_org_infinispan_query_impl_LifecycleManager {
   @Substitute
   public void cacheStarted(ComponentRegistry cr, String cacheName) {
      // Do nothing - this method used to setup indexing parts and JMX (neither which are supported)
   }

   @Substitute
   public void cacheStarting(ComponentRegistry cr, Configuration cfg, String cacheName) {
      if (cfg.indexing().enabled()) {
         Util.unsupportedOperationException("Indexing", "Cache " + cacheName + " has it enabled!");
      }

      InternalCacheRegistry icr = cr.getGlobalComponentRegistry().getComponent(InternalCacheRegistry.class);
      if (!icr.isInternalCache(cacheName) || icr.internalCacheHasFlag(cacheName, InternalCacheRegistry.Flag.QUERYABLE)) {
         AdvancedCache<?, ?> cache = cr.getComponent(Cache.class).getAdvancedCache();
         cr.registerComponent(ObjectReflectionMatcher.create(new ReflectionEntityNamesResolver(getClass().getClassLoader()),null), ObjectReflectionMatcher.class);
         cr.registerComponent(new QueryEngine<>(cache, false), QueryEngine.class);
      }
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
         throw Util.unsupportedOperationException("Indexing");
      }
      attributes.attribute(INDEX).set(index);
      return this;
   }

   @Substitute
   public Object addIndexedEntity(Class<?> indexedEntity) {
      throw Util.unsupportedOperationException("Indexing");
   }

   @Substitute
   private Set<Class<?>> indexedEntities() {
      return Collections.emptySet();
   }
}

@TargetClass(SearchQueryMaker.class)
final class Target_SearchQueryMaker {
   @Substitute
   private boolean isMultiTermText(PropertyPath<?> propertyPath, String text) {
      return false;
   }
}
