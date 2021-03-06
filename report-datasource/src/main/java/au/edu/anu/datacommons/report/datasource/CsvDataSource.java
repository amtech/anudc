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
package au.edu.anu.datacommons.report.datasource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRCsvDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CsvDataSource
 *
 * Australian National University Data Commons
 * 
 * Data source for CSV files.  This has been created so that the field delimiter can be
 * specified in the constructor.
 *
 * JUnit coverage:
 * None
 * 
 * @author Genevieve Turner
 *
 */
public class CsvDataSource extends JRCsvDataSource {
	static final Logger LOGGER = LoggerFactory.getLogger(CsvDataSource.class);
	
	/**
	 * Constructor
	 * 
	 * @param location The location of the CSV file
	 * @param fieldDelimiter The field delimiter for the CSV file
	 * @throws JRException
	 */
	public CsvDataSource(String location, char fieldDelimiter) throws JRException {
		super(location);
		LOGGER.trace("Instantiating CsvDataSource with file location '{}' and delimiter '{}'", location, fieldDelimiter);
		setFieldDelimiter(fieldDelimiter);
	}
}
