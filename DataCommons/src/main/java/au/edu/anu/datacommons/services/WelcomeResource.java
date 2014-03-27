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
package au.edu.anu.datacommons.services;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import au.edu.anu.datacommons.data.solr.dao.SolrSearchDAO;
import au.edu.anu.datacommons.data.solr.model.SolrSearchResult;
import au.edu.anu.datacommons.exception.DataCommonsException;

import com.sun.jersey.api.view.Viewable;

/**
 * WelcomeResource
 *
 * Australian National University Data Commons
 * 
 * Resource for the home page
 *
 * JUnit coverage:
 * None
 * 
 * @author Genevieve Turner
 *
 */
@Path("/welcome")
@Component
public class WelcomeResource {
	static final Logger LOGGER = LoggerFactory.getLogger(WelcomeResource.class);
	
	@Resource(name="solrSearchDAOImpl")
	SolrSearchDAO solrSearch;
	
	/**
	 * Get the welcome page
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response getWelcomePage() {
		LOGGER.info("In Welcome Resource");
		Map<String, Object> model = new HashMap<String, Object>();
		
		if (solrSearch != null) {
			try {
				SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if (auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
					LOGGER.debug("Authenticated Username: {}", auth.getName());
					SolrSearchResult searchResult = solrSearch.executeSearch("*", 0, 10, "team", "_docid_", ORDER.desc);
					model.put("resultSet", searchResult);
				}
			}
			catch (SolrServerException e) {
				LOGGER.error("Error executing search", e);
				throw new DataCommonsException(Status.INTERNAL_SERVER_ERROR, "A problem was encountered executing search");
			}
		}
		else {
			LOGGER.error("SolrSearch is null");
		}
		
		return Response.ok(new Viewable("/welcome.jsp", model)).build();
	}
}
