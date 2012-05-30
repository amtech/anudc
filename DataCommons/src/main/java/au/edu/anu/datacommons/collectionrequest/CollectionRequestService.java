package au.edu.anu.datacommons.collectionrequest;

import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.FetchTxt.FilenameSizeUrl;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.PropertyException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.context.SecurityContextHolder;

import au.edu.anu.datacommons.collectionrequest.CollectionRequestStatus.ReqStatus;
import au.edu.anu.datacommons.collectionrequest.PageMessages.MessageType;
import au.edu.anu.datacommons.data.db.dao.GenericDAOImpl;
import au.edu.anu.datacommons.data.db.dao.UsersDAOImpl;
import au.edu.anu.datacommons.data.db.model.Users;
import au.edu.anu.datacommons.data.fedora.FedoraBroker;
import au.edu.anu.datacommons.persistence.HibernateUtil;
import au.edu.anu.datacommons.properties.GlobalProps;
import au.edu.anu.datacommons.upload.DcBag;
import au.edu.anu.datacommons.upload.UploadService;
import au.edu.anu.datacommons.util.Util;

import com.sun.jersey.api.view.Viewable;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.generated.access.DatastreamType;

/**
 * CollectionRequestService
 * 
 * Australian National University Data Commons
 * 
 * This method serves REST requests related to Collection Requests. The following broad workflows are supported:
 * 
 * <ol>
 * <li>Submitting a Collection Request that includes the specific items (datastreams) requested and the answers to questions assigned to a collection (pid).</li>
 * <li>Changing the status of a Collection Request providing a reason for the change.</li>
 * <li>When a Collection Request is approved, providing access to the created dropbox.</li>
 * <li>Allow for questions to be added to the Question Bank.</li>
 * <li>Allow questions to be assigned to Fedora Objects so when a request is submitted, those questions must be answered.
 * </ol>
 * 
 * <pre>
 * Version	Date		Developer			Description
 * 0.1		1/05/2012	Rahul Khanna (RK)	Initial
 * </pre>
 * 
 */
