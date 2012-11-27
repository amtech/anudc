package au.edu.anu.datacommons.doi.logging;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "log_external_web_resource")
public class ExtWebResourceLog
{
	private Long id;
	private String pid;
	private String request;
	private Date requestTimestamp;
	private String response;
	private Date responseTimestamp;

	protected ExtWebResourceLog()
	{
	}

	public ExtWebResourceLog(String pid, String doiSvcRequest)
	{
		this.pid = pid;
		this.request = doiSvcRequest;
		this.requestTimestamp = new Date();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	@Column(name = "pid")
	public String getPid()
	{
		return pid;
	}

	public void setPid(String pid)
	{
		this.pid = pid;
	}

	@Column(name = "http_request", columnDefinition = "text", nullable = false)
	public String getRequest()
	{
		return request;
	}

	public void setRequest(String request)
	{
		this.request = request;
	}

	@Column(name = "request_timestamp", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getRequestTimestamp()
	{
		return requestTimestamp;
	}

	public void setRequestTimestamp(Date requestTimestamp)
	{
		this.requestTimestamp = requestTimestamp;
	}

	@Column(name = "http_response", columnDefinition = "text")
	public String getResponse()
	{
		return response;
	}

	public void setResponse(String response)
	{
		this.response = response;
	}

	@Column(name = "response_timestamp")
	public Date getResponseTimestamp()
	{
		return responseTimestamp;
	}

	public void setResponseTimestamp(Date responseTimestamp)
	{
		this.responseTimestamp = responseTimestamp;
	}

	@Transient
	public void addResponse(String response)
	{
		this.setResponse(response);
		this.setResponseTimestamp(new Date());
	}
}