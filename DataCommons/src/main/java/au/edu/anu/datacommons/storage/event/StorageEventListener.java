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

package au.edu.anu.datacommons.storage.event;

import static java.text.MessageFormat.format;
import gov.loc.repository.bagit.Bag.BagConstants;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.impl.AbstractBagConstants;
import gov.loc.repository.bagit.impl.BagItTxtImpl;
import gov.loc.repository.bagit.utilities.FilenameHelper;
import gov.loc.repository.bagit.v0_95.impl.BagInfoTxtImpl;
import gov.loc.repository.bagit.v0_97.impl.BagConstantsImpl;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.h2.store.fs.FilePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.edu.anu.datacommons.data.db.model.FedoraObject;
import au.edu.anu.datacommons.security.service.FedoraObjectService;
import au.edu.anu.datacommons.storage.DcStorage;
import au.edu.anu.datacommons.storage.archive.ArchiveTask;
import au.edu.anu.datacommons.storage.archive.ArchiveTask.Operation;
import au.edu.anu.datacommons.storage.completer.fido.FidoParser;
import au.edu.anu.datacommons.storage.event.tasks.BagCompletionTask;
import au.edu.anu.datacommons.storage.event.tasks.ManifestMd5Task;
import au.edu.anu.datacommons.storage.event.tasks.MetadataTask;
import au.edu.anu.datacommons.storage.event.tasks.PreservationTask;
import au.edu.anu.datacommons.storage.event.tasks.PronomTask;
import au.edu.anu.datacommons.storage.event.tasks.StorageSearchIndexTask;
import au.edu.anu.datacommons.storage.event.tasks.TimestampTask;
import au.edu.anu.datacommons.storage.event.tasks.VirusScanTask;
import au.edu.anu.datacommons.storage.info.RecordDataInfoService;
import au.edu.anu.datacommons.storage.search.StorageSearchService;
import au.edu.anu.datacommons.storage.tagfiles.BagInfoTagFile;
import au.edu.anu.datacommons.storage.tagfiles.BagItTagFile;
import au.edu.anu.datacommons.storage.tagfiles.FileMetadataTagFile;
import au.edu.anu.datacommons.storage.tagfiles.ManifestMd5TagFile;
import au.edu.anu.datacommons.storage.tagfiles.PreservationMapTagFile;
import au.edu.anu.datacommons.storage.tagfiles.PronomFormatsTagFile;
import au.edu.anu.datacommons.storage.tagfiles.TagFilesService;
import au.edu.anu.datacommons.storage.tagfiles.TimestampsTagFile;
import au.edu.anu.datacommons.storage.tagfiles.VirusScanTagFile;
import au.edu.anu.datacommons.tasks.ThreadPoolService;
import au.edu.anu.datacommons.util.Util;

/**
 * @author Rahul Khanna
 * 
 */
