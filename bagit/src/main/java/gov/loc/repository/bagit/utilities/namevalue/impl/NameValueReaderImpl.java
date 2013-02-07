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

package gov.loc.repository.bagit.utilities.namevalue.impl;

import gov.loc.repository.bagit.utilities.namevalue.NameValueReader;

import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NameValueReaderImpl implements NameValueReader {

	private static final Log log = LogFactory.getLog(NameValueReaderImpl.class);
	
	private Deque<String> lines = new ArrayDeque<String>();
	private String type;
	
	public NameValueReaderImpl(String encoding, InputStream in, String type) {
		this.type = type;

		InputStreamReader fr = null;
		BufferedReader reader = null;
		try
		{
			// Replaced FileReader with InputStreamReader since all bagit manifest and metadata files must be UTF-8
			// encoded.  If UTF-8 is not explicitly set then data will be read in default native encoding.
			fr = new InputStreamReader(in, encoding);
			reader = new BufferedReader(fr);
			String line = reader.readLine();
			while(line != null) {
				lines.addLast(line);
				line = reader.readLine();
			}
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
		finally {
			IOUtils.closeQuietly(fr);
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(in);
		}
	}
		
	public boolean hasNext() {
		if (lines.isEmpty()) {
			return false;
		}
		return true;
	}	
		
	public NameValue next() {
		if (! this.hasNext()) {
			throw new NoSuchElementException();
		}
		//Split the first line
		String line = this.lines.removeFirst();
		String[] splitString = line.split(" *: *", 2);
		String name = splitString[0];
		String value = null;
		if (splitString.length == 2) {
			value = splitString[1].trim();
			while(! this.lines.isEmpty() && this.lines.getFirst().matches("^( |\\t)+.+$")) {
				value += " " + this.lines.removeFirst().replaceAll("^( |\\t)+", "");
			}			
		}
		NameValue ret = new NameValue(name, value);
		log.debug(MessageFormat.format("Read from {0}: {1}", this.type, ret.toString()));
		return ret;
		
	}
	
	public void remove() {
		throw new UnsupportedOperationException();		
	}

}
