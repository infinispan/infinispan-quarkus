package org.infinispan.quarkus.server.runtime.graal;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.infinispan.configuration.parsing.XMLExtendedStreamReader;
import org.infinispan.server.configuration.ServerConfigurationParser;
import org.infinispan.server.configuration.security.LdapRealmConfigurationBuilder;
import org.infinispan.server.configuration.security.TrustStoreRealmConfigurationBuilder;
import org.infinispan.util.logging.Log;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

public class FixModuleClasses {
}

@Delete
@TargetClass(org.wildfly.security.manager.action.GetModuleClassLoaderAction.class)
final class Delete_orgwildflysecuritymanageractionGetModuleClassLoaderAction { }

@TargetClass(ServerConfigurationParser.class)
final class Target_ServerConfigurationParser {
   @Alias
   private static Log coreLog;

   @Substitute
   private void parseLdapRealm(XMLExtendedStreamReader reader, LdapRealmConfigurationBuilder ldapRealmConfigBuilder) throws XMLStreamException {
      coreLog.debug("LDAP Realm is not supported in native mode - ignoring element");
      // Just read until end of token
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {

      }
   }

   @Substitute
   private void parseTrustStoreRealm(XMLExtendedStreamReader reader, TrustStoreRealmConfigurationBuilder trustStoreBuilder) throws XMLStreamException {
      coreLog.debug("TrustStore Realm is not supported in native mode - ignoring element");
      // Just read until end of token
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {

      }
   }
}
