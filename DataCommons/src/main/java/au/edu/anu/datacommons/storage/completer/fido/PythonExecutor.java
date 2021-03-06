/*******************************************************************************
 * Australian National University Data Commons
 * Copyright (C) 2013  The Australian National University
 * 
 * This file is part of Australian National University Data Commons.
 * 
 * Australian National University Data Commons is free software: you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package au.edu.anu.datacommons.storage.completer.fido;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.anu.datacommons.config.Config;
import au.edu.anu.datacommons.properties.GlobalProps;

/**
 * This class allows for execution of a Python script, passing specified parameters to it, send data through standard
 * input stream (STDIN) and retrieve the output from the Standard Output (STDOUT)
 * 
 * @author Rahul Khanna
 * 
 */
public class PythonExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(PythonExecutor.class);

	private Process pythonProcess;
	private final List<String> cmdLine = new ArrayList<String>();
	
	private String stdoutStr = null;
	private String stderrStr = null;

	/**
	 * Instantiates a new python executor for a specified Python script with command line arguments for the Python
	 * interpreter.
	 * 
	 * @param pythonScript
	 *            the python script
	 * @param cmdParams
	 *            the python switches
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public PythonExecutor(List<String> cmdParams) throws IOException {
		cmdLine.add(getPythonPath());
		if (cmdParams != null) {
			cmdLine.addAll(cmdParams);
		}
	}

	/**
	 * Executes the python process.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Process execute() throws IOException {
		if (LOGGER.isDebugEnabled()) {
			StringBuilder cmdLineAsStr = new StringBuilder();
			for (String cmdArg : cmdLine) {
				if (cmdArg.contains(" ")) {
					cmdLineAsStr.append("\"");
				}
				cmdLineAsStr.append(cmdArg);
				if (cmdArg.contains(" ")) {
					cmdLineAsStr.append("\"");
				}
				cmdLineAsStr.append(" ");
			}
			LOGGER.trace("Executing: {}", cmdLineAsStr.toString().trim());
		}
		ProcessBuilder pb = new ProcessBuilder(cmdLine);
		pythonProcess = pb.start();
		return pythonProcess;
	}

	/**
	 * Sends bytes from an InputStream to the script's Stdin.
	 * 
	 * @param inStream
	 *            InputStream from which data will be read
	 * @throws IOException
	 *             when unable to read from input stream or write to StdIn
	 */
	public void sendStreamToStdIn(InputStream inStream) throws IOException {
		OutputStream outStream = pythonProcess.getOutputStream();
		byte buffer[] = new byte[2 * 1024];
		int numBytesRead = 0;

		try {
			while ((numBytesRead = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, numBytesRead);
			}
			outStream.flush();
		} finally {
			IOUtils.closeQuietly(inStream);
			IOUtils.closeQuietly(outStream);
		}
	}

	/**
	 * Gets the StdOut as an InputStream from which data can be read.
	 * 
	 * @return StdOut as InputStream
	 */
	protected InputStream getStdOutAsInputStream() {
		return pythonProcess.getInputStream();
	}

	protected InputStream getStdErrAsInputStream() {
		return pythonProcess.getErrorStream();
	}
	
	/**
	 * Gets the StdIn stream as OutputStream to which data can be written.
	 * 
	 * @return StdIn as OutputStream
	 */
	protected OutputStream getStdInAsOutputStream() {
		return pythonProcess.getOutputStream();
	}
	

	/**
	 * Returns the output data of the process.
	 * 
	 * @return StdOut output of the process as String
	 * @throws IOException
	 */
	public String getOutputAsString() throws IOException {
		try {
			pythonProcess.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (stdoutStr == null) {
			stdoutStr = streamToString(getStdOutAsInputStream());
		}
		return stdoutStr;
	}

	/**
	 * Returns the error data of the process.
	 * 
	 * @return StdErr output of the process as String
	 * @throws IOException
	 */
	public String getErrorAsString() throws IOException {
		try {
			pythonProcess.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (stderrStr == null) {
			stderrStr = streamToString(getStdErrAsInputStream());
		}
		return stderrStr;
	}
	
	private String streamToString(InputStream is) throws IOException {
		StringBuilder output = new StringBuilder();
		BufferedReader stdout = new BufferedReader(new InputStreamReader(is));
		for (String str = stdout.readLine(); str != null; str = stdout.readLine()) {
			output.append(str);
			output.append(Config.NEWLINE);
		}
		if (output.length() != 0) {
			return output.substring(0, output.lastIndexOf(Config.NEWLINE));
		} else
			return output.toString();
	}

	/**
	 * Reads the location of the python executable from fido.properties file.
	 * 
	 * @return Path to Python executable as String
	 */
	protected String getPythonPath() {
		String pythonPath = GlobalProps.getPythonPath();
		LOGGER.trace("Using Python binary {}", pythonPath);
		return pythonPath;
	}
}
