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

package au.edu.anu.datacommons.data.db.dao;

import java.util.List;

import au.edu.anu.datacommons.data.db.model.PublishLocation;

/**
 * PublishLocationDAO
 * 
 * Australian National University Data Commons
 * 
 * PublishLocationDAOTest
 *
 * JUnit Coverage:
 * PublishLocationDAOTest
 * 
 * <pre>
 * Version	Date		Developer				Description
 * 0.1		28/03/2013	Genevieve Turner (GT)	Initial
 * </pre>
 *
 */
public interface PublishLocationDAO extends GenericDAO<PublishLocation, Long> {
	/**
	 * getByCode
	 *
	 * Gets the Publish Location by with the given code
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		28/03/2013	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param code The code to retrieve the publish location for
	 * @return The publish location
	 */
	public PublishLocation getByCode(String code);
	
	/**
	 * getAllWithTemplates
	 * 
	 * Gets all the publish locations, and at the same time fetching their templates
	 * 
	 * @return The publish locations
	 */
	public List<PublishLocation> getAllWithTemplates();
}
