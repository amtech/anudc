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

package gov.loc.repository.bagit.transfer.fetch;

import static java.text.MessageFormat.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.transfer.BagTransferException;
import gov.loc.repository.bagit.transfer.FetchContext;
import gov.loc.repository.bagit.transfer.FetchProtocol;
import gov.loc.repository.bagit.transfer.FetchedFileDestination;
import gov.loc.repository.bagit.transfer.FileFetcher;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;

public class LocalFileFetchProtocol implements FetchProtocol
{
	private static final Log log = LogFactory.getLog(LocalFileFetchProtocol.class);
	private static final LocalFileFetcher singleton = new LocalFileFetcher();
	
	@Override
	public FileFetcher createFetcher(URI uri, Long size) throws BagTransferException 
	{
		return singleton;
	}
	
	private static class LocalFileFetcher extends LongRunningOperationBase implements FileFetcher
	{
		@Override
		public void initialize() throws BagTransferException 
		{
			// Do nothing.
		}
		
		@Override
		public void close() 
		{
			// Do nothing.
		}
		
		@Override
		public void fetchFile(URI uri, Long size, FetchedFileDestination destination, FetchContext context) throws BagTransferException 
		{
			final String srcPath = uri.getPath();
			final String dstPath = destination.getFilepath();
			
			log.trace(format("Fetching local file from {0} to {1}", srcPath, dstPath));
			
			File src = new File(srcPath);
			InputStream in = null;
			OutputStream out = null;
			
			try
			{
				log.trace(format("Opening input file: {0}", src.getAbsolutePath()));
				in = new FileInputStream(src);
				
				log.trace(format("Opening output file: {0}", dstPath));
				out = destination.openOutputStream(false);
				
				log.trace("Copying...");
				FetchStreamCopier copier = new FetchStreamCopier("Copying", srcPath, src.length());
				this.delegateProgress(copier);
				long bytesCopied = copier.copy(in, out);
				log.trace(format("Successfully copied {0} bytes.", bytesCopied));
			}
			catch (IOException e)
			{
				throw new BagTransferException(format("Unable to copy from \"{0}\" to \"{1}\".", srcPath, dstPath), e);
			}
			finally
			{
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		}
	}
}
