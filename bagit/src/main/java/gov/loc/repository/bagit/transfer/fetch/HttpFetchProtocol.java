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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import static java.text.MessageFormat.format;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;

import gov.loc.repository.bagit.transfer.BagTransferException;
import gov.loc.repository.bagit.transfer.FetchContext;
import gov.loc.repository.bagit.transfer.FetchProtocol;
import gov.loc.repository.bagit.transfer.FetchedFileDestination;
import gov.loc.repository.bagit.transfer.FileFetcher;
import gov.loc.repository.bagit.utilities.LongRunningOperationBase;

public class HttpFetchProtocol implements FetchProtocol
{
    private static final Log log = LogFactory.getLog(HttpFetchProtocol.class);
    
	private ThreadSafeClientConnManager connectionManager;
    private final DefaultHttpClient client;
    
    public HttpFetchProtocol()
    {
        this.connectionManager = new ThreadSafeClientConnManager();
        this.client = new DefaultHttpClient(this.connectionManager);
        this.client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "BagIt Library Parallel Fetcher");
        //this.state = new HttpState();

        // Since we control the threading manually, set the max
        // configuration values to Very Large Numbers.
        this.connectionManager.setDefaultMaxPerRoute(Integer.MAX_VALUE);
        this.connectionManager.setMaxTotal(Integer.MAX_VALUE);

    	// Set the socket timeout, so that it does not default to infinity.
    	// Otherwise, broken TCP steams can hang threads forever.
        client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 20 * 1000);
		client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20 * 1000);
		
        // If there are credentials present, then set up for preemptive authentication.
        PasswordAuthentication auth = Authenticator.requestPasswordAuthentication("remote", null, 80, "http", "", "scheme");
        
        if (auth != null)
        {
        	log.debug(format("Setting premptive authentication using username and password: {0}/xxxxx", auth.getUserName()));
        	this.client.getCredentialsProvider().setCredentials(
        			AuthScope.ANY,
        			new UsernamePasswordCredentials(auth.getUserName(), new String(auth.getPassword())));
        	HttpClientParams.setAuthenticating(this.client.getParams(), true);
        }
        else
        {
        	HttpClientParams.setAuthenticating(this.client.getParams(), false);
        }
        
        // There's no state in this class right now, so just
        // return the same one over and over.
        this.instance = new HttpFetcher();
    }
    
	public void setRelaxedSsl(boolean relaxedSsl)
	{
		SSLSocketFactory sf;
		if (relaxedSsl) {
			try {
				sf = new SSLSocketFactory(
				        new TrustSelfSignedStrategy(),
				        SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (KeyManagementException e) {
				throw new RuntimeException(e);
			} catch (UnrecoverableKeyException e) {
				throw new RuntimeException(e);
			} catch (KeyStoreException e) {
				throw new RuntimeException(e);
			}
		} else {
			sf = SSLSocketFactory.getSocketFactory();
		}
		Scheme https = new Scheme("https", 443, sf);

		this.connectionManager.getSchemeRegistry().register(https);
	}
    
    @Override
    public FileFetcher createFetcher(URI uri, Long size) throws BagTransferException
    {
        return this.instance;
    }

    private final HttpFetcher instance;
    
    private class HttpFetcher extends LongRunningOperationBase implements FileFetcher
    {
    	public void initialize() throws BagTransferException
    	{
    	}
    	
    	public void close()
    	{
    	}
    	
        @Override
        public void fetchFile(URI uri, Long size, FetchedFileDestination destination, FetchContext context) throws BagTransferException
        {
            log.trace(format("Fetching {0} to destination {1}", uri, destination.getFilepath()));
            
            HttpGet method = new HttpGet(uri);
            
            InputStream in = null;
            OutputStream out = null;
            
            try
            {
                log.trace("Executing GET");
                HttpResponse resp = client.execute(method);
                log.trace(format("Server said: {0}", resp.getStatusLine().toString()));
                
                if (resp.getStatusLine() == null || resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
                    throw new BagTransferException(format("Server returned code {0}: {1}", resp.getStatusLine() != null ? resp.getStatusLine().getStatusCode() : "nothing", uri));

                log.trace("Opening destination.");
                out = destination.openOutputStream(false);
                in = resp.getEntity().getContent();
                
                log.trace("Copying from network to destination.");
                FetchStreamCopier copier = new FetchStreamCopier("Downloading", uri, size);
                this.delegateProgress(copier);
                long bytesCopied = copier.copy(in, out);
                log.trace(format("Successfully copied {0} bytes.", bytesCopied));
            }
            catch (IOException e)
            {
            	log.warn("Caught IOException.", e);
                throw new BagTransferException(format("Could not transfer URI: {0}", uri), e);
            }
            catch (RuntimeException e)
            {
            	log.warn("Caught RuntimeException.", e);
            	method.abort();
                throw new BagTransferException(format("Could not transfer URI: {0}", uri), e);
            }            
            finally
            {                
                log.trace("Closing input stream.");
                IOUtils.closeQuietly(in);

                log.trace("Closing output stream.");
                IOUtils.closeQuietly(out);
                
                log.trace("Exiting finally clause.");
            }
        }
    }
}
