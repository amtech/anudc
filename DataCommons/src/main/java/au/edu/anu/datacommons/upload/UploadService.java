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

package au.edu.anu.datacommons.upload;

import static java.text.MessageFormat.*;
import gov.loc.repository.bagit.Manifest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import au.edu.anu.datacommons.collectionrequest.CollectionDropbox;
import au.edu.anu.datacommons.collectionrequest.CollectionRequestItem;
import au.edu.anu.datacommons.collectionrequest.PageMessages;
import au.edu.anu.datacommons.collectionrequest.PageMessages.MessageType;
import au.edu.anu.datacommons.data.db.dao.AccessLogRecordDAOImpl;
import au.edu.anu.datacommons.data.db.dao.DropboxDAO;
import au.edu.anu.datacommons.data.db.dao.DropboxDAOImpl;
import au.edu.anu.datacommons.data.db.dao.FedoraObjectDAOImpl;
import au.edu.anu.datacommons.data.db.dao.UsersDAOImpl;
import au.edu.anu.datacommons.data.db.model.FedoraObject;
import au.edu.anu.datacommons.data.db.model.Users;
import au.edu.anu.datacommons.properties.GlobalProps;
import au.edu.anu.datacommons.security.AccessLogRecord;
import au.edu.anu.datacommons.security.acl.CustomACLPermission;
import au.edu.anu.datacommons.security.acl.PermissionService;
import au.edu.anu.datacommons.security.service.FedoraObjectService;
import au.edu.anu.datacommons.storage.DcStorage;
import au.edu.anu.datacommons.storage.DcStorageException;
import au.edu.anu.datacommons.storage.TempFileTask;
import au.edu.anu.datacommons.storage.info.BagSummary;
import au.edu.anu.datacommons.storage.info.FileSummaryMap;
import au.edu.anu.datacommons.util.Util;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.view.Viewable;

/**
 * UploadService
 * 
 * Australian National University Data Commons
 * 
 * This class accepts POST requests for uploading files to datastreams in a Collection. The files are uploaded using
 * JUpload applet.
 * 
 * <pre>
 * Version	Date		Developer			Description
 * 0.1		14/05/2012	Rahul Khanna (RK)	Initial
 * </pre>
 */
