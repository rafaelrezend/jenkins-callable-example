package com.rafaelrezend.dumb.simpleio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SimpleIOExample {

	public static void main(String[] args) throws IOException {

		StringBuffer message = new StringBuffer();
		
		if (args.length > 0)
			for (String s: args)
	            message.append(s + " ");
		else
			message = new StringBuffer("Default message");
		
		message.append("\n");

		SimpleIOExample io = new SimpleIOExample();
		
		io.writeToFile(message.toString(), "writeToFile.out");
		
	}
	
	public void writeToFile (String message, String filepath) throws IOException {
		
		File file = new File(filepath);

		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
		BufferedWriter buffWriter = new BufferedWriter(fileWriter);
		buffWriter.write(message);
		buffWriter.close();
	}

}
