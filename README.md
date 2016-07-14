# Jenkins plugin example: Callable I/O

This Jenkins plugin is an example of how to channel I/O requests to slaves without relying on FilePath.

This builder uses an external library to write a message into a file in the workspace. Since the external library is itself responsible for I/O, the builder needs to handle the distribution in another layer. This is done by implementing a Callable, invoked via Channel.

The purpose of this example is exactly to demonstrate how to workaround cases where the I/O cannot be managed by the Jenkins plugin itself.

## SimpleIOExample

Simple application that gets a message as input and writes it to a file. Built with Maven.

## jenkins-io-example

Actual Jenkins plugin that uses the ***SimpleIOExample*** above.
