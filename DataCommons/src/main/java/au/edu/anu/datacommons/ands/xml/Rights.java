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

package au.edu.anu.datacommons.ands.xml;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;

import au.edu.anu.datacommons.validator.AtLeastOneOf;

/**
 * Rights
 * 
 * Australian National University Data Commons
 * 
 * Class for the rights element in the ANDS RIF-CS schema
 *
 * JUnit Coverage:
 * None
 * 
 * <pre>
 * Version	Date		Developer				Description
 * 0.1		15/10/2012	Genevieve Turner (GT)	Initial
 * </pre>
 *
 */
@AtLeastOneOf(fieldNames={"rightsStatement", "licence", "accessRights"}, message="Missing one of rights statement, licence or access rights")
public class Rights {
	private RightsSection rightsStatement;
	private RightsSection licence;
	private RightsSection accessRights;
	
	/**
	 * getRightsStatement
	 *
	 * Get the rights statement
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		03/10/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @return the rightsStatement
	 */
	@Valid
	@XmlElement(namespace=Constants.ANDS_RIF_CS_NS)
	public RightsSection getRightsStatement() {
		return rightsStatement;
	}
	
	/**
	 * setRightsStatement
	 *
	 * Set the rights statement/copyright information
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		03/10/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param rightsStatement the rightsStatement to set
	 */
	public void setRightsStatement(RightsSection rightsStatement) {
		this.rightsStatement = rightsStatement;
	}
	
	/**
	 * getLicence
	 *
	 * Get the licence
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		03/10/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @return the licence
	 */
	@Valid
	@XmlElement(namespace=Constants.ANDS_RIF_CS_NS)
	public RightsSection getLicence() {
		return licence;
	}
	
	/**
	 * setLicence
	 *
	 * Set the licence
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		03/10/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param licence the licence to set
	 */
	public void setLicence(RightsSection licence) {
		this.licence = licence;
	}
	
	/**
	 * getAccessRights
	 *
	 * Get the access rights
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		03/10/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @return the accessRights
	 */
	@Valid
	@XmlElement(namespace=Constants.ANDS_RIF_CS_NS)
	public RightsSection getAccessRights() {
		return accessRights;
	}
	
	/**
	 * setAccessRights
	 *
	 * Set the access rights
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		03/10/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param accessRights the accessRights to set
	 */
	public void setAccessRights(RightsSection accessRights) {
		this.accessRights = accessRights;
	}
}
