package org.infininspan.quarkus.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.kohsuke.MetaInfServices;

import io.quarkus.test.common.IntegrationTestStartedNotifier;

@MetaInfServices
public class IntegrationStartNotifier implements IntegrationTestStartedNotifier {
   @Override
   public Result check(Context context) {
      System.out.println("Checking log file for existence of Infinispan Server start up messaage");
      try (BufferedReader reader = new BufferedReader(new FileReader(context.logFile().toFile()))) {
         String line;
         String previousLine = null;
         while ((line = reader.readLine()) != null) {
            if (line.contains("ISPN080001: Infinispan Server")) {
               return new Result() {
                  @Override
                  public boolean isStarted() {
                     return true;
                  }

                  @Override
                  public boolean isSsl() {
                     // Assume our tests are not ssl
                     return false;
                  }
               };
            }
            previousLine = line;
         }
         System.out.println("Not started yet, last line was: " + previousLine);
         return null;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
