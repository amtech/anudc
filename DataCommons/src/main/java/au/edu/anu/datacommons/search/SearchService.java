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

package au.edu.anu.datacommons.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import au.edu.anu.datacommons.data.db.model.Groups;
import au.edu.anu.datacommons.data.solr.SolrManager;
import au.edu.anu.datacommons.data.solr.SolrUtils;
import au.edu.anu.datacommons.properties.GlobalProps;
import au.edu.anu.datacommons.security.service.GroupService;
import au.edu.anu.datacommons.util.Util;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.view.Viewable;

/**
 * SearchService
 * 
 * Australian National University Data Commons
 * 
 * Class provides a REST service using Jersey for searching the Fedora repository
 * 
 * <pre>
 * Version	Date		Developer				Description
 * 0.1		26/03/2012	Rahul Khanna (RK)		Initial.
 * 0.2		04/05/2012	Genevieve Turner (GT)	Updated for the removal of the method 'runRiSearch'
 * 0.4		13/06/2012	Genevieve Turner (GT)	Updated for changes to use solr as the search engine
 * 0.5		13/06/2012	Genevieve Turner (GT)	Updated for varying search filters
 * 0.6		18/06/2012	Genevieve Turner (GT)	Fixed an issue where there is an error in the query if the user has no associated groups
 * 0.7		23/07/2012	Genevieve Turner (GT)	Added Solr query character escaping
 * </pre>
 * 
 */
