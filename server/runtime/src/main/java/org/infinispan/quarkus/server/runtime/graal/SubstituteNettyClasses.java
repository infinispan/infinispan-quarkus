package org.infinispan.quarkus.server.runtime.graal;

import java.util.concurrent.ExecutorService;

import org.infinispan.server.core.configuration.ProtocolServerConfiguration;
import org.infinispan.server.core.logging.Log;
import org.infinispan.server.core.transport.NettyTransport;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

public class SubstituteNettyClasses {
}

@Delete
@TargetClass(className = "org.infinispan.server.core.transport.EPollAvailable")
final class Delete_org_infinispan_server_core_transport_EPollAvailable { }

@TargetClass(NettyTransport.class)
final class Substitute_NettyTransport {
   @Alias
   static private Log log;
   @Alias
   private ProtocolServerConfiguration configuration;

   @Substitute
   private Class<? extends ServerChannel> getServerSocketChannel() {
      Class<? extends ServerChannel> channel = NioServerSocketChannel.class;
      log.createdSocketChannel(channel.getName(), configuration.toString());
      return channel;
   }

   @Substitute
   private EventLoopGroup buildEventLoop(int nThreads, DefaultThreadFactory threadFactory) {
      EventLoopGroup eventLoop = new NioEventLoopGroup(nThreads, threadFactory);
      log.createdNettyEventLoop(eventLoop.getClass().getName(), configuration.toString());
      return eventLoop;
   }
}

@Delete
@TargetClass(className = "org.infinispan.server.core.transport.EPollAvailable")
final class Delete_org_infinispan_client_hotrod_impl_transport_netty_EPollAvailable { }

@Substitute
@TargetClass(className = "org.infinispan.client.hotrod.impl.transport.netty.TransportHelper")
final class SubstituteTransportHelper {

   @Substitute
   static Class<? extends SocketChannel> socketChannel() {
      return NioSocketChannel.class;
   }

   @Substitute
   static EventLoopGroup createEventLoopGroup(int maxExecutors, ExecutorService executorService) {
      return new NioEventLoopGroup(maxExecutors, executorService);
   }
}