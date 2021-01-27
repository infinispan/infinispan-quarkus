# Infinispan Native CLI
The Infinispan Command Line Interface (CLI) connects to Infinispan Server clusters so you can access data remotely and perform administrative operations.

You can find complete documentation for the CLI, in our [CLI User Guide](https://infinispan.org/docs/stable/titles/cli/cli.html).

## Getting Started

#### Linux and macOS
Start the CLI and connect to a running Infinispan Server as follows:
```bash
./ispn-cli -c http://<host>:11222
```

####  Microsoft Windows
Start the CLI with PowerShell and connect to a running Infinispan Server as follows:
```bash
& ".\ispn-cli.exe" -c http://<host>:11222
```

## Installing the CLI as a kubectl Plugin
You can use the Infinispan CLI as `kubectl` client plugin, which enables the CLI to control the Infinispan Operator and simplifies many operations.

Integrating with `kubectl` lets you install and remove the Infinispan Operator, create and delete Infinispan clusters, and obtain information about various resources.

To install the CLI as a `kubectl` plugin, do the following:
1. Copy the `ispn-cli`, or create a hard link, to a file named "kubectl-infinispan".
2. Add `kubectl-infinispan` to your `PATH`.
3. Run `kubectl plugin list` to verify the CLI is added as a plugin.
4. Run `kubectl infinispan --help` to start using the CLI plugin.

For more detailed instructions, please see the [kubectl docs](https://kubernetes.io/docs/tasks/extend-kubectl/kubectl-plugins/#installing-kubectl-plugins).

#### Installing Infinispan Operator
```
kubectl infinispan install
```

#### Creating  Infinispan Clusters
```
kubectl infinispan create --replicas=2 myinfinispan
kubectl infinispan get
```

#### Removing Clusters
```
kubectl infinispan delete myinfinispan
kubectl infinispan get
```

#### Removing the Operator
```
kubectl infinispan uninstall
```

## License

The Infinispan CLI is licensed under the Apache Public License 2.0. The source code for this
program is [located on github](https://github.com/infinispan/infinispan).
