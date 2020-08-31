package org.infinispan.cli.quarkus;

import org.infinispan.cli.commands.CLI;

import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * @author Tristan Tarrant &lt;tristan@infinispan.org&gt;
 * @since 12.0
 **/
@QuarkusMain
public class CliQuarkus {
   public static final void main(String args[]) {
      CLI.main(args);
   }
}
