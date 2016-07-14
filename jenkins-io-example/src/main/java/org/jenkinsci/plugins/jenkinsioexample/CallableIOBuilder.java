package org.jenkinsci.plugins.jenkinsioexample;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.security.MasterToSlaveCallable;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import com.rafaelrezend.dumb.simpleio.SimpleIOExample;

import java.io.IOException;
import java.io.PrintStream;

/**
 * This plugin is an example of how to channel I/O requests to slaves without
 * relying on FilePath.
 * 
 * This builder uses an external library to write a message into a file in the
 * workspace. Since the external library is itself responsible for I/O, the
 * builder needs to handle the distribution in another layer. This is done by
 * implementing a Callable, invoked via Channel.
 * 
 * @author rafaelrezend
 * 
 */
public class CallableIOBuilder extends Builder implements SimpleBuildStep {

	/**
	 * Name of the output file.
	 */
	private static final String OUTPUT_FILE = "remoteOutput.out";

	/**
	 * Message to be written in the output file.
	 */
	private final String message;

	/**
	 * Binding to the Jenkins interface.
	 * 
	 * @param message
	 *            Message to be written in the output file.
	 */
	@DataBoundConstructor
	public CallableIOBuilder(String message) {
		this.message = message;
	}

	/**
	 * Getter used by the Jenkins stapler.
	 * 
	 * @return Message.
	 */
	public String getMessage() {
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jenkins.tasks.SimpleBuildStep#perform(hudson.model.Run,
	 * hudson.FilePath, hudson.Launcher, hudson.model.TaskListener)
	 */
	@Override
	public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher,
			TaskListener listener) throws IOException, InterruptedException {

		PrintStream logger = listener.getLogger();

		// The FilePath workspace automatically resolves to the node workspace.
		FilePath outputFile = new FilePath(workspace, OUTPUT_FILE);

		logger.println("MESSAGE: " + message);
		logger.println("OUTPUT FILE: " + OUTPUT_FILE);
		logger.println("OUTPUT FILE (NODE): " + outputFile.getRemote());

		// Use the provided channel (from launcher) to execute a Callable.
		// The WriteToNode object is created and the channel automatically
		// executes the call function.
		launcher.getChannel().call(new WriteToNode(message, outputFile));

	}

	/**
	 * Simple descriptor for CallableIOBuilder.
	 */
	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Builder> {

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		public String getDisplayName() {
			return "Write message to Node";
		}
	}

	/**
	 * Wraps the external library into a Callable static object. The extension
	 * (MasterToSlaveCallable) is chosen to ensure the direction of the request.
	 * SlaveToMasterCallable is also available, as well as
	 * MasterToSlaveFileCallable and SlaveToMasterFileCallable.
	 * 
	 * @author rafaelrezend
	 *
	 */
	private static class WriteToNode extends
			MasterToSlaveCallable<Void, IOException> {

		private static final long serialVersionUID = 1L;
		
		/**
		 * Message to be written into the output file.
		 */
		private String message;
		
		/**
		 * ABSOLUTE PATH of the file to be written. 
		 */
		private FilePath outputFile;

		/**
		 * Constructor invoked by the channel.
		 * 
		 * @param message Message.
		 * @param outputFile Output file.
		 */
		public WriteToNode(String message, FilePath outputFile) {
			this.message = message;
			this.outputFile = outputFile;
		}

		/* (non-Javadoc)
		 * @see hudson.remoting.Callable#call()
		 */
		@Override
		public Void call() throws IOException {

			// The external libs are used inside the call, so that they are executed within the selected node.
			// This class comes from an external library.
			SimpleIOExample sio = new SimpleIOExample();
			
			sio.writeToFile(
					"Distributed Jenkins: is it written in the correct node?\n"
							+ "Filepath: " + outputFile.getRemote() + "\n"
							+ "Your message: \n" + message + "\n",
					outputFile.getRemote());

			return null;
		}
	}
}
