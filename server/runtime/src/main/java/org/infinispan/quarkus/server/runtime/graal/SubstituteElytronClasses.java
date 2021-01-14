package org.infinispan.quarkus.server.runtime.graal;

import static java.security.AccessController.doPrivileged;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.Key;
import java.security.PrivilegedAction;
import java.util.function.UnaryOperator;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

public class SubstituteElytronClasses {
}

@TargetClass(className = "org.wildfly.security.key.KeyUtil$KeyClonerCreator")
final class Target_org_wildfly_security_key_KeyUtil_KeyClonerCreator {
   @Substitute
   private UnaryOperator<Key> checkForCloneMethod(final Class<?> declType, final Class<?> returnType) {
      final Method method = doPrivileged(new PrivilegedAction<Method>() {
         @Override
         public Method run() {
            try {
               Method cloneMethod = declType.getDeclaredMethod("clone");
               if (cloneMethod.getReturnType() == returnType)
                  return cloneMethod;

               return null;
            } catch (NoSuchMethodException e) {
               return null;
            }
         }
      });

      if (method == null)
         return null;

      return new UnaryOperator<Key>() {
         @Override
         public Key apply(Key key) {
            try {
               return (Key) method.invoke(key);
            } catch (RuntimeException | Error e) {
               throw e;
            } catch (Throwable throwable) {
               throw new UndeclaredThrowableException(throwable);
            }
         }
      };
   }

   @Substitute
   private UnaryOperator<Key> checkForCopyCtor(final Class<?> declType, final Class<?> paramType) {
      final Constructor<?> constructor = doPrivileged(new PrivilegedAction<Constructor<?>>() {
         @Override
         public Constructor<?> run() {
            try {
               return declType.getDeclaredConstructor(paramType);
            } catch (NoSuchMethodException e) {
               return null;
            }
         }
      });

      if (constructor == null)
         return null;

      return new UnaryOperator<Key>() {
         @Override
         public Key apply(Key key) {
            try {
               return (Key) constructor.newInstance(key);
            } catch (RuntimeException | Error e) {
               throw e;
            } catch (Throwable throwable) {
               throw new UndeclaredThrowableException(throwable);
            }
         }
      };
   }
}