@Path("/upload")
@Component
@Scope("request")
public class UploadService {
	private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);
	private static final String PART_FILE_SUFFIX = ".part";
	private static final String UPLOAD_JSP = "/upload.jsp";
	private static final String BAGFILES_JSP = "/bagfiles.jsp";
	private static final DcStorage dcStorage = DcStorage.getInstance();

	@Context
	private UriInfo uriInfo;
	@Context
	private HttpServletRequest request;
	@Context
	private HttpHeaders httpHeaders;

	@Resource(name = "fedoraObjectServiceImpl")
	private FedoraObjectService fedoraObjectService;

	@Resource(name = "permissionService")
	private PermissionService permissionService;

	/**
	 * doGetAsHtml
	 * 
	 * Australian National University Data Commons
	 * 
	 * Accepts Get requests to display the file upload page.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		14/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @return HTML page as response.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	@PreAuthorize("hasRole('ROLE_ANU_USER')")
	public Response doGetAsHtml() {
		LOGGER.trace("In doGetAsHtml");
		return Response.ok(new Viewable(UPLOAD_JSP)).build();
	}

	/**
	 * Accepts POST requests from a JUpload applet and saves the files on the server for further processing. Creates a
	 * placeholder datastream in the fedora object preventing reuploading to the same datastream.
	 * 
	 * @return A response with status information.
	 */
	@POST
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@PreAuthorize("hasRole('ROLE_ANU_USER')")
	public Response doPostJUploadFilePart() {
		Response resp = null;
		List<FileItem> uploadedItems;
		int filePartNum = 0;
		boolean isLastPart = false;
		File savedFile = null;

		// Check if this is a part file. Files greater than threshold specified in JUpload params will be split up and
		// sent as parts.
		if (Util.isNotEmpty(request.getParameter("jupart"))) {
			filePartNum = Integer.parseInt(request.getParameter("jupart"));
			// Check if this is the final part of a file being send in parts.
			if (Util.isNotEmpty(request.getParameter("jufinal")))
				isLastPart = request.getParameter("jufinal").equals("1");
		}

		FileWriter propFileWriter = null;
		try {
			// Get a list of uploaded items. Some may be files, others form fields.
			uploadedItems = parseUploadRequest(request);

			String md5 = null;
			String pid = null;
			for (FileItem i : uploadedItems) {
				if (i.isFormField()) {
					if (i.getFieldName().equalsIgnoreCase("md5sum0")) {
						md5 = i.getString();
					} else if (i.getFieldName().equalsIgnoreCase("pid")) {
						pid = i.getString();
					}
				}
			}
			
			if (md5 == null || md5.length() == 0) {
				throw new NullPointerException("MD5 cannot be null.");
			}
			if (pid == null || pid.length() == 0) {
				throw new NullPointerException("Record Identifier cannot be null.");
			}
			
			// Check for write access to the fedora object.
			getFedoraObjectWriteAccess(pid);
			
			for (FileItem i : uploadedItems) {
				if (!i.isFormField()) {
					savedFile = saveFileOnServer(i, filePartNum, isLastPart, pid, md5);
					break;
				}
			}
			if (savedFile == null) {
				throw new NullPointerException("File saved on server is null.");
			}
			
			if (isLastPart || filePartNum == 0) {
				try {
					dcStorage.addFileToBag(pid, savedFile, savedFile.getName());
				} finally {
					if (!savedFile.delete()) {
						LOGGER.warn("Unable to delete {}", savedFile.getAbsolutePath());
					}
					if (!savedFile.getParentFile().equals(GlobalProps.getUploadDirAsFile()) && savedFile.getParentFile().listFiles().length == 0) {
						if (!savedFile.getParentFile().delete()) {
							LOGGER.warn("Unable to delete {}", savedFile.getAbsolutePath());
						}
					}
				}
			}
			resp = Response.ok("SUCCESS", MediaType.TEXT_PLAIN_TYPE).build();
		} catch (Exception e) {
			LOGGER.error("Unable to process POST request.", e);
			resp = Response.ok(format("ERROR: Unable to process request. [{0}]", e.getMessage()),
					MediaType.TEXT_PLAIN_TYPE).build();
		} finally {
			IOUtils.closeQuietly(propFileWriter);
		}

		return resp;
	}

	/**
	 * This method gets called after jupload file parts have been uploaded to merge the file parts, and add the
	 * completed files into bags for the collection.
	 * 
	 * @param pid
	 *            Pid of the collection to which the uploaded bags are to be added.
	 * @return Response as HTML
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("bagit/{pid}")
	@PreAuthorize("hasRole('ROLE_ANU_USER')")
	public Response doGetCreateBag(@PathParam("pid") String pid) {
		Properties uploadProps = new Properties();
		File propFile = null;
		File uploadedFilesDir = null;
		Response resp = null;
		BufferedInputStream propFileInStream = null;
		AccessLogRecord accessRec = null;
		UriBuilder redirUri = UriBuilder.fromUri(uriInfo.getBaseUri()).path(UploadService.class);

		// Check for write access to the fedora object.
		getFedoraObjectWriteAccess(pid);

		try {
			// Read properties file [Pid].properties.
			propFile = new File(GlobalProps.getUploadDirAsFile(), DcStorage.convertToDiskSafe(pid) + ".properties");
			propFileInStream = new BufferedInputStream(new FileInputStream(propFile));
			uploadProps.load(propFileInStream);

			// Check the pid in the URL against the one in the properties file.
			if (!pid.equalsIgnoreCase(uploadProps.getProperty("pid")))
				throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);

			// Create access log.
			if (dcStorage.bagExists(pid))
				accessRec = new AccessLogRecord(uriInfo.getPath(), getCurUser(), request.getRemoteAddr(),
						request.getHeader("User-Agent"), AccessLogRecord.Operation.UPDATE);
			else
				accessRec = new AccessLogRecord(uriInfo.getPath(), getCurUser(), request.getRemoteAddr(),
						request.getHeader("User-Agent"), AccessLogRecord.Operation.CREATE);

			// Add files to bag.
			uploadedFilesDir = new File(GlobalProps.getUploadDirAsFile(), DcStorage.convertToDiskSafe(pid));
			String filename;
			for (int i = 0; (filename = uploadProps.getProperty("file" + i)) != null; i++) {
				LOGGER.debug("Adding file {} to Bag {}.", filename, pid);
				dcStorage.addFileToBag(pid, new File(uploadedFilesDir, filename), filename);
			}

			// Add External Reference URLs.
			Set<String> urls = new HashSet<String>();
			for (int i = 0; uploadProps.containsKey("url" + i); i++) {
				urls.add(uploadProps.getProperty("url" + i));
			}
			dcStorage.addExtRefs(pid, urls);

			// Save the access log record created earlier for the activity performed.
			new AccessLogRecordDAOImpl(AccessLogRecord.class).create(accessRec);
			resp = Response.temporaryRedirect(
					redirUri.path(UploadService.class, "doGetBagFileListingAsHtml")
							.queryParam("smsg", "Upload Successful.").build(pid)).build();
		} catch (Exception e) {
			LOGGER.error("Unable to bag file", e);
			resp = Response.temporaryRedirect(
					redirUri.path(UploadService.class, "doGetAsHtml").queryParam("pid", pid)
							.queryParam("emsg", "Upload Unsuccessful. Unable to bag file.").build()).build();
		} finally {
			// Delete properties file and uploaded files.
			IOUtils.closeQuietly(propFileInStream);
			if (!FileUtils.deleteQuietly(propFile))
				LOGGER.warn("Unable to delete properties file.");
			if (!FileUtils.deleteQuietly(uploadedFilesDir))
				LOGGER.warn("Unable to delete uploaded files.");
		}

		return resp;
	}

	/**
	 * Returns the details of contents of a bag.
	 * 
	 * @param pid
	 *            Pid of the collection whose bag details are returned
	 * @return Response as HTML including BagSummary object
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("bag/{pid}")
	public Response doGetBagFileListingAsHtml(@PathParam("pid") String pid, @QueryParam("task") String task) {
		Response resp = null;
		Map<String, Object> model = new HashMap<String, Object>();
		UriBuilder redirUri = UriBuilder.fromUri(uriInfo.getBaseUri()).path(UploadService.class);

		if (task == null || task.length() == 0) {
			FedoraObject fo = null;
			if (hasRole(new String[] { "ROLE_ANU_USER" })) {
				// Check if user's got read access to fedora object.
				fo = getFedoraObjectReadAccess(pid);
			} else if (hasRole(new String[] { "ROLE_ANONYMOUS" })) {
				// Check if data files are public
				fo = fedoraObjectService.getItemByPid(pid);
				if (!fo.getPublished() || !fo.isFilesPublic()) {
					throw new AccessDeniedException("No access");
				}
			}
			model.put("fo", fo);

			if (dcStorage.bagExists(pid)) {
				BagSummary bagSummary;
				try {
					bagSummary = dcStorage.getBagSummary(pid);
					model.put("bagSummary", bagSummary);
					model.put("bagInfoTxt", bagSummary.getBagInfoTxt().entrySet());
					if (bagSummary.getExtRefsTxt() != null)
						model.put("extRefsTxt", bagSummary.getExtRefsTxt().entrySet());
					UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(UploadService.class)
							.path(UploadService.class, "doGetFileInBagAsOctetStream2");
					model.put("dlBaseUri", uriBuilder.build(pid, "").toString());
					model.put("downloadAsZipUrl", uriBuilder.build(pid, "zip").toString());
					model.put("isFilesPublic", fo.isFilesPublic().booleanValue());
				} catch (DcStorageException e) {
					LOGGER.error(e.getMessage(), e);
					PageMessages messages = new PageMessages();
					messages.add(MessageType.ERROR, e.getMessage(), model);
				}
			}

			resp = Response.ok(new Viewable(BAGFILES_JSP, model), MediaType.TEXT_HTML_TYPE).build();
		} else if (task.equals("recomplete")) {
			getFedoraObjectWriteAccess(pid);
			try {
				dcStorage.recompleteBag(pid);
				resp = Response.temporaryRedirect(
						redirUri.path(UploadService.class, "doGetBagFileListingAsHtml")
								.queryParam("smsg", "Files rescanning commenced and will run in the background.").build(pid)).build();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				resp = Response.serverError().build();
			}
		}
		return resp;
	}
	
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Path("bag/{pid}")
	@PreAuthorize("hasRole('ROLE_ANU_USER')")
	public Response doGetBagSummary(@PathParam("pid") String pid) {
		Response resp = null;
		
		getFedoraObjectReadAccess(pid);
		if (!dcStorage.bagExists(pid)) {
			throw new NotFoundException(format("Bag not found for {0}", pid));
		}
		
		try {
			BagSummary bagSummary = dcStorage.getBagSummary(pid);
			resp = Response.ok(bagSummary).build();
		} catch (DcStorageException e) {
			resp = Response.serverError().build();
		}
		
		return resp;
	}

	/**
	 * Returns the contents of a file of a group of files combined into a ZipStream as InputStream in the Response
	 * object. The user gets a request to open or save the requested file. This method checks that the user requesting
	 * the file has a valid collection request.
	 * 
	 * @param pid
	 *            Pid of collection whose files
	 * @param filename
	 *            filename of the file being requested. E.g. "data/file.txt"
	 * @param dropboxAccessCode
	 *            Access Code of the dropbox that the requestor's been given access to
	 * @param password
	 *            Password of dropbox
	 * @return Response containing octet_stream of file or files as zipfile.
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("files/{pid}/{fileInBag:.*}")
	public Response doGetFileInBagAsOctetStream(@PathParam("pid") String pid, @PathParam("fileInBag") String filename,
			@QueryParam("dropboxAccessCode") Long dropboxAccessCode, @QueryParam("p") String password) {
		Response resp = null;

		LOGGER.trace("pid: {}, filename: {}", pid, filename);

		// Get dropbox requesting file.
		DropboxDAO dropboxDAO = new DropboxDAOImpl(CollectionDropbox.class);
		CollectionDropbox dropbox = dropboxDAO.getSingleByAccessCode(dropboxAccessCode);
		Users requestor = dropbox.getCollectionRequest().getRequestor();
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		// If dropbox is valid and the requestor of the collection request is the one accessing it, return file as octet
		// stream.
		if (dropbox.isValid(password) && requestor.getUsername().equals(username)) {
			LOGGER.info("Dropbox details valid. ID: {}, Access Code: {}. Returning file requested.", dropbox.getId()
					.toString(), dropbox.getAccessCode().toString());
			new AccessLogRecordDAOImpl(AccessLogRecord.class).create(new AccessLogRecord(uriInfo.getPath(),
					getCurUser(), request.getRemoteAddr(), request.getHeader("User-Agent"),
					AccessLogRecord.Operation.READ));
			Set<CollectionRequestItem> items = dropbox.getCollectionRequest().getItems();
			if (filename.equalsIgnoreCase("zip")) {
				Set<String> fileSet = new HashSet<String>();
				for (CollectionRequestItem item : items)
					fileSet.add(item.getItem());
				resp = getBagFilesAsZip(pid, fileSet, format("{0}.zip", DcStorage.convertToDiskSafe(pid)));
			} else {
				boolean isAllowedItem = false;
				for (CollectionRequestItem item : items) {
					if (item.getItem().equals(filename)) {
						isAllowedItem = true;
						break;
					}
				}
				if (isAllowedItem)
					resp = getBagFileOctetStreamResp(pid, filename);
				else
					resp = Response.status(Status.FORBIDDEN).build();
			}
		} else {
			LOGGER.warn("Unauthorised access to Dropbox ID: {}, Access Code: {}. Returning HTTP 403 Forbidden.",
					dropbox.getId().toString(), dropbox.getAccessCode().toString());
			resp = Response.status(Status.FORBIDDEN).build();
		}

		return resp;
	}

	/**
	 * Returns a file in a collection's bag as InputStream in Response.
	 * 
	 * @param pid
	 *            Pid of the collection containing the file
	 * @param fileRequested
	 *            File being requested. E.g. "data/File.txt"
	 * @return File contents as InputStream in Response
	 */
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("bag/{pid}/{fileInBag:.*}")
	public Response doGetFileInBagAsOctetStream2(@PathParam("pid") String pid,
			@PathParam("fileInBag") String fileRequested, @QueryParam("file") Set<String> filepaths) {
		LOGGER.trace("pid: {}, filename: {}", pid, fileRequested);
		Response resp = null;
		// Check for read access.
		getFedoraObjectReadAccess(pid);

		FedoraObject fo = null;
		if (hasRole(new String[] { "ROLE_ANU_USER" })) {
			// Check if user's got read access to fedora object.
			fo = getFedoraObjectReadAccess(pid);
		} else if (hasRole(new String[] { "ROLE_ANONYMOUS" })) {
			// Check if data files are public
			fo = fedoraObjectService.getItemByPid(pid);
			if (!fo.getPublished() || !fo.isFilesPublic()) {
				throw new AccessDeniedException("No access");
			}
		}

		try {
			Users curUser = getCurUser();
			if (curUser != null) {
				AccessLogRecord accessLogRecord = new AccessLogRecord(uriInfo.getPath(), curUser,
						request.getRemoteAddr(), request.getHeader("User-Agent"), AccessLogRecord.Operation.READ);
				new AccessLogRecordDAOImpl(AccessLogRecord.class).create(accessLogRecord);
			}
			
			if (fileRequested.equals("zip")) {
				FileSummaryMap fsMap = dcStorage.getBagSummary(pid).getFileSummaryMap();
				
				if (filepaths == null || filepaths.size() == 0) {
					filepaths.addAll(fsMap.keySet());
				} else {
					for (Iterator<String> it = filepaths.iterator(); it.hasNext(); ) {
						if (!fsMap.containsKey(it.next())) {
							it.remove();
						}
					}
				}
				
				resp = getBagFilesAsZip(pid, filepaths, format("{0}.{1}", DcStorage.convertToDiskSafe(pid), "zip"));
			} else {
				if (!dcStorage.fileExistsInBag(pid, fileRequested))
					throw new NotFoundException(format("File {0} not found in {1}", fileRequested, pid));
				resp = getBagFileOctetStreamResp(pid, fileRequested);
			}
		} catch (DcStorageException e) {
			resp = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

		return resp;
	}

	/**
	 * Adds a single file to a bag.
	 * 
	 * @param pid
	 *            Pid of the collection to which a file will be added
	 * @param fileInBag
	 *            Filename to be stored in the bag. For example, "data/File.txt"
	 * @param is
	 *            InputStream to read and save into the file
	 * @return HTTP response as Response
	 */
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("bag/{pid}/{fileInBag:.*}")
	@PreAuthorize("hasRole('ROLE_ANU_USER')")
	public Response doAddFileToBag(@PathParam("pid") String pid, @PathParam("fileInBag") String fileInBag,
			InputStream is) {
		Response resp = null;
		getFedoraObjectWriteAccess(pid);
		File uploadedFile = null;

		try {
			if (dcStorage.fileExistsInBag(pid, fileInBag))
				new AccessLogRecordDAOImpl(AccessLogRecord.class).create(new AccessLogRecord(uriInfo.getPath(),
						getCurUser(), request.getRemoteAddr(), request.getHeader("User-Agent"),
						AccessLogRecord.Operation.UPDATE));
			else
				new AccessLogRecordDAOImpl(AccessLogRecord.class).create(new AccessLogRecord(uriInfo.getPath(),
						getCurUser(), request.getRemoteAddr(), request.getHeader("User-Agent"),
						AccessLogRecord.Operation.CREATE));

			TempFileTask tfTask = new TempFileTask(is);
			if (httpHeaders.getRequestHeader("Content-MD5") != null) {
				String providedMd5 = httpHeaders.getRequestHeader("Content-MD5").get(0);
				if (providedMd5 != null && providedMd5.length() > 0) {
					tfTask.setExpectedMessageDigest(Manifest.Algorithm.MD5, providedMd5);
				}
			}
			uploadedFile = tfTask.call();
			dcStorage.addFileToBag(pid, uploadedFile, getFilenameFromPath(fileInBag));
			resp = Response.ok(tfTask.getCalculatedMd()).build();
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			resp = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} finally {
			FileUtils.deleteQuietly(uploadedFile);
		}
		return resp;
	}

	/**
	 * Deletes a file in a collection's bag.
	 * 
	 * @param pid
	 *            Collection which contains the file to be deleted.
	 * @param fileInBag
	 *            File to be deleted. E.g. "data/File.txt"
	 * @return Response with HTTP OK if successful, HTTP INTERNAL_SERVER_ERROR otherwise.
	 */
	@DELETE
	@Path("bag/{pid}/{fileInBag:.*}")
	@PreAuthorize("hasRole('ROLE_ANU_USER')")
	public Response doDeleteFileInBag(@PathParam("pid") String pid, @PathParam("fileInBag") String fileInBag) {
		Response resp = null;
		getFedoraObjectWriteAccess(pid);

		try {
			new AccessLogRecordDAOImpl(AccessLogRecord.class).create(new AccessLogRecord(uriInfo.getPath(),
					getCurUser(), request.getRemoteAddr(), request.getHeader("User-Agent"),
					AccessLogRecord.Operation.DELETE));
			dcStorage.deleteFileFromBag(pid, fileInBag);
			resp = Response.ok(format("File {0} deleted from {1}", fileInBag, pid)).build();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			resp = Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(format("Unable to delete file {0} from {1}", fileInBag, pid)).build();
		}
		return resp;
	}

	/**
	 * Adds and/or deletes a set of external reference URLs stored in a collection.
	 * 
	 * <p>
	 * URL format: <code>
	 * http://.../bag/test:123/extrefs?addUrl=http://http://www.add1.com&addUrl=http://www.add2.com&deleteUrl=http://delete1.com&deleteUrl=http://delete2.com
	 * </code>
	 * 
	 * @param pid
	 *            Pid of the collection whose external references are to be added/deleted.
	 * @param addUrlSet
	 *            Set of URLs to add as Set&lt;String&gt;
	 * @param deleteUrlSet
	 *            Set of URLs to delete as Set&lt;String&gt;
	 * @return Response with HTTP OK if successful, HTTP INTERNAL_SERVER_ERROR otherwise.
	 */
	@POST
	@Path("bag/{pid}/extrefs")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@PreAuthorize("hasRole('ROLE_ANU_USER')")
	public Response doAddDeleteExtRef(@PathParam("pid") String pid, @FormParam("addUrl") Set<String> addUrlSet,
			@FormParam("deleteUrl") Set<String> deleteUrlSet) {
		Response resp = null;

		getFedoraObjectWriteAccess(pid);
		try {
			if (addUrlSet != null && !addUrlSet.isEmpty())
				dcStorage.addExtRefs(pid, addUrlSet);
			if (deleteUrlSet != null && deleteUrlSet.isEmpty())
				dcStorage.deleteExtRefs(pid, deleteUrlSet);

			resp = Response.ok(format("Added {0} to {1}.", addUrlSet, pid)).build();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			resp = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

		return resp;
	}
	
	@GET
	@Path("bag/{pid}/complete")
	@Produces(MediaType.TEXT_PLAIN)
	@PreAuthorize("hasRole('ROLE_ANU_USER')")
	public Response doCompleteBag(@PathParam("pid") String pid) {
		Response resp = null;
		getFedoraObjectWriteAccess(pid);
		try {
			dcStorage.recompleteBag(pid);
			resp = Response.ok().build();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			resp = Response.serverError().build();
		}
		return resp;
	}

	/**
	 * Accepts a zipped bag for adding to a collection. If the specified collection already has a bag assigned, this bag
	 * replaces that bag.
	 * 
	 * @param pid
	 *            Pid of collection to which a bag will be added
	 * 
	 * @param is
	 *            Bag contents as InputStream
	 * @return HTTP OK if successful, HTTP INTERNAL_SERVER_ERROR otherwise.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Path("bag/{pid}")
	@PreAuthorize("hasRole('ROLE_ANU_USER')")
	public Response doPostBag(@PathParam("pid") String pid, InputStream is) {
		// Check for write access to the fedora object.
		getFedoraObjectWriteAccess(pid);

		Response resp = null;
		AccessLogRecord accessRec = null;
		File uploadedFile = null;
		try {
			uploadedFile = File.createTempFile("Rep", null, GlobalProps.getUploadDirAsFile());
			LOGGER.info("Saving uploaded file as {}...", uploadedFile.getAbsolutePath());
			saveInputStreamAsFile(is, uploadedFile);
			LOGGER.info("Uploaded file saved as {}", uploadedFile.getAbsolutePath());

			// Check if a current bag exists. If yes, replace, else saveAs.
			if (dcStorage.bagExists(pid)) {
				LOGGER.info("A bag exists for {}. Replacing it with the file uploaded...", pid);
				accessRec = new AccessLogRecord(uriInfo.getPath(), getCurUser(), request.getRemoteAddr(),
						request.getHeader("User-Agent"), AccessLogRecord.Operation.UPDATE);
			} else {
				LOGGER.info("No bag exists for {}. Storing Bag...", pid);
				accessRec = new AccessLogRecord(uriInfo.getPath(), getCurUser(), request.getRemoteAddr(),
						request.getHeader("User-Agent"), AccessLogRecord.Operation.CREATE);
			}
			dcStorage.storeBag(pid, uploadedFile);
			new AccessLogRecordDAOImpl(AccessLogRecord.class).create(accessRec);
			LOGGER.info("Bag updated for {}", pid);
			resp = Response.ok().build();
		} catch (Exception e) {
			LOGGER.error("Unable to upload bag.", e);
			resp = Response.serverError().entity(e.toString()).type(MediaType.TEXT_PLAIN).build();
		} finally {
			// Delete uploaded file.
			if (uploadedFile != null && uploadedFile.exists())
				if (!FileUtils.deleteQuietly(uploadedFile))
					LOGGER.warn("Unable to delete temp file {}.", uploadedFile.getAbsolutePath());
		}

		return resp;
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("bag/{pid}/ispublic")
	public Response doGetIsFilesPublic(@PathParam("pid") String pid) {
		Response resp = null;
		boolean isFilesPublic = fedoraObjectService.isFilesPublic(pid);
		resp = Response.ok(Boolean.toString(isFilesPublic)).build();
		return resp;
	}

	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("bag/{pid}/ispublic")
	@PreAuthorize("hasRole('ROLE_ANU_USER')")
	public Response doPutSetFilesPublic(@PathParam("pid") String pid, String isFilesPublic) {
		Response resp = null;
		if (isFilesPublic == null || isFilesPublic.length() == 0) {
			resp = Response.status(Status.BAD_REQUEST).build();
		} else {
			FedoraObject fedoraObject = fedoraObjectService.getItemByPid(pid);
			if (!permissionService.checkPermission(fedoraObject, CustomACLPermission.PUBLISH)) {
				throw new AccessDeniedException("User does not have Publish permissions.");
			}
			fedoraObjectService.setFilesPublic(pid, Boolean.parseBoolean(isFilesPublic));
			resp = Response.ok().build();
		}
		return resp;
	}

	/**
	 * Returns information about the current logged on user in the format username:displayName. E.g.
	 * "u1234567:John Smith"
	 * 
	 * @return Response including user information As String.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("userinfo")
	@PreAuthorize("hasRole('ROLE_ANU_USER')")
	public Response doGetUserInfo() {
		Users curUser = getCurUser();
		Response resp = null;
		StringBuilder respEntity = new StringBuilder();
		respEntity.append(curUser.getUsername());
		respEntity.append(":");
		respEntity.append(curUser.getDisplayName());
		resp = Response.ok(respEntity.toString()).build();
		return resp;
	}

	/**
	 * Saves an InputStream to a File on disk.
	 * 
	 * @param is
	 *            InputStream from which data will be read.
	 * @param target
	 *            target as File to which data will be written.
	 * @throws IOException
	 *             if unable to read from inputstream or write file to disk.
	 */
	private void saveInputStreamAsFile(InputStream is, File target) throws IOException {
		FileChannel targetChannel = null;
		FileOutputStream fos = null;
		ReadableByteChannel sourceChannel = null;
		try {
			fos = new FileOutputStream(target);
			targetChannel = fos.getChannel();
			sourceChannel = Channels.newChannel(is);
			ByteBuffer buffer = ByteBuffer.allocate((int) FileUtils.ONE_MB);
			while (sourceChannel.read(buffer) != -1) {
				buffer.flip();
				targetChannel.write(buffer);
				buffer.compact();
			}

			buffer.flip();
			while (buffer.hasRemaining())
				targetChannel.write(buffer);
		} finally {
			IOUtils.closeQuietly(sourceChannel);
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(targetChannel);
			IOUtils.closeQuietly(fos);
		}
	}

	/**
	 * Gets a Users object containing information about the currently logged in user.
	 * 
	 * @return Users object containing information about the currently logged in user.
	 */
	private Users getCurUser() {
		return new UsersDAOImpl(Users.class).getUserByName(SecurityContextHolder.getContext().getAuthentication()
				.getName());
	}

	/**
	 * Creates a Response object containing the contents of a single file in a bag of collection as Response object
	 * containing InputStream.
	 * 
	 * @param pid
	 *            Pid of the collection from which a bagfile is to be read.
	 * @param fileInBag
	 *            Name of file in bag whose contents are to be returned as InputStream.
	 * @return Response object including HTTP headers and InputStream containing file contents.
	 */
	private Response getBagFileOctetStreamResp(String pid, String fileInBag) {
		Response resp = null;
		InputStream is = null;

		if (!dcStorage.fileExistsInBag(pid, fileInBag))
			throw new NotFoundException(format("File {} not found in {}", fileInBag, pid));

		try {
			is = dcStorage.getFileStream(pid, fileInBag);
			ResponseBuilder respBuilder = Response.ok(is, MediaType.APPLICATION_OCTET_STREAM_TYPE);
			// Add filename, MD5 and file size to response header.
			respBuilder = respBuilder.header("Content-Disposition",
					format("attachment; filename=\"{0}\"", getFilenameFromPath(fileInBag)));
			respBuilder = respBuilder.header("Content-MD5", dcStorage.getFileMd5(pid, fileInBag));
			respBuilder = respBuilder.header("Content-Length", dcStorage.getFileSize(pid, fileInBag));
			resp = respBuilder.build();
		} catch (DcStorageException e) {
			LOGGER.error(e.getMessage());
			throw new NotFoundException(e.getMessage());
		}

		return resp;
	}

	/**
	 * Creates a Response object containing a Zip file comprised of data from multiple files in a bag of a collection.
	 * 
	 * @param pid
	 *            Pid of the collection whose files are to be included in the Response object.
	 * 
	 * @param fileSet
	 *            Set of file names as Set&lt;String&gt; that are to be included in the Response.
	 * @param zipFilename
	 *            Name of the zip file that will be added to the Content-Disposition HTTP header.
	 * @return Response object
	 */
	private Response getBagFilesAsZip(String pid, Set<String> fileSet, String zipFilename) {
		Response resp = null;
		InputStream zipStream;
		try {
			zipStream = dcStorage.getFilesAsZipStream(pid, fileSet);
			ResponseBuilder respBuilder = Response.ok(zipStream, MediaType.APPLICATION_OCTET_STREAM_TYPE);
			respBuilder.header("Content-Disposition", format("attachment; filename=\"{0}\"", zipFilename));
			resp = respBuilder.build();
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new NotFoundException(e.getMessage());
		}

		return resp;
	}

	/**
	 * Checks if the currently logged in user has READ permissions for the specified record.
	 * 
	 * @param pid
	 *            Pid of the record
	 * 
	 * @return FedoraObject representing the record with the Pid specified.
	 */
	@PostAuthorize("hasPermission(returnObject, 'READ')")
	private FedoraObject getFedoraObjectReadAccess(String pid) {
		return getFedoraObject(pid);
	}

	/**
	 * Checks if the currently logged in user has READ permissions for the specified record.
	 * 
	 * @param pid
	 *            Pid of the record
	 * 
	 * @return FedoraObject representing the record with the Pid specified.
	 */
	@PostAuthorize("hasPermission(returnObject, 'WRITE')")
	private FedoraObject getFedoraObjectWriteAccess(String pid) {
		return getFedoraObject(pid);
	}

	/**
	 * 
	 * @param pid
	 * @return
	 */
	private FedoraObject getFedoraObject(String pid) {
		String decodedpid = null;
		decodedpid = Util.decodeUrlEncoded(pid);
		if (decodedpid == null) {
			return null;
		}
		FedoraObjectDAOImpl object = new FedoraObjectDAOImpl(FedoraObject.class);
		FedoraObject fo = (FedoraObject) object.getSingleByName(decodedpid);
		return fo;
	}

	/**
	 * saveFileOnServer
	 * 
	 * Australian National University Data Commons
	 * 
	 * Saves a file in a FileItem object on the server.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		14/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @param fileItem
	 *            FileItem object
	 * @param partNum
	 *            -1 if the file is a complete file, else a number from 0-(n-1) where n is the number of parts a file is
	 *            split up into.
	 * @param pid
	 * @throws IOException
	 * @throws Exception
	 *             Thrown when unable to save the file on the server.
	 */
	private File saveFileOnServer(FileItem fileItem, int partNum, boolean isLastPart, String pid, String expectedMd5)
			throws IOException {
		String filename = fileItem.getName();
		String serverFilename = getFilenameFromPath(filename);

		LOGGER.debug("filename: {}, filesize: {}.", filename, fileItem.getSize());

		// Append .part[n] to the filename if its a part file.
		if (partNum > 0)
			serverFilename += PART_FILE_SUFFIX + partNum;

		BufferedInputStream uploadedFileStream = null;
		BufferedInputStream mergedFileStream = null;
		File pidDir = new File(GlobalProps.getUploadDirAsFile(), DcStorage.convertToDiskSafe(pid));
		if (!pidDir.exists())
			pidDir.mkdirs();
		File fileOnServer = new File(pidDir, serverFilename);
		try {
			// Check if file already exists in pid dir. If not, write the file. Otherwise check if the file on server's
			// the same size as the one uploaded. If not, write it.
			if (!fileOnServer.exists() || (fileOnServer.exists() && fileOnServer.length() != fileItem.getSize())) {
				LOGGER.debug("Writing file {}", fileOnServer.getAbsolutePath());
				uploadedFileStream = new BufferedInputStream(fileItem.getInputStream(), (int) FileUtils.ONE_MB);
				saveInputStreamAsFile(uploadedFileStream, fileOnServer);
				if (isLastPart) // If last part then merge part files.
				{
					File mergedFile = processFinalPartFile(fileItem, partNum, pidDir);
					mergedFileStream = new BufferedInputStream(new FileInputStream(mergedFile), (int) FileUtils.ONE_MB);
					String computedMd5Sum = DigestUtils.md5Hex(mergedFileStream);
					if (!computedMd5Sum.equalsIgnoreCase(expectedMd5))
						throw new IOException(format(
								"Computed MD5 {0} does not match expected {1} for uploaded file {2}", computedMd5Sum,
								expectedMd5, mergedFile.getAbsolutePath()));
					fileOnServer = mergedFile;
				}
			} else {
				LOGGER.warn("File {} exists on server with same size. Not overwriting it.",
						fileOnServer.getAbsoluteFile());
			}
		} finally {
			IOUtils.closeQuietly(uploadedFileStream);
			IOUtils.closeQuietly(mergedFileStream);
		}

		return fileOnServer;
	}

	/**
	 * processFinalPartFile
	 * 
	 * Australian National University Data Commons
	 * 
	 * This method is called after the final part of a file has been saved on the server. This method merges the parts
	 * into a single file.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		14/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @param fileItem
	 *            FileItem object.
	 * @param partNum
	 *            Part number has int
	 * @param pidDir
	 * @throws IOException
	 * @throws Exception
	 *             Thrown if the files could not be merged.
	 */
	private File processFinalPartFile(FileItem fileItem, int partNum, File pidDir) throws IOException {
		BufferedInputStream partInStream = null;
		File[] partFiles = new File[partNum];

		// Open a FileOutputStream in the Target directory where the merged file will be placed.
		File mergedFile = new File(pidDir, fileItem.getName());
		BufferedOutputStream mergedFileStream = new BufferedOutputStream(new FileOutputStream(mergedFile));

		LOGGER.debug("Processing final file part# {}", partNum);

		try {
			// Merge individual file parts.
			LOGGER.debug("Merging file parts...");
			for (int i = 1; i <= partNum; i++) {
				// Open the FileInputStream to read from the part file.
				partFiles[i - 1] = new File(pidDir, fileItem.getName() + PART_FILE_SUFFIX + i);
				partInStream = new BufferedInputStream(new FileInputStream(partFiles[i - 1]), (int) FileUtils.ONE_MB);

				// Read bytes and add them to the merged file until all files in this file part have been merged.
				try {
					byte[] buffer = new byte[(int) FileUtils.ONE_MB];
					int numBytesRead;
					while ((numBytesRead = partInStream.read(buffer)) != -1)
						mergedFileStream.write(buffer, 0, numBytesRead);
				} finally {
					IOUtils.closeQuietly(partInStream);
				}
			}

			LOGGER.debug("Merged into {}", pidDir + File.separator + fileItem.getName());
		} finally {
			IOUtils.closeQuietly(mergedFileStream);

			// Delete the part files now that they've been merged.
			for (int i = 0; i < partFiles.length; i++) {
				if (!partFiles[i].delete())
					LOGGER.warn("Unable to delete part file {}.", partFiles[i].getAbsolutePath());
			}
		}

		return mergedFile;
	}

	/**
	 * getFilenameFromPath
	 * 
	 * Australian National University Data Commons
	 * 
	 * Gets the filename part from a full filename on the client that may contain file separators different from the
	 * ones used on the server.
	 * 
	 * <pre>
	 * Version	Date		Developer			Description
	 * 0.1		14/05/2012	Rahul Khanna (RK)	Initial
	 * </pre>
	 * 
	 * @param fullFilename
	 *            Full path and filename as on the client's computer.
	 * @return Filename only as String.
	 */
	private String getFilenameFromPath(String fullFilename) {
		// Extract the type of slash being used in the filename.
		char clientSlashType = (fullFilename.lastIndexOf("\\") > 0) ? '\\' : '/';

		// Get the index where the filename starts. -1 if the path isn't specified.
		int clientFilenameStartIndex = fullFilename.lastIndexOf(clientSlashType);

		// Get the part of the string after the last instance of the path separator.
		String filename = fullFilename.substring(clientFilenameStartIndex + 1, fullFilename.length());
		return filename;
	}

	/**
	 * Parses an HttpServletRequest and returns a list of FileItem objects. A fileItem can contain form data or a file
	 * that was uploaded by a user.
	 * 
	 * @param request
	 *            HttpServletRequest object to parse.
	 * @return FileItem objects as List&lt;FileItem&gt;
	 * @throws FileUploadException
	 */
	@SuppressWarnings("unchecked")
	private List<FileItem> parseUploadRequest(HttpServletRequest request) throws FileUploadException {
		// Create a new file upload handler.
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory(GlobalProps.getMaxSizeInMem(),
				GlobalProps.getUploadDirAsFile()));
		return (List<FileItem>) upload.parseRequest(request);
	}

	protected boolean hasRole(String[] roles) {
		boolean hasRole = false;
		for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
			String userRole = authority.getAuthority();
			for (String role : roles) {
				if (role.equals(userRole)) {
					hasRole = true;
					break;
				}
			}

			if (hasRole) {
				break;
			}
		}

		return hasRole;
	}
}
