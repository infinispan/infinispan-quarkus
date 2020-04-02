package org.infinispan.quarkus.server.deployment;

import java.util.Arrays;
import java.util.Collection;

import org.infinispan.configuration.internal.PrivateGlobalConfigurationBuilder;
import org.infinispan.protostream.WrappedMessage;
import org.infinispan.quarkus.server.runtime.InfinispanServerProducer;
import org.infinispan.quarkus.server.runtime.InfinispanServerRecorder;
import org.infinispan.quarkus.server.runtime.InfinispanServerRuntimeConfig;
import org.infinispan.query.affinity.AffinityIndexManager;
import org.infinispan.query.affinity.ShardAllocationManagerImpl;
import org.infinispan.rest.RestServer;
import org.infinispan.server.configuration.ServerConfigurationBuilder;
import org.infinispan.server.core.configuration.ProtocolServerConfigurationBuilder;
import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.memcached.MemcachedServer;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jgroups.protocols.SASL;

import com.thoughtworks.xstream.security.NoTypePermission;

import io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageSystemPropertyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.infinispan.embedded.deployment.InfinispanReflectionExcludedBuildItem;

class InfinispanServerProcessor {
   private static final String FEATURE_NAME = "infinispan-server";
   @BuildStep
   void setSystemProperties(BuildProducer<NativeImageSystemPropertyBuildItem> buildSystemProperties) {
      // We disable the replacement of JdkSslContext in the NettyExtensions - this shouldn't be needed once we move to Java 11
      buildSystemProperties.produce(new NativeImageSystemPropertyBuildItem("substratevm.replacement.jdksslcontext", "false"));
   }