@Component
public class StorageEventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageEventListener.class);

	@Autowired(required = true)
	ThreadPoolService threadPoolSvc;
	
	@Autowired(required = true)
	private RecordDataInfoService rdiSvc;
	
	@Autowired(required = true)
	TagFilesService tagFilesSvc;
	
	@Resource(name = "fedoraObjectServiceImpl")
	FedoraObjectService fedoraObjectService;
	
	@Autowired
	StorageSearchService searchSvc;
	
	@Autowired
	DcStorage dcStorage;

	private Path archiveRootDir;
	
	public enum EventTime {
		PRE, POST
	};

	public enum EventType {
		ADD_FILE, READ_FILE, UPDATE_FILE, DELETE_FILE, TAGFILE_UPDATE;
		
		public boolean isOneOf(EventType... types) {
			for (EventType iType : types) {
				if (this.equals(iType)) {
					return true;
				}
			}
			return false;
		}
	};

	public void setArchiveRootDir(Path archiveRootDir) {
		this.archiveRootDir = archiveRootDir;
	}

	public void notify(EventTime time, EventType type, String pid, Path bagDir, String relPath) throws IOException {
		relPath = normalizeRelPath(relPath);
		if (time == EventTime.PRE) {
			processPreEventTasks(type, pid, bagDir, relPath);
		} else if (time == EventTime.POST) {
			processPostEventTasks(type, pid, bagDir, relPath);
		} else {
			throw new RuntimeException(format("Unexpected event time - {0}", time));
		}
			
	}

	private void processPreEventTasks(EventType type, String pid, Path bagDir, String relPath) throws IOException {
		if (type.isOneOf(EventType.READ_FILE, EventType.UPDATE_FILE, EventType.DELETE_FILE)) {
			verifyFileExists(pid, bagDir, relPath);
		}
		if (type.isOneOf(EventType.ADD_FILE)) {
			createParentPath(pid, bagDir, relPath);
		}
		if (type.isOneOf(EventType.UPDATE_FILE, EventType.DELETE_FILE)) {
			if (!hasHiddenParts(relPath)) {
				archive(type, pid, bagDir, relPath);
			} else {
				deleteFileAndEmptyParents(bagDir, relPath);
			}
		}
	}
	
	private void processPostEventTasks(EventType type, final String pid, Path bagDir, String relPath) throws IOException {
		List<Future<?>> waitList = new ArrayList<>();
		if (type.isOneOf(EventType.ADD_FILE, EventType.UPDATE_FILE)) {
			waitList.add(threadPoolSvc.submit(new PreservationTask(pid, bagDir, relPath, tagFilesSvc, dcStorage)));
			
			waitList.add(threadPoolSvc.submit(new ManifestMd5Task(pid, bagDir, relPath, tagFilesSvc)));
			waitList.add(threadPoolSvc.submit(new MetadataTask(pid, bagDir, relPath, tagFilesSvc)));
			waitList.add(threadPoolSvc.submit(new PronomTask(pid, bagDir, relPath, tagFilesSvc)));
			waitList.add(threadPoolSvc.submit(new TimestampTask(pid, bagDir, relPath, tagFilesSvc)));
			waitList.add(threadPoolSvc.submit(new VirusScanTask(pid, bagDir, relPath, tagFilesSvc)));
			
			if (searchSvc != null && isPublishedAndPublic(fedoraObjectService.getItemByPid(pid))) {
				threadPoolSvc.submit(new StorageSearchIndexTask(pid, bagDir, relPath, searchSvc));
			}

			threadPoolSvc.submitCachedPool(new BagCompletionTask(pid, bagDir, relPath, tagFilesSvc, rdiSvc, waitList));
			touchBagDir(bagDir);
		}
		if (type.isOneOf(EventType.DELETE_FILE)) {
			String dataPrependedRelPath = "data/" + relPath;
			
			String presvRelpath = tagFilesSvc.getEntryValue(pid, PreservationMapTagFile.class, dataPrependedRelPath);
			if (presvRelpath != null && !presvRelpath.equals("UNCONVERTIBLE")) {
				tagFilesSvc.removeEntry(pid, PreservationMapTagFile.class, presvRelpath);
				dcStorage.processDeleteFile(pid, presvRelpath.replaceFirst("^data/", ""));
			}
			tagFilesSvc.removeEntry(pid, PreservationMapTagFile.class, dataPrependedRelPath);
			
			tagFilesSvc.removeEntry(pid, ManifestMd5TagFile.class, dataPrependedRelPath);
			tagFilesSvc.removeEntry(pid, FileMetadataTagFile.class, dataPrependedRelPath);
			tagFilesSvc.removeEntry(pid, PreservationMapTagFile.class, dataPrependedRelPath);
			tagFilesSvc.removeEntry(pid, PronomFormatsTagFile.class, dataPrependedRelPath);
			tagFilesSvc.removeEntry(pid, TimestampsTagFile.class, dataPrependedRelPath);
			tagFilesSvc.removeEntry(pid, VirusScanTagFile.class, dataPrependedRelPath);
			
			// If a file's deleted, its search index entry must be deleted irrespective of
			// published status.
			if (searchSvc != null) {
				threadPoolSvc.submit(new StorageSearchIndexTask(pid, bagDir, relPath, searchSvc));
			}
			
			threadPoolSvc.submitCachedPool(new BagCompletionTask(pid, bagDir, relPath, tagFilesSvc, rdiSvc, waitList));			
			touchBagDir(bagDir);
		}
		
		if (type.isOneOf(EventType.TAGFILE_UPDATE)) {
			threadPoolSvc.submitCachedPool(new BagCompletionTask(pid, bagDir, relPath, tagFilesSvc, rdiSvc, waitList));
			touchBagDir(bagDir);
		}
	}

	private void initBagIt(String pid, Path bagDir, String relPath) throws IOException {
		Map<String, String> bagItEntries = tagFilesSvc.getAllEntries(pid, BagItTagFile.class);
		if (bagItEntries.size() != 2) {
			addBagItEntries(pid);
		}
	}

	private void addBagItEntries(String pid) throws IOException {
		// Must clear all entries to ensure insertion order.
		tagFilesSvc.clearAllEntries(pid, BagItTagFile.class);
		tagFilesSvc.addEntry(pid, BagItTagFile.class, BagItTxtImpl.VERSION_KEY, Version.V0_97.versionString);
		tagFilesSvc.addEntry(pid, BagItTagFile.class, BagItTxtImpl.CHARACTER_ENCODING_KEY, AbstractBagConstants.BAG_ENCODING);
	}
	
	private String normalizeRelPath(String relPath) throws IOException {
		if (relPath != null) {
			StringBuilder processed = new StringBuilder(FilenameHelper.normalizePathSeparators(relPath));
			while (processed.charAt(0) == '/') {
				processed.deleteCharAt(0);
			}
			
			while (processed.charAt(processed.length() - 1) == '/') {
				processed.deleteCharAt(processed.length() - 1);
			}
			
			if (LOGGER.isTraceEnabled() && !processed.toString().equals(relPath)) {
				LOGGER.trace("Normalized relative path {} to {}", relPath, processed.toString());
			}
			return processed.toString();
		} else {
			return null;
		}
	}
	
	private void verifyFileExists(String pid, Path bagDir, String relPath) throws FileNotFoundException {
		Path targetFile = getPayloadDir(bagDir).resolve(relPath);
		if (!Files.isRegularFile(targetFile)) {
			throw new FileNotFoundException(format("File {0}/data/{1} does not exist.", pid, relPath));
		}
	}
	
	private boolean hasHiddenParts(String relPath) {
		Path relPathAsPath = Paths.get(relPath);
		for (int i = 0; i < relPathAsPath.getNameCount(); i++) {
			if (relPathAsPath.getName(i).toString().startsWith(".")) {
				return true;
			}
		}
		return false;
	}

	private void createParentPath(String pid, Path bagDir, String relPath) throws IOException {
		Path targetFile = getPayloadDir(bagDir).resolve(relPath);
		if (!Files.isDirectory(bagDir.getParent())) {
			throw new IllegalStateException(format("Bag Roots directory {0} doesn''t exist.", bagDir.getParent()
					.toAbsolutePath().toString()));
		}
		if (!Files.isDirectory(bagDir)) {
			initBagIt(pid, bagDir, relPath);
		}
		Files.createDirectories(targetFile.getParent());
	}

	private void archive(EventType type, String pid, Path bagDir, String relPath) throws IOException {
		Path fileToArchive = getPayloadDir(bagDir).resolve(relPath);
		if (this.archiveRootDir != null) {
			LOGGER.info("Archiving {}/data/{} ({}) for event {}", pid, relPath, Util.byteCountToDisplaySize(Files.size(fileToArchive)), type.toString());
			Operation op = type == EventType.UPDATE_FILE ? ArchiveTask.Operation.REPLACE : ArchiveTask.Operation.DELETE;
			ArchiveTask archiveTask = new ArchiveTask(this.archiveRootDir.toFile(), pid, fileToArchive.toFile(), Algorithm.MD5, op);
			threadPoolSvc.submit(archiveTask);
		} else {
			Files.delete(fileToArchive);
			LOGGER.warn("Archive directory not specified. {}/data/{} deleted.", pid, relPath);
		}		
	}

	private Path getPayloadDir(Path bagDir) {
		return bagDir.resolve("data/");
	}
	
	private boolean isPublishedAndPublic(FedoraObject fo) {
		return fo.getPublished() && fo.isFilesPublic();
	}

	/**
	 * Deletes a file and its parent directories upto, but excluding the payload directory, if they're empty.
	 * 
	 * @param bagDir
	 *            Directory of the bag directory
	 * @param relPath
	 *            Relative path of a file from the payload directory i.e. without "data/" prefix
	 * @throws IOException
	 */
	private void deleteFileAndEmptyParents(Path bagDir, String relPath) throws IOException {
		Path targetFile = getPayloadDir(bagDir).resolve(relPath);
		Files.deleteIfExists(targetFile);
		boolean isEmptyDir;
		for (Path parent = targetFile.getParent(); !parent.equals(getPayloadDir(bagDir)); parent = parent.getParent()) {
			try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(parent)) {
				isEmptyDir = !dirStream.iterator().hasNext();
			}
			if (isEmptyDir) {
				// Must delete dir after directory stream's closed.
				Files.deleteIfExists(parent);
			} else {
				break;
			}
		}
	}

	private void touchBagDir(Path bagDir) throws IOException {
		if (bagDir != null && Files.isDirectory(bagDir)) {
			Files.setLastModifiedTime(bagDir, FileTime.fromMillis(new Date().getTime()));
		}
	}
}