@Path("/collreq")
public class CollectionRequestService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRequestService.class);
	// TODO Check if the entitymanager is to be object specific or not - static or non-static?
	private static final EntityManager entityManager = HibernateUtil.getSessionFactory().createEntityManager();

	private static final String COLL_REQ_JSP = "/collreq.jsp";
	private static final String DROPBOX_JSP = "/dropbox.jsp";
	private static final String QUESTION_JSP = "/question.jsp";
	private static final String DROPBOX_ACCESS_JSP = "/dropboxaccess.jsp";

	@Resource(name = "mailSender")
	JavaMailSenderImpl mailSender;

	/**
	 * doGetAsHtml
	 * 
	 * Australian National University Data Commons
	 * 
	 * Gets the Collection Request page.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		1/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @return Collection Request page as HTML.
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response doGetAsHtml()
	{
		Response resp = null;
		Map<String, Object> model = new HashMap<String, Object>();

		entityManager.getTransaction().begin();
		List<CollectionRequest> collReqs = entityManager.createQuery("FROM CollectionRequest cr ORDER BY cr.timestamp DESC", CollectionRequest.class)
				.getResultList();
		entityManager.getTransaction().commit();
		model.put("collReqs", collReqs);
		resp = Response.ok(new Viewable(COLL_REQ_JSP, model)).build();
		return resp;
	}

	/**
	 * doGetReqItemAsHtml
	 * 
	 * Australian National University Data Commons
	 * 
	 * Gets the specific Collection Request with the provided ID.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		1/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @param collReqId
	 *            ID of the Collection request as Long.
	 * @return Collection Request page as HTML.
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("{collReqId}")
	public Response doGetReqItemAsHtml(@PathParam("collReqId") Long collReqId)
	{
		PageMessages messages = new PageMessages();
		Map<String, Object> model = new HashMap<String, Object>();
		Response resp = null;

		LOGGER.trace("In method doGetReqItemAsHtml. Param collReqId={}.", collReqId);

		try
		{
			LOGGER.debug("Retrieving Collection Request with ID: {}...", collReqId);
			entityManager.getTransaction().begin();
			// Find the Collection Request with the specified ID.
			CollectionRequest collReq = entityManager.find(CollectionRequest.class, collReqId);
			entityManager.getTransaction().commit();

			// Check if the Collection Request actually exists. If not, throw Exception.
			if (collReq == null)
				throw new Exception("Invalid Collection Request ID or no Collection Request with that ID exists.");

			LOGGER.debug("Found Collection Request ID {}, for Pid {}.", collReq.getId(), collReq.getPid());
			model.put("collReq", collReq);
		}
		catch (Exception e)
		{
			LOGGER.error("Unable to find or retrieve Collection Request " + collReqId, e);
			if (entityManager.getTransaction().isActive())
				entityManager.getTransaction().rollback();
			messages.clear();
			messages.add(MessageType.ERROR, e.getMessage(), model);
		}
		finally
		{
			resp = Response.ok(new Viewable(COLL_REQ_JSP, model)).build();
		}

		return resp;
	}

	/**
	 * doPostCollReqAsHtml
	 * 
	 * Australian National University Data Commons
	 * 
	 * Receives a POST request with the details of a new Collection Request to be created and creates a new Collection Request.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		1/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @param pid
	 *            Pid for which the request belongs to.
	 * @param requestedFileSet
	 *            Datastream IDs as a set being requested.
	 * @param allFormParams
	 *            Map of all form parameters from which questions that have been answered are extracted.
	 * @return Response as HTML
	 */
	@POST
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response doPostCollReqAsHtml(@Context HttpServletRequest request, @FormParam("pid") String pid, @FormParam("file") Set<String> requestedFileSet,
			MultivaluedMap<String, String> allFormParams)
	{
		PageMessages messages = new PageMessages();
		Map<String, Object> model = new HashMap<String, Object>();
		Response resp = null;
		UriBuilder uriBuilder = null;

		LOGGER.trace("In method doPostCollReqAsHtml. Param pid={}, dsIdSet={}, allFormParams={}.", new Object[] { pid, requestedFileSet, allFormParams });

		// Save the Collection Request for further processing.
		try
		{
			// Check if at least one item has been requested. If not, throw exception.
			LOGGER.debug("Number of items selected in CR: {}", requestedFileSet.size());
			if (requestedFileSet.size() <= 0)
				throw new Exception("At least one datastream must be selected in the request.");

			entityManager.getTransaction().begin();
			Users user = new UsersDAOImpl(Users.class).getUserByName(SecurityContextHolder.getContext().getAuthentication().getName());
			CollectionRequest newCollReq = new CollectionRequest(pid, user, request.getRemoteAddr());

			// Add each of the items requested (datastreams) to the CR.
			for (String iFile : requestedFileSet)
			{
				CollectionRequestItem collReqItem = new CollectionRequestItem(iFile);
				newCollReq.addItem(collReqItem);
			}

			// Get a list of questions assigned to the Pid.
			List<Question> questionList = entityManager
					.createQuery("SELECT q FROM Question q, QuestionMap qm WHERE qm.pid=:pid AND q=qm.question", Question.class).setParameter("pid", pid)
					.getResultList();

			// Iterate through the questions that need to be answered for the pid, get the answers for those questions and add to CR.
			// If an answer for a question doesn't exist throw exception.
			for (Question iQuestion : questionList)
			{
				// Check if the answer to the current question is provided. If yes, save the answer, else throw exception.
				if (allFormParams.containsKey("q" + iQuestion.getId()) && Util.isNotEmpty(allFormParams.getFirst("q" + iQuestion.getId())))
				{
					CollectionRequestAnswer ans = new CollectionRequestAnswer(iQuestion, allFormParams.getFirst("q" + iQuestion.getId()));
					newCollReq.addAnswer(ans);
				}
				else
				{
					throw new Exception("All questions must be answered. The question '" + iQuestion.getQuestionText() + "' has been left blank.");
				}
			}
			LOGGER.debug("All questions answered for this Pid.");

			// Save the newly created CR and add success message to message set.
			entityManager.persist(newCollReq);
			entityManager.getTransaction().commit();
			messages.add(MessageType.SUCCESS, "Collection Request successfully saved. ID# " + newCollReq.getId(), model);
			model.put("collReq", newCollReq);
			uriBuilder = UriBuilder.fromPath("/collreq/").path(newCollReq.getId().toString());
			uriBuilder = uriBuilder.queryParam("smsg", "Collection Request saved. ID# " + newCollReq.getId().toString());
		}
		catch (Exception e)
		{
			LOGGER.error("Unable to create new Collection Request.", e);
			if (entityManager.getTransaction().isActive())
				entityManager.getTransaction().rollback();
			uriBuilder = UriBuilder.fromPath("/collreq/").queryParam("pid", pid).queryParam("emsg", e.getMessage());
		}
		finally
		{
			resp = Response.seeOther(uriBuilder.build()).build();
		}

		return resp;
	}

	/**
	 * doPostUpdateCollReqAsHtml
	 * 
	 * Australian National University Data Commons
	 * 
	 * This method accepts changes to be made to a Collection Request.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		2/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @param collReqId
	 *            The collection ID to be updated.
	 * @param status
	 *            The status of the collection request. E.g. active, rejected etc.
	 * @param reason
	 *            The reason for the status change.
	 * @return Response as HTML.
	 */
	@POST
	@Path("{collReqId}")
	@Produces(MediaType.TEXT_HTML)
	public Response doPostUpdateCollReqAsHtml(@PathParam("collReqId") long collReqId, @FormParam("status") ReqStatus status, @FormParam("reason") String reason)
	{
		PageMessages messages = new PageMessages();
		Map<String, Object> model = new HashMap<String, Object>();
		Response resp = null;
		CollectionRequest collReq = null;

		LOGGER.trace("In doPostUpdateCollReqAsHtml. Params collReqId={}, status={}, reason={}", new Object[] { collReqId, status, reason });

		try
		{
			LOGGER.debug("Saving Collection Request ID {} with updated details...", collReqId);

			// Get the CR with the provided ID.
			entityManager.getTransaction().begin();
			collReq = entityManager.find(CollectionRequest.class, collReqId);

			// Check if the CR exists.
			if (collReq == null)
				throw new Exception("Invalid Collection Request ID or Collection Request could not be retrieved.");

			model.put("collReq", collReq);

			// Check if a reason is provided. If not, add error to message set.
			if (!Util.isNotEmpty(reason))
				throw new Exception("A reason must be provided for a status change.");

			// If the CR's current status is approved or rejected, the status cannot change anymore.
			ReqStatus curStatus = collReq.getLastStatus().getStatus();
			if (curStatus == ReqStatus.ACCEPTED || curStatus == ReqStatus.REJECTED)
				throw new Exception(
						"Cannot change status of a CR with an Approved or Rejected status. A new CR must be submitted by the requestor for processing.");

			// Add a status row to the status history for that CR.
			Users user = new UsersDAOImpl(Users.class).getUserByName(SecurityContextHolder.getContext().getAuthentication().getName());
			CollectionRequestStatus newStatus = new CollectionRequestStatus(collReq, status, reason, user);
			collReq.addStatus(newStatus);
			entityManager.getTransaction().commit();

			LOGGER.debug("Updated details of CR ID# {}.", collReq.getId());
			messages.add(MessageType.SUCCESS, "Successfully added status to Status History", model);

			// Add a message about the dropbox being created if the status is now Accepted.
			if (status == ReqStatus.ACCEPTED)
			{
				messages.add(MessageType.INFO, "Dropbox created<br /><strong>Code: </strong>" + collReq.getDropbox().getAccessCode()
						+ "<br /><strong>Password: </strong>" + collReq.getDropbox().getAccessPassword(), model);
				// TODO Change hard code below.
				messages.add(MessageType.INFO, "Dropbox Access Link: <a href='" + "/DataCommons/rest/collreq/dropbox/access/"
						+ collReq.getDropbox().getAccessCode() + "?p=" + collReq.getDropbox().getAccessPassword() + "'>Dropbox Access</a>", model);
			}

			/*
			// TODO Send email to requestor. Add message to screen if email was successful.
			HashMap<String, String> varMap = new HashMap<String, String>();
			varMap.put("requestorGivenName", collReq.getRequestor().getGivenName());
			varMap.put("collReqId", collReq.getId().toString());
			varMap.put("changedByDispName", collReq.getLastStatus().getUser().getDisplayName());
			varMap.put("dateChanged", collReq.getLastStatus().getTimestamp().toString());
			varMap.put("status", collReq.getLastStatus().getStatus().toString());
			varMap.put("reason", collReq.getLastStatus().getReason());

			Email email = new Email(mailSender);
			email.setFromName("ANU DataCommons");
			email.setFromEmail("no-reply@anu.edu.au");
			email.setToName(collReq.getRequestor().getDisplayName());
			email.setToEmail(collReq.getRequestor().getEmail());
			email.setSubject("Dropbox# " + collReq.getId() + " Status Changed");
			email.setBody("mailtmpl/dropboxstatus.txt", varMap);
			*/

		}
		catch (Exception e)
		{
			LOGGER.error("Unable to add request row.", e);
			if (entityManager.getTransaction().isActive())
				entityManager.getTransaction().rollback();
			messages.clear();
			messages.add(MessageType.ERROR, e.getMessage(), model);
		}
		finally
		{
			resp = Response.ok(new Viewable(COLL_REQ_JSP, model)).build();
		}

		return resp;
	}

	/**
	 * doGetDropboxesAsHtml
	 * 
	 * Australian National University Data Commons
	 * 
	 * This method returns a list of dropboxes and their details. This method enables administration of dropboxes only. Access to the files within the dropbox
	 * is from doGetDropboxAccessAsHtml method.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		2/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @return Response as HTML.
	 */
	@GET
	@Path("dropbox")
	@Produces(MediaType.TEXT_HTML)
	public Response doGetDropboxesAsHtml()
	{
		PageMessages messages = new PageMessages();
		Map<String, Object> model = new HashMap<String, Object>();
		Response resp = null;

		LOGGER.trace("In doGetDropboxesAsHtml.");

		try
		{
			entityManager.getTransaction().begin();
			// Get all relevant dropboxes.
			// TODO Exclude any dropboxes that the user doesn't have any admin rights to.
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<CollectionDropbox> criteria = builder.createQuery(CollectionDropbox.class);
			Root<CollectionDropbox> root = criteria.from(CollectionDropbox.class);
			criteria.select(root);
			List<CollectionDropbox> dropboxes = entityManager.createQuery(criteria).getResultList();
			entityManager.getTransaction().commit();

			// Add a message for the user if no dropboxes found.
			if (dropboxes.size() == 0)
				messages.add(MessageType.WARNING, "No dropboxes found.", model);

			model.put("dropboxes", dropboxes);
		}
		catch (Exception e)
		{
			LOGGER.error("Unable to get list of Collection Dropboxes.", e);
			if (entityManager.getTransaction().isActive())
				entityManager.getTransaction().rollback();
			messages.clear();
			messages.add(MessageType.ERROR, e.getMessage(), model);
		}
		finally
		{
			resp = Response.ok(new Viewable(DROPBOX_JSP, model)).build();
		}

		return resp;
	}

	/**
	 * doGetDropboxAsHtml
	 * 
	 * Australian National University Data Commons
	 * 
	 * This method Displays the details of a dropbox. It does not allow access to the files that have been requested in the dropbox.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		2/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @param dropboxId
	 *            The ID of the dropbox.
	 * 
	 * @return Response as HTML.
	 */
	@GET
	@Path("dropbox/{dropboxId}")
	@Produces(MediaType.TEXT_HTML)
	public Response doGetDropboxAsHtml(@PathParam("dropboxId") long dropboxId)
	{
		PageMessages messages = new PageMessages();
		Map<String, Object> model = new HashMap<String, Object>();
		Response resp = null;

		LOGGER.trace("In doGetDropboxAsHtml. Params dropboxId: {}", dropboxId);

		try
		{
			// Find the dropbox with the specified ID.
			entityManager.getTransaction().begin();
			CollectionDropbox dropbox = entityManager.find(CollectionDropbox.class, dropboxId);
			entityManager.getTransaction().commit();

			// Check if a valid dropbox exists and was retrieved.
			if (dropbox == null)
				throw new Exception("Invalid Dropbox ID or a dropbox with ID " + dropboxId + "doesn't exist.");

			model.put("dropbox", dropbox);
		}
		catch (Exception e)
		{
			LOGGER.error("Unable to get list of Collection Dropboxes.", e);
			if (entityManager.getTransaction().isActive())
				entityManager.getTransaction().rollback();
			messages.clear();
			messages.add(MessageType.ERROR, e.getMessage(), model);
		}
		finally
		{
			resp = Response.ok(new Viewable(DROPBOX_JSP, model)).build();
		}

		return resp;
	}

	/**
	 * doPostUpdateDropboxAsHtml
	 * 
	 * Australian National University Data Commons
	 * 
	 * This method accepts POST requests to update the details of a dropbox.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		15/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @param dropboxId
	 *            ID of the dropbox to be updated.
	 * @return Response as HTML.
	 */
	@POST
	@Path("dropbox/{dropboxId}")
	@Produces(MediaType.TEXT_HTML)
	public Response doPostUpdateDropboxAsHtml(@PathParam("dropboxId") long dropboxId)
	{
		PageMessages messages = new PageMessages();
		Map<String, Object> model = new HashMap<String, Object>();
		Response resp = null;

		// TODO Process updates to dropbox. Change notifyOnPickup and/or active status.

		return resp;
	}

	/**
	 * doGetDropboxAccessAsHtml
	 * 
	 * Australian National University Data Commons
	 * 
	 * This method allows access to the files in a dropbox.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		2/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @param dropboxAccessCode
	 *            Access code of a dropbox.
	 * @param password
	 *            Password to access the dropbox.
	 * @return Response as HTML.
	 */
	@GET
	@Path("dropbox/access/{dropboxAccessCode}")
	@Produces(MediaType.TEXT_HTML)
	public Response doGetDropboxAccessAsHtml(@Context HttpServletRequest request, @Context UriInfo uriInfo,
			@PathParam("dropboxAccessCode") long dropboxAccessCode, @QueryParam("p") String password)
	{
		PageMessages messages = new PageMessages();
		Map<String, Object> model = new HashMap<String, Object>();
		Response resp = null;

		LOGGER.trace("In doGetDropboxAccessAsHtml. Params dropboxAccessCode={}, password={}.", dropboxAccessCode, password);

		try
		{
			// TODO Use CriteriaBuilder.
			/*
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<CollectionDropbox> criteria = builder.createQuery(CollectionDropbox.class);
			Root<CollectionDropbox> root = criteria.from(CollectionDropbox.class);
			criteria.select(root);
			criteria.where(builder.equal()
			*/

			LOGGER.debug("Finding dropbox with access code {}...", dropboxAccessCode);
			entityManager.getTransaction().begin();
			CollectionDropbox dropbox = (CollectionDropbox) entityManager.createQuery("FROM CollectionDropbox cd WHERE cd.accessCode=:dropboxAccessCode")
					.setParameter("dropboxAccessCode", dropboxAccessCode).getSingleResult();
			entityManager.getTransaction().commit();
			LOGGER.debug("Dropbox found.");
			model.put("dropbox", dropbox);

			if (new Date().after(dropbox.getExpiry()))				// Check if today's date and time is after dropbox expiry.
				throw new Exception("This Dropbox has expired. Please submit a new Collection Request.");

			if (!dropbox.isActive())								// Check if dropbox active.
				throw new Exception("This Dropbox has been marked as inactive.");

			if (!Util.isNotEmpty(password))							// Check if password provided.
				throw new IllegalArgumentException("Please enter password for this Dropbox.");

			if (!password.equals(dropbox.getAccessPassword()))		// Check if password correct.
				throw new Exception("Incorrect password entered.");

			// Create HashMap downloadables with a link for each item to be downloaded.
			HashMap<String, String> downloadables = new HashMap<String, String>();
			for (CollectionRequestItem reqItem : dropbox.getCollectionRequest().getItems())
			{
				UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(UploadService.class).path(UploadService.class, "doGetFileInBagAsOctetStream");
				LOGGER.debug("Adding URI {} to downloadables", uriBuilder.build(dropbox.getCollectionRequest().getPid(), reqItem.getItem()).toString());
				downloadables.put(reqItem.getItem(), uriBuilder.build(dropbox.getCollectionRequest().getPid(), reqItem.getItem()).toString());
			}
			model.put("downloadables", downloadables);

			// Fetch List.
			Map<String, String> fetchables = new HashMap<String, String>();
			try
			{
				DcBag dcBag = new DcBag(new File(GlobalProps.getBagsDirAsFile(), Util.convertToDiskSafe(dropbox.getCollectionRequest().getPid())),
						LoadOption.BY_MANIFESTS);
				for (FilenameSizeUrl iFetchItem : dcBag.getFetchEntries())
				{
					LOGGER.debug("Added fetch item {}", iFetchItem.toString());
					fetchables.put(iFetchItem.getFilename(), iFetchItem.getUrl());
				}
			}
			finally
			{
				// GlobalProps throws exception if no Bags Dir specified in props file.
			}
			if (fetchables.size() > 0)
				model.put("fetchables", fetchables);

			// Make a log of access
			entityManager.getTransaction().begin();
			CollectionDropboxAccessLog log = new CollectionDropboxAccessLog(dropbox, request.getRemoteAddr());
			entityManager.persist(log);
			entityManager.getTransaction().commit();
		}
		catch (Exception e)
		{
			LOGGER.error("Unable to get Dropbox for access.", e);
			messages.clear();
			if (entityManager.getTransaction().isActive())
				entityManager.getTransaction().rollback();
			if (e.getClass() == IllegalArgumentException.class)
				messages.add(MessageType.INFO, e.getMessage(), model);
			else
				messages.add(MessageType.ERROR, e.getMessage(), model);
		}
		finally
		{
			resp = Response.ok(new Viewable(DROPBOX_ACCESS_JSP, model)).build();
		}

		return resp;
	}

	/**
	 * doGetQuestionsAsHtml
	 * 
	 * Australian National University Data Commons
	 * 
	 * Returns a Viewable with all questions in the question bank in its model.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		2/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @return Response as HTML.
	 */
	@GET
	@Path("question")
	@Produces(MediaType.TEXT_HTML)
	public Response doGetQuestionsAsHtml()
	{
		PageMessages messages = new PageMessages();
		Map<String, Object> model = new HashMap<String, Object>();
		Response resp = null;

		LOGGER.trace("In doGetQuestionsAsHtml");

		try
		{
			// Get all questions from the Question Bank.
			LOGGER.debug("Retrieving questions from question bank...");
			List<Question> questions = getAllQuestions();

			// Add a warning to the message set to let the user know that there aren't any questions in the question bank.
			if (questions.size() == 0)
				messages.add(MessageType.WARNING, "No questions found in the question bank.", model);

			model.put("questions", questions);
		}
		catch (Exception e)
		{
			LOGGER.error("Unable to retrieve questions from question bank", e);
			if (entityManager.getTransaction().isActive())
				entityManager.getTransaction().rollback();
			messages.clear();
			messages.add(MessageType.ERROR, e.getMessage(), model);
		}
		finally
		{
			resp = Response.ok(new Viewable(QUESTION_JSP, model)).build();
		}

		return resp;
	}

	/**
	 * doPostQuestionAsHtml
	 * 
	 * Australian National University Data Commons
	 * 
	 * Accepts POST requests to add new questions in the question bank as well as requests to assign a question to a Pid.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		2/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @param submit
	 *            The task to be performed. Value comes from the submit button in a form. "Add Question" to add a question to the question bank, or "Save" to
	 *            update the list of questions against a Pid.
	 * @param questionText
	 *            Question as String
	 * @param pid
	 *            Pid as String
	 * @param qIdSet
	 *            Questions to be assigned to Pid as Set.
	 * @return Response as HTML.
	 */
	@POST
	@Path("question")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response doPostQuestionAsHtml(@Context UriInfo uriInfo, @FormParam("submit") String submit, @FormParam("q") String questionText,
			@FormParam("pid") String pid, @FormParam("qid") Set<Long> qIdSet)
	{
		Response resp = null;
		UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(CollectionRequestService.class).path(CollectionRequestService.class, "doGetQuestionsAsHtml");

		LOGGER.trace("In doPostQuestionAsHtml. Params submit={}, questionStr={}, pid={}, qid={}.", new Object[] { submit, questionText, pid, qIdSet });

		if (Util.isNotEmpty(pid))
			uriBuilder = uriBuilder.queryParam("pid", pid);

		if (Util.isNotEmpty(submit))
		{
			// Adding a question to the question bank.
			if (submit.equals("Add Question"))
			{
				try
				{
					// Validate question text.
					questionText = questionText.trim();
					if (questionText.equals(""))
						throw new Exception("Question text not provided. A Question cannot be blank.");

					LOGGER.debug("Saving question in question bank...", pid);
					// Create Question object and persist it.
					entityManager.getTransaction().begin();
					Question question = new Question(questionText);
					entityManager.persist(question);
					entityManager.getTransaction().commit();
					uriBuilder = uriBuilder.queryParam("smsg", "The question <em>" + question.getQuestionText() + "</em> saved in the Question Bank.");
					LOGGER.info("Saved question in question bank: {}", question.getQuestionText());
				}
				catch (Exception e)
				{
					LOGGER.error("Unable to save question in the question bank.", e);
					uriBuilder.queryParam("emsg", "Unable to save question in the question bank.");
					if (entityManager.getTransaction().isActive())
						entityManager.getTransaction().rollback();
				}

				resp = Response.seeOther(uriBuilder.build()).build();
			}
			// Assigning questions to a pid.
			else if (submit.equals("Save"))
			{
				try
				{
					entityManager.getTransaction().begin();
					// TODO Use CriteriaBuilder.
					// Get list of questions currently assigned to the pid.
					List<Question> curQuestionsPid = entityManager
							.createQuery("SELECT q FROM Question q, QuestionMap qm WHERE qm.pid=:pid AND q=qm.question", Question.class)
							.setParameter("pid", pid).getResultList();

					// Check if each question Id provided as query parameters already exist. If not, add them.
					for (Long iUpdatedId : qIdSet)
					{
						boolean isAlreadyMapped = false;
						for (Question iCurQuestion : curQuestionsPid)
						{
							if (iCurQuestion.getId() == iUpdatedId.longValue())
							{
								isAlreadyMapped = true;
								break;
							}
						}

						if (!isAlreadyMapped)
						{
							Question question = entityManager.find(Question.class, iUpdatedId);
							LOGGER.debug("Adding Question '{}' against Pid {}", question.getQuestionText(), pid);
							QuestionMap qm = new QuestionMap(pid, question);
							entityManager.persist(qm);
						}
					}

					// Check if each question for a pid is provided in the updated list. If not, delete it.
					for (Question iCurQuestion : curQuestionsPid)
					{
						if (!qIdSet.contains(iCurQuestion.getId()))
						{
							LOGGER.debug("Mapping of Question ID" + iCurQuestion.getId() + "to be deleted...");
							QuestionMap qMap = entityManager
									.createQuery("SELECT qm FROM QuestionMap qm, Question q WHERE qm.pid=:pid AND qm.question=:q", QuestionMap.class)
									.setParameter("pid", pid).setParameter("q", iCurQuestion).getSingleResult();
							entityManager.remove(qMap);
						}
					}

					entityManager.getTransaction().commit();
					uriBuilder = uriBuilder.queryParam("smsg", "Question List updated for this Fedora object.");
				}
				catch (Exception e)
				{
					LOGGER.error("Unable to update questions for Pid " + pid, e);
					if (entityManager.getTransaction().isActive())
						entityManager.getTransaction().rollback();
					uriBuilder = uriBuilder.queryParam("emsg", "Unable to update questions for this Collection.");
				}

				resp = Response.seeOther(uriBuilder.build()).build();
			}
		}
		else
		{
			LOGGER.error("Attempt to POST question without param 'submit'.");
			resp = Response.serverError().build();
		}

		return resp;
	}

	/**
	 * doGetDsListAsJson
	 * 
	 * Australian National University Data Commons
	 * 
	 * This method returns JSON responses for requests for the following:
	 * 
	 * <ol>
	 * <li>List of Datastreams of a Fedora Object.</li>
	 * <li>List of Collection Requests and their details</li>
	 * <ol>
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		8/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @return JSON object
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("json")
	public Response doGetCollReqInfoAsJson(@QueryParam("task") String task, @QueryParam("pid") String pid)
	{
		Response resp = null;

		LOGGER.trace("In doGetDsListAsJson. Params task={}, pid={}.", task, pid);

		// Gets a list of items available for request in a Collection.
		if (task.equals("listPidItems"))
		{
			JSONArray itemsAvail = new JSONArray();
			try
			{
				// Get a list of datastreams for the pid from the Fedora Repository.
				List<DatastreamType> pidDsList = FedoraBroker.getDatastreamList(pid);

				//Iterate through the files holding datastreams - FILE0 onwards.
				for (DatastreamType iDs : pidDsList)
				{
					if (iDs.getDsid().startsWith("FILE"))
					{
						try
						{
							DcBag dcBag = new DcBag(new File(GlobalProps.getBagsDirAsFile(), Util.convertToDiskSafe(pid)), LoadOption.BY_MANIFESTS);
							for (Entry<String, String> iFileItem : dcBag.getPayloadFileList())
							{
								try
								{
									JSONObject dsJsonObj = new JSONObject();
									dsJsonObj.put("filename", iFileItem.getKey().replaceFirst("data/", ""));
									itemsAvail.put(dsJsonObj);
								}
								catch (JSONException e)
								{
								}
							}
						}
						catch (RuntimeException e)
						{
							// Thrown when the bag doesn't exist.
						}
					}
				}

				// Convert the JSONArray containing datastream details into a JSON string and create a Response object for return.
				LOGGER.debug("Returning JSON Object: {}", itemsAvail.toString());
				resp = Response.ok(itemsAvail.toString(), MediaType.APPLICATION_JSON_TYPE).build();
			}
			catch (FedoraClientException e)
			{
				LOGGER.error("Unable to retrieve list of datastreams.", e);
				resp = Response.serverError().build();
			}
			catch (PropertyException e)
			{
				LOGGER.error("Unable to retrieve list of datastreams.", e);
				resp = Response.serverError().build();
			}
		}
		// Gets a list of Questions assigned to a Pid.
		else if (task.equals("listPidQuestions"))
		{
			JSONObject questionsJson = new JSONObject();
			try
			{
				// Get all Questions assigned to the specified Pid.
				entityManager.getTransaction().begin();
				List<Question> curQuestionsPid = entityManager
						.createQuery("SELECT q FROM Question q, QuestionMap qm WHERE qm.pid=:pid AND q=qm.question", Question.class).setParameter("pid", pid)
						.getResultList();
				entityManager.getTransaction().commit();

				// Add the Id and question (String) for each Question (Object) into a JSONObject. 
				for (Question iQuestion : curQuestionsPid)
					questionsJson.put(iQuestion.getId().toString(), iQuestion.getQuestionText());

				// Convert the JSONObject into a JSON String and include it in the Response object.
				resp = Response.ok(questionsJson.toString(), MediaType.APPLICATION_JSON_TYPE).build();
			}
			catch (Exception e)
			{
				LOGGER.error("Unable to get list of questions for Pid " + pid, e);
				resp = Response.serverError().build();
			}
		}
		// Get a list of Collection Requests and their details.
		else if (task.equals("listCollReq"))
		{
			JSONArray reqStatusListJsonArray = new JSONArray();

			try
			{
				LOGGER.debug("Requested Collection Requests as JSON.");

				// Retrieve Collection Requests. 
				entityManager.getTransaction().begin();
				List<CollectionRequest> reqStatusList = entityManager.createQuery("FROM CollectionRequest cr ORDER BY cr.timestamp DESC",
						CollectionRequest.class).getResultList();
				entityManager.getTransaction().commit();

				// Add the details of each CR into a JSONObject. Then add that JSONObject to a JSONArray.
				for (CollectionRequest iCr : reqStatusList)
				{
					JSONObject reqStatusJsonObj = new JSONObject();
					reqStatusJsonObj.put("id", iCr.getId());
					reqStatusJsonObj.put("pid", iCr.getPid());
					reqStatusJsonObj.put("requestor", iCr.getRequestor().getUsername());
					reqStatusJsonObj.put("timestamp", iCr.getTimestamp().toString());
					reqStatusJsonObj.put("lastStatus", iCr.getLastStatus().getStatus().toString());
					reqStatusJsonObj.put("lastStatusTimestamp", iCr.getLastStatus().getTimestamp().toString());
					reqStatusListJsonArray.put(reqStatusJsonObj);
				}

				// Convert the JSONArray into a JSON String and include in Response object.
				resp = Response.ok(reqStatusListJsonArray.toString(), MediaType.APPLICATION_JSON_TYPE).build();
			}
			catch (Exception e)
			{
				LOGGER.error("Unable to get list of Collection Requests.");
				if (entityManager.getTransaction().isActive())
					entityManager.getTransaction().rollback();
				resp = Response.serverError().build();
			}
		}

		return resp;
	}

	/**
	 * getAllQuestions
	 * 
	 * Australian National University Data Commons
	 * 
	 * Gets a list of all questions from the Question Bank.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		16/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @return Questions as List<Questions>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Question> getAllQuestions()
	{
		List<Question> questions = new GenericDAOImpl(Question.class).getAll();
		return questions;
	}
}