   @BuildStep
   void extensionFeatureStuff(BuildProducer<FeatureBuildItem> feature, BuildProducer<AdditionalBeanBuildItem> additionalBeans,
         BuildProducer<IndexDependencyBuildItem> indexedDependencies, BuildProducer<ExtensionSslNativeSupportBuildItem> sslNativeSupport) {
      feature.produce(new FeatureBuildItem(FEATURE_NAME));
      additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(InfinispanServerProducer.class));
      sslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem(FEATURE_NAME));

      for (String infinispanArtifact : Arrays.asList("infinispan-server-runtime", "infinispan-server-hotrod",
            "infinispan-server-core", "infinispan-server-rest", "infinispan-remote-query-server", "infinispan-query",
            // Is there a reason remote-query-client is in dependency tree??
            "infinispan-objectfilter", "infinispan-query-dsl", "infinispan-remote-query-client", "infinispan-server-memcached",
            // Why is client-hotrod in dependency tree??
            "infinispan-server-router", "infinispan-client-hotrod", "infinispan-cachestore-jdbc", "infinispan-cachestore-rocksdb",
            "infinispan-cachestore-remote", "infinispan-persistence-soft-index", "infinispan-clustered-counter")) {
         indexedDependencies.produce(new IndexDependencyBuildItem("org.infinispan", infinispanArtifact));
      }
   }

   @BuildStep
   void addExcludedClassesFromReflection(BuildProducer<InfinispanReflectionExcludedBuildItem> excludedClasses) {
      // Don't support SASL in JGroups yet - need to fix elytron
      excludedClasses.produce(new InfinispanReflectionExcludedBuildItem(DotName.createSimple(SASL.class.getName())));
      // We don't support Indexing so don't these to reflection
      excludedClasses.produce(new InfinispanReflectionExcludedBuildItem(DotName.createSimple(AffinityIndexManager.class.getName())));
      excludedClasses.produce(new InfinispanReflectionExcludedBuildItem(DotName.createSimple(ShardAllocationManagerImpl.class.getName())));
      excludedClasses.produce(new InfinispanReflectionExcludedBuildItem(DotName.createSimple("org.infinispan.query.indexmanager.ClusteredSwitchingBackend")));
      excludedClasses.produce(new InfinispanReflectionExcludedBuildItem(DotName.createSimple("org.infinispan.query.backend.SearchFactoryHandler$CacheListener")));
      // This class is used by JBossMarshalling so we don't need
      excludedClasses.produce(new InfinispanReflectionExcludedBuildItem(DotName.createSimple("org.infinispan.persistence.remote.upgrade.MigrationTask$RemoveListener")));

      // TODO: exclude all the TerminalFunctions SerializeWith references
   }

   @Record(ExecutionTime.RUNTIME_INIT)
   @BuildStep
   void configureRuntimeProperties(InfinispanServerRecorder recorder, InfinispanServerRuntimeConfig infinispanServerRuntimeConfig) {
      recorder.configureRuntimeProperties(infinispanServerRuntimeConfig);
   }

   @BuildStep
   void addRuntimeInitializedClasses(BuildProducer<RuntimeInitializedClassBuildItem> runtimeInitialized) {
      runtimeInitialized.produce(new RuntimeInitializedClassBuildItem(CleartextHttp2ServerUpgradeHandler.class.getName()));
      runtimeInitialized.produce(new RuntimeInitializedClassBuildItem(Http2ServerUpgradeCodec.class.getName()));
   }

   @BuildStep
   void addReflectionAndResources(BuildProducer<ReflectiveClassBuildItem> reflectionClass,
         BuildProducer<NativeImageResourceBuildItem> resources, CombinedIndexBuildItem combinedIndexBuildItem) {

      reflectionClass.produce(new ReflectiveClassBuildItem(false, false, PrivateGlobalConfigurationBuilder.class.getName()));
      reflectionClass.produce(new ReflectiveClassBuildItem(false, false, ServerConfigurationBuilder.class.getName()));

      // Add the various protocol server implementations
      reflectionClass.produce(new ReflectiveClassBuildItem(false, false, HotRodServer.class));
      reflectionClass.produce(new ReflectiveClassBuildItem(false, false, MemcachedServer.class));
      reflectionClass.produce(new ReflectiveClassBuildItem(false, false, RestServer.class));

      IndexView combinedIndex = combinedIndexBuildItem.getIndex();
      addReflectionForClass(ProtocolServerConfigurationBuilder.class, false, combinedIndex, reflectionClass);

      // TODO: not sure why this is required for native runtime...
      reflectionClass.produce(new ReflectiveClassBuildItem(false, false, NoTypePermission.class.getName()));

      resources.produce(new NativeImageResourceBuildItem("infinispan-defaults.xml",
            "proto/generated/persistence.rocksdb.proto",
            "proto/generated/persistence.counters.proto",
            "proto/generated/persistence.query.proto",
            "proto/generated/persistence.remote_query.proto",
            "proto/generated/persistence.memcached.proto",
            "proto/generated/persistence.event_logger.proto",
            "proto/generated/persistence.multimap.proto",
            "proto/persistence.m.event_logger.proto",
            "proto/generated/persistence.server.core.proto",
            "proto/generated/persistence.scripting.proto",
            "org/infinispan/query/remote/client/query.proto",
            WrappedMessage.PROTO_FILE
      ));
   }

   private void addReflectionForClass(Class<?> classToUse, boolean isInterface, IndexView indexView,
         BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
      addReflectionForName(classToUse.getName(), isInterface, indexView, reflectiveClass, false, false);
   }

   private void addReflectionForName(String className, boolean isInterface, IndexView indexView,
         BuildProducer<ReflectiveClassBuildItem> reflectiveClass, boolean methods, boolean fields) {
      Collection<ClassInfo> classInfos;
      if (isInterface) {
         classInfos = indexView.getAllKnownImplementors(DotName.createSimple(className));
      } else {
         classInfos = indexView.getAllKnownSubclasses(DotName.createSimple(className));
      }

      if (!classInfos.isEmpty()) {
         reflectiveClass.produce(new ReflectiveClassBuildItem(methods, fields,
               classInfos.stream().map(ClassInfo::toString).toArray(String[]::new)));
      }
   }
}
