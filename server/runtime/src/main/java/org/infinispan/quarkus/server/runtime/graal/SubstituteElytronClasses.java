package org.infinispan.quarkus.server.runtime.graal;

import java.util.Properties;

import org.infinispan.server.configuration.security.LdapRealmConfiguration;
import org.infinispan.server.configuration.security.RealmConfiguration;
import org.infinispan.server.configuration.security.SecurityConfiguration;
import org.wildfly.security.auth.realm.ldap.LdapSecurityRealmBuilder;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.auth.server.SecurityRealm;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

public class SubstituteElytronClasses {
}

@TargetClass(LdapRealmConfiguration.class)
final class Target_LdapRealmConfiguration {
   @Substitute
   public SecurityRealm build(SecurityConfiguration security, RealmConfiguration realm, SecurityDomain.Builder domainBuilder, Properties properties) {
      return LdapSecurityRealmBuilder.builder().build();
   }
}
