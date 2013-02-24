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

package au.edu.anu.datacommons.webservice;

import static java.text.MessageFormat.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.edu.anu.datacommons.data.db.dao.FedoraObjectDAOImpl;
import au.edu.anu.datacommons.data.db.model.FedoraObject;
import au.edu.anu.datacommons.data.fedora.FedoraBroker;
import au.edu.anu.datacommons.data.solr.SolrManager;
import au.edu.anu.datacommons.data.solr.SolrUtils;
import au.edu.anu.datacommons.security.service.FedoraObjectException;
import au.edu.anu.datacommons.security.service.FedoraObjectService;
import au.edu.anu.datacommons.storage.DcStorage;
import au.edu.anu.datacommons.storage.DcStorageException;
import au.edu.anu.datacommons.util.Constants;
import au.edu.anu.datacommons.util.Util;
import au.edu.anu.datacommons.webservice.bindings.Collection;
import au.edu.anu.datacommons.webservice.bindings.DcRequest;
import au.edu.anu.datacommons.webservice.bindings.FedoraItem;
import au.edu.anu.datacommons.webservice.bindings.Link;

import com.yourmediashelf.fedora.client.FedoraClientException;

@Component
@Scope("request")
@Path("/ws")
public class WebServiceResource
{
	private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceResource.class);
	private static final DcStorage dcStorage = DcStorage.getInstance();

	private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	private static DocumentBuilder docBuilder;
	private static ExecutorService threadExec;

	private static JAXBContext context;
	private static Unmarshaller um;

	@Resource(name = "fedoraObjectServiceImpl")
	private FedoraObjectService fedoraObjectService;

	static
	{
		docBuilderFactory.setNamespaceAware(true);
		try
		{
			docBuilder = docBuilderFactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			// This should never happen as we're using default Document Builder Factory. If it does, then NPE is to be expected.
			LOGGER.error(e.getMessage(), e);
			docBuilder = null;
		}

		try
		{
			ClassLoader cl = WebServiceResource.class.getClassLoader();
			context = JAXBContext.newInstance("au.edu.anu.datacommons.webservice.bindings", cl);
			um = context.createUnmarshaller();
		}
		catch (JAXBException e)
		{
			LOGGER.error(e.getMessage(), e);
			context = null;
			um = null;
		}
		
		threadExec = Executors.newSingleThreadExecutor(new ThreadFactory() {
			// Reduced priority for these threads.
			private final static int PRIORITY = (Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2;
			
			@Override
			public Thread newThread(Runnable r)
			{
				Thread newThread = new Thread(r);
				newThread.setPriority(PRIORITY);
				newThread.setDaemon(true);
				return newThread;
			}
		});
	}

	@Context
	private UriInfo uriInfo;
	@Context
	private Request request;
	@Context
	private HttpHeaders httpHeaders;

	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_PLAIN)
	public Response doGetAsXml()
	{
		Response resp = null;

		// TODO Implement method.

		resp = Response.ok("Test - Generic web service", MediaType.TEXT_PLAIN_TYPE).build();
		return resp;
	}

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@PreAuthorize("hasAnyRole('ROLE_ANU_USER', 'ROLE_REGISTERED')")
	public Response doPostAsXml(Document xmlDoc, @QueryParam("rid") Long rid)
	{
		Response resp = null;
		Document respDoc = docBuilder.newDocument();
		respDoc.appendChild(respDoc.createElement("response"));

		LOGGER.trace(getStringFromDoc(xmlDoc));
		DcRequest req;
		try
		{
			req = (DcRequest) um.unmarshal(xmlDoc);

			final FedoraItem item = req.getFedoraItem();

			// If a pid's not provided, but external IDs are, search for objects with that external ID. If found use the result's pid.
			if (item.getPid() == null)
			{
				NodeList extIdNodes = xmlDoc.getElementsByTagName("externalId");
				if (extIdNodes.getLength() > 0)
				{
					String pid = "";
					for (int i = 0; i < extIdNodes.getLength() && pid.length() == 0; i++)
						pid = getPidFromExtId(extIdNodes.item(i).getTextContent(), item.getType(), item.getOwnerGroup());

					if (pid.length() > 0)
						item.setPid(pid);
				}
			}

			if (item.getPid() == null)
			{
				// Create
				Element el = createItem(item, respDoc, rid);
				respDoc.getDocumentElement().appendChild(el);
			}
			else if (item.generateDataMap().size() > 0)
			{
				// Update
				Element el = updateItem(item, respDoc, rid);
				respDoc.getDocumentElement().appendChild(el);
			}
			else
			{
				// Query
				Element el = getDatastream(item, respDoc);
				respDoc.getDocumentElement().appendChild(el);
			}

			// If collection, add files - download or byRef.
			if (item instanceof Collection && req.getCollection().getFileUrlList() != null)
			{
				List<Link> fileUrlList = req.getCollection().getFileUrlList();
				for (Link iLink : fileUrlList)
				{
					if (iLink.isRefOnly() == null || iLink.isRefOnly() == Boolean.FALSE)
					{
						final String filename = iLink.getFilename();
						final String fileUrl = iLink.getUrl();
						Runnable downloadRunnable = new Runnable()
						{
							@Override
							public void run()
							{
								try
								{
									LOGGER.info("Beginning download of file {} from {} to add to {}...", filename, fileUrl, item.getPid());
									dcStorage.addFileToBag(item.getPid(), filename, new URL(fileUrl));
									LOGGER.info("Successfully downloaded file {} from {} and added to {}.", filename, fileUrl, item.getPid());
								}
								catch (Exception e)
								{
									LOGGER.error("Failed to download file {} from {} to add to {}.", filename, fileUrl, item.getPid());
									LOGGER.error(e.getMessage(), e);
								}
							}
						};
						threadExec.execute(downloadRunnable);
					}
					else
					{
						// Refer to the url.
						dcStorage.addExtRefs(item.getPid(), Arrays.asList(iLink.getUrl()));
					}
				}
			}

			resp = Response.ok(respDoc).build();
		}
		catch (Exception e)
		{
			respDoc.getDocumentElement().appendChild(createElement(respDoc, "status", "", new String[] { "error", e.getMessage() }));
			resp = Response.serverError().entity(respDoc).build();
		}

		return resp;
	}

	/**
	 * Creates an element with a tagName, textContent and pairs of attribute key and value pairs. For example,
	 * 
	 * <p>
	 * <code>
	 * createElement(doc, "abc", "xyz", new String[] {"key1", "val1"}, new String[] {"key2", "val2"});
	 * </code>
	 * 
	 * <p>
	 * creates
	 * 
	 * <p>
	 * &lt;abc "key1"="val1" "key2"="val2"&gt;xyz&lt;/abc&gt;
	 * 
	 * @param doc
	 *            Document the element belongs to.
	 * @param tagName
	 *            Tagname of the element.
	 * @param textContent
	 *            Text contents of the element
	 * @param attrs
	 *            Key-value pairs of attributes as String[] where index 0 is key, 1 is value.
	 * @return The created Element object.
	 */
	private Element createElement(Document doc, String tagName, String textContent, String[]... attrs)
	{
		Element el = doc.createElement(tagName);
		el.setTextContent(textContent);
		for (String[] attr : attrs)
			el.setAttribute(attr[0], attr[1]);

		return el;
	}

	private Document xmlAsDoc(String xmlStr) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db;
		Document doc = null;
		try
		{
			db = dbf.newDocumentBuilder();
			doc = db.parse(new ByteArrayInputStream(xmlStr.getBytes()));
		}
		catch (ParserConfigurationException e)
		{
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
		catch (SAXException e)
		{
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
		catch (IOException e)
		{
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
		return doc;
	}

	private String getStringFromDoc(Document doc)
	{
		StringWriter writer = new StringWriter();
		try
		{
			DOMSource domSource = new DOMSource(doc);
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			writer.flush();
		}
		catch (TransformerException ex)
		{
			ex.printStackTrace();
			return null;
		}

		return writer.toString();
	}

	@PostAuthorize("hasPermission(returnObject, 'READ')")
	private FedoraObject getFedoraObjectReadAccess(String pid)
	{
		return getFedoraObject(pid);
	}

	@PostAuthorize("hasPermission(returnObject, 'WRITE')")
	private FedoraObject getFedoraObjectWriteAccess(String pid)
	{
		return getFedoraObject(pid);
	}

	private FedoraObject getFedoraObject(String pid)
	{
		LOGGER.debug("Retrieving object for: {}", pid);
		String decodedpid = null;
		decodedpid = Util.decodeUrlEncoded(pid);
		if (decodedpid == null)
		{
			return null;
		}
		LOGGER.debug("Decoded pid: {}", decodedpid);
		FedoraObjectDAOImpl object = new FedoraObjectDAOImpl(FedoraObject.class);
		FedoraObject fo = (FedoraObject) object.getSingleByName(decodedpid);
		return fo;
	}

	private Element createItem(FedoraItem item, Document doc, Long rid) throws FedoraClientException, JAXBException
	{
		FedoraObject fo = fedoraObjectService.saveNew(item, rid);
		String pidCreated = fo.getObject_id();
		item.setPid(pidCreated);

		InputStream dataStream = null;
		Element el = createElement(doc, "status", "", new String[] { "action", "created" }, new String[] { "anudcid", pidCreated },
				new String[] { "type", item.getType() });
		try
		{
			dataStream = FedoraBroker.getDatastreamAsStream(pidCreated, Constants.XML_SOURCE);
			el.appendChild(doc.adoptNode(docBuilder.parse(dataStream).getDocumentElement()));
		}
		catch (FedoraClientException e)
		{
			// Catching exception when datastream can't be read instead of throwing it as the item's been created.
			LOGGER.warn(e.getMessage(), e);
		}
		catch (DOMException e)
		{
			LOGGER.warn(e.getMessage(), e);
		}
		catch (SAXException e)
		{
			LOGGER.warn(e.getMessage(), e);
		}
		catch (IOException e)
		{
			LOGGER.warn(e.getMessage(), e);
		}
		finally
		{
			IOUtils.closeQuietly(dataStream);
		}

		return el;
	}

	private Element updateItem(FedoraItem item, Document doc, Long rid) throws FedoraClientException, JAXBException
	{
		getFedoraObjectWriteAccess(item.getPid());
		FedoraObject fo = fedoraObjectService.saveEdit(item, rid);
		Element el = createElement(doc, "status", "", new String[] { "action", "updated" }, new String[] { "anudcid", fo.getObject_id() });
		InputStream dataStream = null;
		try
		{
			dataStream = FedoraBroker.getDatastreamAsStream(fo.getObject_id(), Constants.XML_SOURCE);
			el.appendChild(doc.adoptNode(docBuilder.parse(dataStream).getDocumentElement()));
		}
		catch (FedoraClientException e)
		{
			// Catching exception when datastream can't be read instead of throwing it as the item's been updated.
			LOGGER.warn(e.getMessage(), e);
		}
		catch (DOMException e)
		{
			LOGGER.warn(e.getMessage(), e);
		}
		catch (SAXException e)
		{
			LOGGER.warn(e.getMessage(), e);
		}
		catch (IOException e)
		{
			LOGGER.warn(e.getMessage(), e);
		}
		finally
		{
			IOUtils.closeQuietly(dataStream);
		}
		return el;
	}

	private Element getDatastream(FedoraItem item, Document doc) throws FedoraClientException, DOMException, SAXException, IOException
	{
		getFedoraObjectReadAccess(item.getPid());
		InputStream dataStream = null;
		Element el = createElement(doc, "status", "", new String[] { "action", "query" }, new String[] { "anudcid", item.getPid() });
		try
		{
			dataStream = FedoraBroker.getDatastreamAsStream(item.getPid(), Constants.XML_SOURCE);
			el.appendChild(doc.adoptNode(docBuilder.parse(dataStream).getDocumentElement()));
		}
		finally
		{
			IOUtils.closeQuietly(dataStream);
		}
		return el;
	}

	private String getPidFromExtId(String extId, String type, String ownerGroup) throws FedoraObjectException
	{
		String pid = "";

		SolrServer solrServer = SolrManager.getInstance().getSolrServer();
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery(format("unpublished.externalId:\"{0}\"", SolrUtils.escapeSpecialCharacters(extId)));
		solrQuery.addFilterQuery(format("unpublished.ownerGroup:\"{0}\"", ownerGroup));
		solrQuery.addFilterQuery(format("unpublished.type:\"{0}\"", type));
		solrQuery.addField("id");

		LOGGER.debug("Finding {} with external ID {}, belonging to ownerGroup {}...", type, extId, ownerGroup);

		try
		{
			QueryResponse queryResponse = solrServer.query(solrQuery);
			SolrDocumentList resultList = queryResponse.getResults();
			LOGGER.debug("{} {}(s) found with external ID {}, belonging to ownerGroup {}.", resultList.getNumFound(), type, extId, ownerGroup);
			if (resultList.getNumFound() == 0)
			{
				pid = "";
			}
			else if (resultList.getNumFound() == 1)
			{
				SolrDocument solrDoc = resultList.get(0);
				pid = (String) solrDoc.get("id");
			}
			else
			{
				StringBuilder pids = new StringBuilder();
				for (int i = 0; i < resultList.getNumFound(); i++)
				{
					pids.append((String) resultList.get(i).get("id"));
					if (i < resultList.getNumFound() - 1)
						pids.append(", ");
				}
				throw new FedoraObjectException(format("Multiple items of type {0} in group {1} have the external ID [{2}]: {3}", type, ownerGroup, extId,
						pids.toString()));
			}
		}
		catch (SolrServerException e)
		{
			pid = "";
		}

		return pid;
	}
}