@Component
@Path("/search")
public class SearchService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);
	private static final String SEARCH_JSP = "/search.jsp";
	
	@Resource(name="groupServiceImpl")
	GroupService groupService;
	
	/**
	 * doGetAsXml
	 * 
	 * Australian National University Data Commons
	 * 
	 * This method is called when the search service is accessed and the type requested is XML.
	 * 
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		23/03/2012	Rahul Khanna (RK)		Initial
	 * 0.2		04/05/2012	Genevieve Turner (GT)	Updated for the removal of the method 'runRiSearch'
	 * 0.4		13/06/2012	Genevieve Turner (GT)	Updated for changes to use solr as the search engine
	 * </pre>
	 * 
	 * @return XML containing search results as a Response object.
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response doGetAsXml(@QueryParam("q") String q, @QueryParam("offset") int offset, @QueryParam("limit") int limit, @QueryParam("filter") String filter)
	{
		//TODO Test this function
		Response response = null;
		
		try {
			QueryResponse queryResponse = executeQuery(q, offset, limit, filter);
			SolrDocumentList resultList = queryResponse.getResults();
			response = Response.ok(resultList).build();
		}
		catch (SolrServerException e) {
			LOGGER.error("Exception retrieving results", e);
			response = Response.status(Status.BAD_REQUEST).build();
		}
		return response;
	}

	/**
	 * doGetAsHtml
	 * 
	 * Australian National University Data Commons
	 * 
	 * This method is called when the search service is accessed and the type requested is HTML.
	 * 
	 * @return Response to display the Search JSP and passing an object to it.
	 * 
	 *         <pre>
	 * Version	Date		Developer				Description
	 * 0.1		23/03/2012	Rahul Khanna (RK)		Initial
	 * 0.2		04/05/2012	Genevieve Turner (GT)	Updated for the removal of the method 'runRiSearch'
	 * 0.3		8/05/2012	Rahul Khanna (RK)		Updated for RiSearchRequest object.
	 * 0.4		13/06/2012	Genevieve Turner (GT)	Updated for changes to use solr as the search engine
	 * </pre>
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response doGetAsHtml(@QueryParam("q") String q, @QueryParam("offset") int offset, @QueryParam("limit") int limit, @QueryParam("filter") String filter)
	{
		Response response = null;
		
		if (Util.isNotEmpty(q)) {
			Map<String, Object> model = new HashMap<String, Object>();
			try {
				LOGGER.info("User {} submitted search query [{}]", getCurUsername(), q);
				QueryResponse queryResponse = executeQuery(q, offset, limit, filter);
				
				SolrDocumentList resultList = queryResponse.getResults();
				LOGGER.trace("Number of results: {}",resultList.getNumFound());
				SolrSearchResult solrSearchResult = new SolrSearchResult(resultList);
				model.put("resultSet", solrSearchResult);
			}
			catch (SolrServerException e) {
				LOGGER.error("Exception querying solr", e);
			}
			response = Response.ok(new Viewable(SEARCH_JSP, model)).build();
		}
		else {
			response = Response.ok(new Viewable(SEARCH_JSP)).build();
		}
		return response;
	}

	/**
	 * doGetAsJson
	 * 
	 * Australian National University Data Commons
	 * 
	 * Submits a request to RI Search Service. Returns response as JSON.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		08/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @param q
	 *            Search terms.
	 * @param offset
	 *            Offset as int.
	 * @param limit
	 *            Limit as int.
	 * @param filter
	 *            Filter as String.
	 * @return HTTP OK Response with JSON string as its entity.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response doGetAsJson(@QueryParam("q") String q, @QueryParam("offset") int offset, @QueryParam("limit") int limit, @QueryParam("filter") String filter)
	{
		//TODO Test this function
		Response response = null;
		
		try {
			QueryResponse queryResponse = executeQuery(q, offset, limit, filter);
			SolrDocumentList resultList = queryResponse.getResults();
			response = Response.ok(resultList).build();
		}
		catch (SolrServerException e) {
			LOGGER.error("Exception retrieving results", e);
			response = Response.status(Status.BAD_REQUEST).build();
		}
		return response;
	}
	
	
	/**
	 * executeQuery
	 *
	 * Executes a query on the solr search engine
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.4		13/06/2012	Genevieve Turner(GT)	Initial
	 * 0.5		13/06/2012	Genevieve Turner (GT)	Updated for varying search filters
	 * 0.7		23/07/2012	Genevieve Turner (GT)	Added Solr query character escaping
	 * </pre>
	 * 
	 * @param q	Search terms
	 * @param offset Offset amount (i.e. the value to start the search from)
	 * @param limit How many rows to return
	 * @param filter Extra filters
	 * @return The response to the query
	 * @throws SolrServerException
	 */
	private QueryResponse executeQuery (String q, int offset, int limit, String filter) throws SolrServerException {
		// Remove some of Solr/Lucene's special characters
		q = SolrUtils.escapeSpecialCharacters(q);
		
		Object[] list = {q, offset, limit};
		LOGGER.trace("Query Term: {}, Offset: {}, Limit: {}", list);

		SolrQuery solrQuery = new SolrQuery();
		
		if ("team".equals(filter)) {
			setTeamQuery(solrQuery, q);
		}
		else if ("published".equals(filter)) {
			setPublishedQuery(solrQuery, q);
		}
		else {
			setAllQuery(solrQuery, q);
		}
		solrQuery.setStart(offset);
		solrQuery.setRows(limit);
		SolrServer solrServer = SolrManager.getInstance().getSolrServer();
		QueryResponse queryResponse = solrServer.query(solrQuery);
		return queryResponse;
	}

	/**
	 * setAllQuery
	 *
	 * Creates the query for where the all/no filter has been selected
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.5		13/06/2012	Genevieve Turner(GT)	Initial
	 * 0.6		18/06/2012	Genevieve Turner (GT)	Fixed an issue where there is an exception if the filterGroups is null
	 * </pre>
	 * 
	 * @param solrQuery The query that will be sent to the SolrServer
	 * @param q The values to search for
	 */
	private void setAllQuery(SolrQuery solrQuery, String q) {
		String filterGroups = getGroupsString();
		solrQuery.setQuery("published.all:(" + q + ") unpublished.all:(" + q + ")");
		
		solrQuery.addField("id");
		setReturnFields("published", solrQuery);
		setReturnFields("unpublished", solrQuery);
		if (Util.isNotEmpty(filterGroups)) {
			solrQuery.addFilterQuery("(location.published:ANU or unpublished.ownerGroup:(" + filterGroups + "))");
		}
		else {
			solrQuery.addFilterQuery("location.published:ANU");
		}
	}
	
	/**
	 * setTeamQuery
	 *
	 * Creates the query where the team filter has been selected
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.5		13/06/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param solrQuery The query that will be sent to the SolrServer
	 * @param q The values to search for
	 */
	private void setTeamQuery(SolrQuery solrQuery, String q) {

		if (groupService == null) {
			LOGGER.error("Group service is null");
		}
		else {
			String filterGroups = getGroupsString();
			solrQuery.setQuery("unpublished.all:(" + q + ")");
			
			solrQuery.addField("id");
			setReturnFields("unpublished", solrQuery);
			solrQuery.addFilterQuery("unpublished.ownerGroup:(" + filterGroups + ")");
		}
	}
	
	/**
	 * setPublishedQuery
	 *
	 * Creates the query where the published filter has been selected
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.5		13/06/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param solrQuery The query that will be sent to the SolrServer
	 * @param q The values to search for
	 */
	private void setPublishedQuery(SolrQuery solrQuery, String q) {
		solrQuery.setQuery("published.all:(" + q + ")");
		
		solrQuery.addField("id");
		setReturnFields("published", solrQuery);
		
		solrQuery.addFilterQuery("location.published:ANU");
	}
	
	/**
	 * getGroupsString
	 *
	 * Creates a string of combined groups to be used in filters
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.5		13/06/2012	Genevieve Turner (GT)	Initial
	 * </pre>
	 * 
	 * @return Returns a string of combined groups
	 */
	private String getGroupsString() {
		StringBuffer filterGroups = new StringBuffer();
		List<Groups> groups = groupService.getAll();
		for (Groups group : groups) {
			filterGroups.append(group.getId());
			filterGroups.append(" ");
		}
		LOGGER.trace("Filter Groups: {}", filterGroups.toString());
		return filterGroups.toString();
	}
	
	/**
	 * setReturnFields
	 *
	 * Sets the return fields for the solr query based upon the property
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.5		14/06/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param type The prefix of the field to search (e.g. 'published' or 'unpublished')
	 * @param solrQuery The query that will be sent to the SolrServer
	 */
	private void setReturnFields(String type, SolrQuery solrQuery) {
		String returnFields = GlobalProps.getProperty(GlobalProps.PROP_SEARCH_SOLR_RETURNFIELDS);
		String[] splitReturnFields = returnFields.split(",");
		for (String field : splitReturnFields) {
			solrQuery.addField(type + "." + field);
		}
	}
	
	private String getCurUsername() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}
}
