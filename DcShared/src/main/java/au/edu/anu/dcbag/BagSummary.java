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

package au.edu.anu.dcbag;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagInfoTxt;
import au.edu.anu.dcbag.BagPropsTxt.DataSource;
import au.edu.anu.dcbag.BagPropsTxt.Key;

/**
 * Represents the summary of a bag's contents. This class contains the following information about a bag:
 * 
 * <li>
 * <ul>FileSummaryMap - FileSummary for each BagFile in the Bag this BagSummary represents</ul>
 * <ul>Bag Properties - Contains Data Commons specific attributes of a Bag</ul>
 * <ul>Bag Info - Contains the Pid this bag belongs to, the bagging date and bag size</ul>
 * </li>
 */
public class BagSummary
{
	private final Bag bag;
	private BagPropsTxt bagPropsTxt = null;
	private FileSummaryMap fsMap = null;
	private BagInfoTxt bagInfoTxt = null;
	private ExtRefsTxt extRefsTxt = null;
	
	/**
	 * Instantiates a new bag summary.
	 * 
	 * @param bag
	 *            the bag whose summary is to be read
	 */
	public BagSummary(Bag bag)
	{
		this.bag = bag;
		
		// Read bag properties file.
		BagFile bagPropsTxtFile = bag.getBagFile(BagPropsTxt.FILEPATH);
		if (bagPropsTxtFile != null)
			this.bagPropsTxt = new BagPropsTxt(BagPropsTxt.FILEPATH, bagPropsTxtFile, bag.getBagItTxt().getCharacterEncoding());
		
		// File summary map.
		this.fsMap = new FileSummaryMap(this.bag);
		
		// Bag Info Txt
		this.bagInfoTxt = bag.getBagInfoTxt();
		
		// External references
		BagFile extRefsFile = bag.getBagFile(ExtRefsTxt.FILEPATH);
		if (extRefsFile != null)
			this.extRefsTxt = new ExtRefsTxt(ExtRefsTxt.FILEPATH, extRefsFile, bag.getBagItTxt().getCharacterEncoding());
	}
	
	/**
	 * Gets the friendly size of this bag. For example, <code>2 MB</code>, <code>257 KB</code>
	 * 
	 * @return the friendly size
	 */
	public String getFriendlySize()
	{
		return bag.getBagInfoTxt().getBagSize();
	}
	
	/**
	 * Gets the number of payload files in this bag.
	 * 
	 * @return the number of payload files as long
	 */
	public long getNumFiles()
	{
		return bag.getPayload().size();
	}
	
	/**
	 * Gets the pid of the record this bag belongs to.
	 * 
	 * @return the pid of record
	 */
	public String getPid()
	{
		return bag.getBagInfoTxt().getExternalIdentifier();
	}
	
	/**
	 * Gets the data source attribute specified for this bag. If none is specified, the value <code>general</code> is returned.
	 * 
	 * @return DataSource
	 */
	public DataSource getDataSource()
	{
		// If the Bag properites file exists, read the data source. If data source not specified, set GENERAL.
		DataSource dataSource = null;
		if (this.bagPropsTxt != null)
		{
			String value = bagPropsTxt.get(Key.DATASOURCE.toString());
			if (value == null || value.length() == 0)
				dataSource = DataSource.GENERAL;
			dataSource = DataSource.getValueOf(value);
		}
		
		return dataSource;
	}
	
	/**
	 * Gets the FileSummaryMap containing FileSummary for each BagFile in this bag.
	 * 
	 * @return FileSummaryMap
	 */
	public FileSummaryMap getFileSummaryMap()
	{
		return fsMap;
	}

	/**
	 * Gets the bag info txt.
	 * 
	 * @return the bag info txt
	 */
	public BagInfoTxt getBagInfoTxt()
	{
		return bagInfoTxt;
	}

	/**
	 * Gets the external references tag file containing links to external resources.
	 * 
	 * @return ExtRefsTxt
	 */
	public ExtRefsTxt getExtRefsTxt()
	{
		return extRefsTxt;
	}
}
