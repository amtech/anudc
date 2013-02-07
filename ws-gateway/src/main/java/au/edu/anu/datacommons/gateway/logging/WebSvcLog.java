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

package au.edu.anu.datacommons.gateway.logging;

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
@Table(name = "log_webservice")
public class WebSvcLog
{
	private Long id;
	private String request;
	private Date requestTimestamp;
	private String response;
	private Date responseTimestamp;
	private String function;
	private String ipAddress;
	
	protected WebSvcLog()
	{
	}
	
	public WebSvcLog(String request, String ipAddress, String function)
	{
		this.request = request;
		this.ipAddress = ipAddress;
		this.function = function;
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

	@Column(name = "requestor_ip")
	public String getIpAddress()
	{
		return ipAddress;
	}

	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	@Column(name = "http_request", nullable = false, columnDefinition="text")
	public String getRequest()
	{
		return request;
	}

	public void setRequest(String request)
	{
		this.request = request;
	}

	@Column(name = "http_response", columnDefinition="text")
	public String getResponse()
	{
		return response;
	}

	public void setResponse(String response)
	{
		this.response = response;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "request_timestamp")
	public Date getRequestTimestamp()
	{
		return requestTimestamp;
	}

	public void setRequestTimestamp(Date requestTimestamp)
	{
		this.requestTimestamp = requestTimestamp;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "response_timestamp")
	public Date getResponseTimestamp()
	{
		return responseTimestamp;
	}

	public void setResponseTimestamp(Date responseTimestamp)
	{
		this.responseTimestamp = responseTimestamp;
	}
	
	@Column(name = "function_name")
	public String getFunction()
	{
		return function;
	}

	public void setFunction(String function)
	{
		this.function = function;
	}

	@Transient
	public void addResponse(String response)
	{
		this.setResponse(response);
		this.setResponseTimestamp(new Date());
	}
}
