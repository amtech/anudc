package au.edu.anu.datacommons.exception;

import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataCommonsExceptionMapper
 * 
 * Australian National University Data Commons
 * 
 * Maps the Data Commons Exception to a response
 *
 * JUnit Coverage:
 * None
 * 
 * <pre>
 * Version	Date		Developer				Description
 * 0.1		02/01/2013	Genevieve Turner (GT)	Initial
 * </pre>
 *
 */
@Provider
public class DataCommonsExceptionMapper implements ExceptionMapper<DataCommonsException> {
	static final Logger LOGGER = LoggerFactory.getLogger(DataCommonsExceptionMapper.class);
	@Context HttpHeaders headers;
	
	/**
	 * toResponse
	 * 
	 * Map the exception to a response
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		02/01/2013	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param exception The exception to map to a response
	 * @return The response for the Data Commons Exception
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
	 */
	@Override
	public Response toResponse(DataCommonsException exception) {
		List<MediaType> accepts = headers.getAcceptableMediaTypes();
		LOGGER.info("In DataCommonsExceptionMapper, error: {}", exception.getErrorMessage());
		
		ResponseBuilder responseBuilder = Response.status(exception.getStatus()).entity(exception.getErrorMessage());

		LOGGER.debug("Header Media Type: {}", headers.getMediaType());
		
		if (accepts != null && accepts.size() > 0) {
			LOGGER.debug("There is an acceptable media type set");
			MediaType m = accepts.get(0);
			LOGGER.debug("Media Type: {}", m);
			responseBuilder = responseBuilder.type(m);
		}
		else {
			responseBuilder = responseBuilder.type(headers.getMediaType());
			LOGGER.debug("Using header media type");
		}
		
		return responseBuilder.build();
	}

}
