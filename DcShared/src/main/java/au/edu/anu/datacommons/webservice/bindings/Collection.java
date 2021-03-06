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

package au.edu.anu.datacommons.webservice.bindings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents a Collection record and contains metadata information of that record. This class can then be
 * marshalled and unmarshalled to and from an XML document as required for Data Commons Web Service. The class marshalls
 * into the following XML element:
 *
 * <pre>
 * {@code
 * <collection tmplt="...">
 *         <accessRights>...</accessRights>
 *         <altName>...</altName>
 *         <anzforSubject>...</anzforSubject>
 *         <anzforSubject>...</anzforSubject>
 *         <anzseoSubject>...</anzseoSubject>
 *         <anzseoSubject>...</anzseoSubject>
 *         <briefDesc>...</briefDesc>
 *         <abbrName>...</abbrName>
 *         <fullCitation>...</fullCitation>
 *         <fullCitationType>...</fullCitationType>
 *         <collaborator>...</collaborator>
 *         <collaborator>...</collaborator>
 *         <postalAddress>...</postalAddress>
 *         <coverageDateText>...</coverageDateText>
 *         <coverageDateText>...</coverageDateText>
 *         <dataExtent>...</dataExtent>
 *         <dataLang>...</dataLang>
 *         <dataLocation>...</dataLocation>
 *         <dataMgmtPlan>...</dataMgmtPlan>
 *         <dataSize>...</dataSize>
 *         <coverageDates>
 *             <dateFrom>...</dateFrom>
 *             <dateTo>...</dateTo>
 *         </coverageDates>
 *         <coverageDates>
 *             <dateFrom>...</dateFrom>
 *             <dateTo>...</dateTo>
 *         </coverageDates>
 *         <disposalDate>...</disposalDate>
 *         <email>...</email>
 *         <email>...</email>
 *         <externalId>...</externalId>
 *         <externalId>...</externalId>
 *         <fax>...</fax>
 *         <fax>...</fax>
 *         <link filename="...">...</link>
 *         <link reference-only="...">...</link>
 *         <fullDesc>...</fullDesc>
 *         <coverageArea>
 *             <covAreaType>...</covAreaType>
 *             <covAreaValue>...</covAreaValue>
 *         </coverageArea>
 *         <coverageArea>
 *             <covAreaType>...</covAreaType>
 *             <covAreaValue>...</covAreaValue>
 *         </coverageArea>
 *         <locSubject>...</locSubject>
 *         <locSubject>...</locSubject>
 *         <licence>...</licence>
 *         <licenceType>...</licenceType>
 *         <metaLang>...</metaLang>
 *         <ownerGroup>...</ownerGroup>
 *         <phone>...</phone>
 *         <phone>...</phone>
 *         <principalInvestigator>...</principalInvestigator>
 *         <principalInvestigator>...</principalInvestigator>
 *         <principalInvestigator>...</principalInvestigator>
 *         <publication>
 *             <pubValue>...</pubValue>
 *             <pubType>...</pubType>
 *             <pubTitle>...</pubTitle>
 *         </publication>
 *         <relatedWebsites>
 *             <relatedWebTitle>...</relatedWebTitle>
 *             <relatedWebURL>...</relatedWebURL>
 *         </relatedWebsites>
 *         <relatedWebsites>
 *             <relatedWebTitle>...</relatedWebTitle>
 *             <relatedWebURL>...</relatedWebURL>
 *         </relatedWebsites>
 *         <anztoaSubject>...</anztoaSubject>
 *         <dataRetention>...</dataRetention>
 *         <rightsStatement>...</rightsStatement>
 *         <significanceStatement>...</significanceStatement>
 *         <subType>...</subType>
 *         <supervisor>...</supervisor>
 *         <supervisor>...</supervisor>
 *         <name>...</name>
 *         <type>...</type>
 *         <websiteAddress>...</websiteAddress>
 *         <websiteAddress>...</websiteAddress>
 *     </collection>
 * }
 * </pre>
 */
@XmlType
public class Collection implements FedoraItem {
	private String pid;
	private String template;
	private final String type = "Collection";

	// General
	private String title;
	private String briefTitle;
	private String altTitle;
	private String subType;
	private String ownerGroup;
	private String websiteAddress;
	private String metadataLanguage;
	private String dataLanguage;

	// Coverage
	private List<DateCoverage> dateCoverage;
	private List<String> coverageDateTextList;
	private List<GeospatialLocation> geospatialLocations;

	// Description
	private String significanceStatement;
	private String briefDesc;
	private String fullDesc;
	private String citationYear;
	private List<Creator> creators;
	private String publisher;
	private List<Publication> publications;
	private List<RelatedWebsites> related;
	private List<String> extIds;

	// People
	private List<String> emails;
	private String contactAddress;
	private List<String> phones;
	private List<String> faxes;
	private List<String> websites;
	private List<String> principalInvestigators;
	private List<String> supervisors;
	private List<String> collaborators;

	// Subject
	private List<String> anzForCodes;
	private List<String> anzSeoCodes;
	private List<String> keywords;
	private String researchType;

	// Rights
	private String accessRights;
	private String rightsStatement;
	private String licenceType;
	private String licence;

	// Management
	private String dataLocation;
	private String retentionPeriod;
	private String disposalDate;
	private String dataExtent;
	private String dataSize;
	private Boolean dataMgmtPlan;

	// Data files
	private List<Link> fileUrlList;

	/**
	 * Gets the title.
	 * 
	 * @return the title
	 */
	@XmlElement(name = "name")
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title.
	 * 
	 * @param title
	 *            the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets the brief title.
	 * 
	 * @return the brief title
	 */
	@XmlElement(name = "abbrName")
	public String getBriefTitle() {
		return briefTitle;
	}

	/**
	 * Sets the brief title.
	 * 
	 * @param briefTitle
	 *            the new brief title
	 */
	public void setBriefTitle(String briefTitle) {
		this.briefTitle = briefTitle;
	}

	/**
	 * Gets the alt title.
	 * 
	 * @return the alt title
	 */
	@XmlElement(name = "altName")
	public String getAltTitle() {
		return altTitle;
	}

	/**
	 * Sets the alt title.
	 * 
	 * @param altTitle
	 *            the new alt title
	 */
	public void setAltTitle(String altTitle) {
		this.altTitle = altTitle;
	}

	/**
	 * Gets the sub type.
	 * 
	 * @return the sub type
	 */
	@XmlElement(name = "subType")
	public String getSubType() {
		return subType;
	}

	/**
	 * Sets the sub type.
	 * 
	 * @param subType
	 *            the new sub type
	 */
	public void setSubType(String subType) {
		this.subType = subType;
	}

	@XmlElement(name = "ownerGroup")
	public String getOwnerGroup() {
		return ownerGroup;
	}

	public void setOwnerGroup(String ownerGroup) {
		this.ownerGroup = ownerGroup;
	}

	public String getWebsiteAddress() {
		return websiteAddress;
	}

	public void setWebsiteAddress(String websiteAddress) {
		this.websiteAddress = websiteAddress;
	}

	/**
	 * Gets the metadata language.
	 * 
	 * @return the metadata language
	 */
	@XmlElement(name = "metaLang")
	public String getMetadataLanguage() {
		return metadataLanguage;
	}

	/**
	 * Sets the metadata language.
	 * 
	 * @param metadataLanguage
	 *            the new metadata language
	 */
	public void setMetadataLanguage(String metadataLanguage) {
		this.metadataLanguage = metadataLanguage;
	}

	/**
	 * Gets the data language.
	 * 
	 * @return the data language
	 */
	@XmlElement(name = "dataLang")
	public String getDataLanguage() {
		return dataLanguage;
	}

	/**
	 * Sets the data language.
	 * 
	 * @param dataLanguage
	 *            the new data language
	 */
	public void setDataLanguage(String dataLanguage) {
		this.dataLanguage = dataLanguage;
	}

	// Coverage

	/**
	 * Gets the date coverage.
	 * 
	 * @return the date coverage
	 */
	@XmlElement(name = "coverageDates")
	public List<DateCoverage> getDateCoverage() {
		return dateCoverage;
	}

	/**
	 * Sets the date coverage.
	 * 
	 * @param dateCoverage
	 *            the new date coverage
	 */
	public void setDateCoverage(List<DateCoverage> dateCoverage) {
		this.dateCoverage = dateCoverage;
	}

	/**
	 * Gets the coverage date text list.
	 * 
	 * @return the coverage date text list
	 */
	@XmlElement(name = "coverageDateText")
	public List<String> getCoverageDateTextList() {
		return coverageDateTextList;
	}

	/**
	 * Sets the coverage date text list.
	 * 
	 * @param coverageDateTextList
	 *            the new coverage date text list
	 */
	public void setCoverageDateTextList(List<String> coverageDateTextList) {
		this.coverageDateTextList = coverageDateTextList;
	}

	/**
	 * Gets the geospatial locations.
	 * 
	 * @return the geospatial locations
	 */
	@XmlElement(name = "coverageArea")
	public List<GeospatialLocation> getGeospatialLocations() {
		return geospatialLocations;
	}

	/**
	 * Sets the geospatial locations.
	 * 
	 * @param geospatialLocations
	 *            the new geospatial locations
	 */
	public void setGeospatialLocations(List<GeospatialLocation> geospatialLocations) {
		this.geospatialLocations = geospatialLocations;
	}

	// Description

	/**
	 * Gets the significance statement.
	 * 
	 * @return the significance statement
	 */
	@XmlElement(name = "significanceStatement")
	public String getSignificanceStatement() {
		return significanceStatement;
	}

	/**
	 * Sets the significance statement.
	 * 
	 * @param significanceStatement
	 *            the new significance statement
	 */
	public void setSignificanceStatement(String significanceStatement) {
		this.significanceStatement = significanceStatement;
	}

	/**
	 * Gets the brief desc.
	 * 
	 * @return the brief desc
	 */
	@XmlElement(name = "briefDesc")
	public String getBriefDesc() {
		return briefDesc;
	}

	/**
	 * Sets the brief desc.
	 * 
	 * @param briefDesc
	 *            the new brief desc
	 */
	public void setBriefDesc(String briefDesc) {
		this.briefDesc = briefDesc;
	}

	/**
	 * Gets the full desc.
	 * 
	 * @return the full desc
	 */
	@XmlElement(name = "fullDesc")
	public String getFullDesc() {
		return fullDesc;
	}

	/**
	 * Sets the full desc.
	 * 
	 * @param fullDesc
	 *            the new full desc
	 */
	public void setFullDesc(String fullDesc) {
		this.fullDesc = fullDesc;
	}

	/**
	 * Get year of data publication.
	 * 
	 * @return
	 */
	public String getCitationYear() {
		return citationYear;
	}

	/**
	 * Set year of publication.
	 * 
	 * @param citationYear
	 */
	public void setCitationYear(String citationYear) {
		this.citationYear = citationYear;
	}

	/**
	 * Get creators for citation.
	 * 
	 * @return
	 */
	public List<Creator> getCreators() {
		return creators;
	}

	/**
	 * Set creators for citation.
	 * 
	 * @param creators
	 */
	public void setCreators(List<Creator> creators) {
		this.creators = creators;
	}

	/**
	 * Get publisher
	 * 
	 * @return
	 */
	public String getPublisher() {
		return publisher;
	}

	/**
	 * Set publisher
	 * 
	 * @param publisher
	 */
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	/**
	 * Gets the publications.
	 * 
	 * @return the publications
	 */
	@XmlElement(name = "publication")
	public List<Publication> getPublications() {
		return publications;
	}

	/**
	 * Sets the publications.
	 * 
	 * @param publications
	 *            the new publications
	 */
	public void setPublications(List<Publication> publications) {
		this.publications = publications;
	}

	/**
	 * Gets the related.
	 * 
	 * @return the related
	 */
	@XmlElement(name = "relatedWebsites")
	public List<RelatedWebsites> getRelated() {
		return related;
	}

	/**
	 * Sets the related.
	 * 
	 * @param related
	 *            the new related
	 */
	public void setRelated(List<RelatedWebsites> related) {
		this.related = related;
	}

	/**
	 * Gets the ext ids.
	 * 
	 * @return the ext ids
	 */
	@XmlElement(name = "externalId")
	public List<String> getExtIds() {
		return extIds;
	}

	/**
	 * Sets the ext ids.
	 * 
	 * @param extIds
	 *            the new ext ids
	 */
	public void setExtIds(List<String> extIds) {
		this.extIds = extIds;
	}

	// People

	/**
	 * Gets the emails.
	 * 
	 * @return the emails
	 */
	@XmlElement(name = "email")
	public List<String> getEmails() {
		return emails;
	}

	/**
	 * Sets the emails.
	 * 
	 * @param emails
	 *            the new emails
	 */
	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

	/**
	 * Gets the contact address.
	 * 
	 * @return the contact address
	 */
	@XmlElement(name = "postalAddress")
	public String getContactAddress() {
		return contactAddress;
	}

	/**
	 * Sets the contact address.
	 * 
	 * @param contactAddress
	 *            the new contact address
	 */
	public void setContactAddress(String contactAddress) {
		this.contactAddress = contactAddress;
	}

	/**
	 * Gets the phones.
	 * 
	 * @return the phones
	 */
	@XmlElement(name = "phone")
	public List<String> getPhones() {
		return phones;
	}

	/**
	 * Sets the phones.
	 * 
	 * @param phones
	 *            the new phones
	 */
	public void setPhones(List<String> phones) {
		this.phones = phones;
	}

	/**
	 * Gets the faxes.
	 * 
	 * @return the faxes
	 */
	@XmlElement(name = "fax")
	public List<String> getFaxes() {
		return faxes;
	}

	/**
	 * Sets the faxes.
	 * 
	 * @param faxes
	 *            the new faxes
	 */
	public void setFaxes(List<String> faxes) {
		this.faxes = faxes;
	}

	/**
	 * Gets the websites.
	 * 
	 * @return the websites
	 */
	@XmlElement(name = "websiteAddress")
	public List<String> getWebsites() {
		return websites;
	}

	/**
	 * Sets the websites.
	 * 
	 * @param websites
	 *            the new websites
	 */
	public void setWebsites(List<String> websites) {
		this.websites = websites;
	}

	/**
	 * Gets the principal investigators.
	 * 
	 * @return the principal investigators
	 */
	@XmlElement(name = "principalInvestigator")
	public List<String> getPrincipalInvestigators() {
		return principalInvestigators;
	}

	/**
	 * Sets the principal investigators.
	 * 
	 * @param principalInvestigators
	 *            the new principal investigators
	 */
	public void setPrincipalInvestigators(List<String> principalInvestigators) {
		this.principalInvestigators = principalInvestigators;
	}

	/**
	 * Gets the supervisors.
	 * 
	 * @return the supervisors
	 */
	@XmlElement(name = "supervisor")
	public List<String> getSupervisors() {
		return supervisors;
	}

	/**
	 * Sets the supervisors.
	 * 
	 * @param supervisors
	 *            the new supervisors
	 */
	public void setSupervisors(List<String> supervisors) {
		this.supervisors = supervisors;
	}

	/**
	 * Gets the collaborators.
	 * 
	 * @return the collaborators
	 */
	@XmlElement(name = "collaborator")
	public List<String> getCollaborators() {
		return collaborators;
	}

	/**
	 * Sets the collaborators.
	 * 
	 * @param collaborators
	 *            the new collaborators
	 */
	public void setCollaborators(List<String> collaborators) {
		this.collaborators = collaborators;
	}

	// Subject

	/**
	 * Gets the anz for codes.
	 * 
	 * @return the anz for codes
	 */
	@XmlElement(name = "anzforSubject")
	public List<String> getAnzForCodes() {
		return anzForCodes;
	}

	/**
	 * Sets the anz for codes.
	 * 
	 * @param anzForCodes
	 *            the new anz for codes
	 */
	public void setAnzForCodes(List<String> anzForCodes) {
		this.anzForCodes = anzForCodes;
	}

	/**
	 * Gets the anz seo codes.
	 * 
	 * @return the anz seo codes
	 */
	@XmlElement(name = "anzseoSubject")
	public List<String> getAnzSeoCodes() {
		return anzSeoCodes;
	}

	/**
	 * Sets the anz seo codes.
	 * 
	 * @param anzSeoCodes
	 *            the new anz seo codes
	 */
	public void setAnzSeoCodes(List<String> anzSeoCodes) {
		this.anzSeoCodes = anzSeoCodes;
	}

	/**
	 * Gets the keywords.
	 * 
	 * @return the keywords
	 */
	@XmlElement(name = "locSubject")
	public List<String> getKeywords() {
		return keywords;
	}

	/**
	 * Sets the keywords.
	 * 
	 * @param keywords
	 *            the new keywords
	 */
	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	/**
	 * Gets the research type.
	 * 
	 * @return the research type
	 */
	@XmlElement(name = "anztoaSubject")
	public String getResearchType() {
		return researchType;
	}

	/**
	 * Sets the research type.
	 * 
	 * @param researchType
	 *            the new research type
	 */
	public void setResearchType(String researchType) {
		this.researchType = researchType;
	}

	// Rights

	/**
	 * Gets the access rights.
	 * 
	 * @return the access rights
	 */
	@XmlElement(name = "accessRights")
	public String getAccessRights() {
		return accessRights;
	}

	/**
	 * Sets the access rights.
	 * 
	 * @param accessRights
	 *            the new access rights
	 */
	public void setAccessRights(String accessRights) {
		this.accessRights = accessRights;
	}

	/**
	 * Gets the rights statement.
	 * 
	 * @return the rights statement
	 */
	@XmlElement(name = "rightsStatement")
	public String getRightsStatement() {
		return rightsStatement;
	}

	/**
	 * Sets the rights statement.
	 * 
	 * @param rightsStatement
	 *            the new rights statement
	 */
	public void setRightsStatement(String rightsStatement) {
		this.rightsStatement = rightsStatement;
	}

	/**
	 * Gets the licence type.
	 * 
	 * @return the licence type
	 */
	@XmlElement(name = "licenceType")
	public String getLicenceType() {
		return licenceType;
	}

	/**
	 * Sets the licence type.
	 * 
	 * @param licenceType
	 *            the new licence type
	 */
	public void setLicenceType(String licenceType) {
		this.licenceType = licenceType;
	}

	/**
	 * Gets the licence.
	 * 
	 * @return the licence
	 */
	@XmlElement(name = "licence")
	public String getLicence() {
		return licence;
	}

	/**
	 * Sets the licence.
	 * 
	 * @param licence
	 *            the new licence
	 */
	public void setLicence(String licence) {
		this.licence = licence;
	}

	// Management

	/**
	 * Gets the data location.
	 * 
	 * @return the data location
	 */
	@XmlElement(name = "dataLocation")
	public String getDataLocation() {
		return dataLocation;
	}

	/**
	 * Sets the data location.
	 * 
	 * @param dataLocation
	 *            the new data location
	 */
	public void setDataLocation(String dataLocation) {
		this.dataLocation = dataLocation;
	}

	/**
	 * Gets the retention period.
	 * 
	 * @return the retention period
	 */
	@XmlElement(name = "dataRetention")
	public String getRetentionPeriod() {
		return retentionPeriod;
	}

	/**
	 * Sets the retention period.
	 * 
	 * @param retentionPeriod
	 *            the new retention period
	 */
	public void setRetentionPeriod(String retentionPeriod) {
		this.retentionPeriod = retentionPeriod;
	}

	/**
	 * Gets the disposal date.
	 * 
	 * @return the disposal date
	 */
	@XmlElement(name = "disposalDate")
	public String getDisposalDate() {
		return disposalDate;
	}

	/**
	 * Sets the disposal date.
	 * 
	 * @param disposalDate
	 *            the new disposal date
	 */
	public void setDisposalDate(String disposalDate) {
		this.disposalDate = disposalDate;
	}

	/**
	 * Gets the data extent.
	 * 
	 * @return the data extent
	 */
	@XmlElement(name = "dataExtent")
	public String getDataExtent() {
		return dataExtent;
	}

	/**
	 * Sets the data extent.
	 * 
	 * @param dataExtent
	 *            the new data extent
	 */
	public void setDataExtent(String dataExtent) {
		this.dataExtent = dataExtent;
	}

	/**
	 * Gets the data size.
	 * 
	 * @return the data size
	 */
	@XmlElement(name = "dataSize")
	public String getDataSize() {
		return dataSize;
	}

	/**
	 * Sets the data size.
	 * 
	 * @param dataSize
	 *            the new data size
	 */
	public void setDataSize(String dataSize) {
		this.dataSize = dataSize;
	}

	/**
	 * Gets the data mgmt plan.
	 * 
	 * @return the data mgmt plan
	 */
	@XmlElement(name = "dataMgmtPlan")
	public Boolean getDataMgmtPlan() {
		return dataMgmtPlan;
	}

	/**
	 * Sets the data mgmt plan.
	 * 
	 * @param dataMgmtPlan
	 *            the new data mgmt plan
	 */
	public void setDataMgmtPlan(Boolean dataMgmtPlan) {
		this.dataMgmtPlan = dataMgmtPlan;
	}

	/**
	 * Gets the file url list.
	 * 
	 * @return the file url list
	 */
	@XmlElement(name = "link")
	public List<Link> getFileUrlList() {
		return fileUrlList;
	}

	/**
	 * Sets the file url list.
	 * 
	 * @param fileUrlList
	 *            the new file url list
	 */
	public void setFileUrlList(List<Link> fileUrlList) {
		this.fileUrlList = fileUrlList;
	}

	@Override
	@XmlAttribute(name = "tmplt")
	public String getTemplate() {
		return template;
	}

	@Override
	public void setTemplate(String template) {
		this.template = template;
	}

	@Override
	@XmlElement(name = "pid")
	public String getPid() {
		return pid;
	}

	@Override
	public void setPid(String pid) {
		this.pid = pid;
	}

	@XmlElement(name = "type")
	public String getType() {
		return type;
	}

	@Override
	public Map<String, List<String>> generateDataMap() {
		Map<String, List<String>> data = new HashMap<String, List<String>>();

		data.put("type", Arrays.asList(this.getType()));
		if (this.getTitle() != null)
			data.put("name", new ArrayList<String>(Arrays.asList(this.getTitle())));
		if (this.getBriefTitle() != null)
			data.put("abbrName", new ArrayList<String>(Arrays.asList(this.getBriefTitle())));
		if (this.getAltTitle() != null)
			data.put("altName", new ArrayList<String>(Arrays.asList(this.getAltTitle())));
		if (this.getSubType() != null)
			data.put("subType", new ArrayList<String>(Arrays.asList(this.getSubType())));
		if (this.getOwnerGroup() != null)
			data.put("ownerGroup", new ArrayList<String>(Arrays.asList(this.getOwnerGroup())));
		if (this.getWebsiteAddress() != null)
			data.put("websiteAddress", new ArrayList<String>(Arrays.asList(this.getWebsiteAddress())));
		if (this.getMetadataLanguage() != null) 
			data.put("metaLang", new ArrayList<String>(Arrays.asList(this.getMetadataLanguage())));
		if (this.getDataLanguage() != null)
			data.put("dataLang", new ArrayList<String>(Arrays.asList(this.getDataLanguage())));

		if (this.getDateCoverage() != null && this.getDateCoverage().size() > 0) {
			ArrayList<String> datesFrom = new ArrayList<String>();
			ArrayList<String> datesTo = new ArrayList<String>();
			for (DateCoverage iDc : this.getDateCoverage()) {
				datesFrom.add(iDc.getDateFrom());
				datesTo.add(iDc.getDateTo());
			}
			data.put("dateFrom", datesFrom);
			data.put("dateTo", datesTo);
		}
		if (this.getCoverageDateTextList() != null && this.getCoverageDateTextList().size() > 0)
			data.put("coverageDateText", this.getCoverageDateTextList());
		if (this.getGeospatialLocations() != null && this.getGeospatialLocations().size() > 0) {
			ArrayList<String> covTypes = new ArrayList<String>();
			ArrayList<String> covVals = new ArrayList<String>();
			for (GeospatialLocation iGl : this.getGeospatialLocations()) {
				covTypes.add(iGl.getCovAreaType());
				covVals.add(iGl.getCovAreaValue());
			}
			data.put("covAreaType", covTypes);
			data.put("covAreaValue", covVals);
		}
		if (this.getSignificanceStatement() != null)
			data.put("significanceStatement", new ArrayList<String>(Arrays.asList(this.getSignificanceStatement())));
		if (this.getBriefDesc() != null)
			data.put("briefDesc", new ArrayList<String>(Arrays.asList(this.getBriefDesc())));
		if (this.getFullDesc() != null)
			data.put("fullDesc", new ArrayList<String>(Arrays.asList(this.getFullDesc())));
		if (this.getCitationYear() != null)
			data.put("citationYear", new ArrayList<String>(Arrays.asList(this.getCitationYear())));
		if (this.getCreators() != null) {
			ArrayList<String> citCreatorGivenList = new ArrayList<>();
			ArrayList<String> citCreatorSurnameList = new ArrayList<>();
			for (Creator iCreator : this.getCreators()) {
				citCreatorGivenList.add(iCreator.getCitCreatorGiven());
				citCreatorSurnameList.add(iCreator.getCitCreatorSurname());
			}
			data.put("citCreatorGiven", citCreatorGivenList);
			data.put("citCreatorSurname", citCreatorSurnameList);
		}
		if (this.publisher != null)
			data.put("citationPublisher", new ArrayList<String>(Arrays.asList(this.getPublisher())));

		if (this.getPublications() != null) {
			ArrayList<String> pubTypeList = new ArrayList<String>();
			ArrayList<String> pubValueList = new ArrayList<String>();
			ArrayList<String> pubTitleList = new ArrayList<String>();
			for (Publication iPub : this.getPublications()) {
				pubTypeList.add(iPub.getIdType());
				pubValueList.add(iPub.getId());
				pubTitleList.add(iPub.getTitle());
			}
			data.put("pubType", pubTypeList);
			data.put("pubValue", pubValueList);
			data.put("pubTitle", pubTitleList);
		}

		if (this.getRelated() != null) {
			ArrayList<String> relatedWebUrlList = new ArrayList<String>();
			ArrayList<String> relatedWebTitleList = new ArrayList<String>();
			for (RelatedWebsites iRelated : this.getRelated()) {
				relatedWebUrlList.add(iRelated.getRelatedWebUrl());
				relatedWebTitleList.add(iRelated.getRelatedWebTitle());
			}
			data.put("relatedWebURL", relatedWebUrlList);
			data.put("relatedWebTitle", relatedWebTitleList);
		}

		if (this.getExtIds() != null && this.getExtIds().size() > 0)
			data.put("externalId", this.getExtIds());

		if (this.getEmails() != null && this.getEmails().size() > 0)
			data.put("email", this.getEmails());
		if (this.getContactAddress() != null)
			data.put("postalAddress", new ArrayList<String>(Arrays.asList(this.getContactAddress())));
		if (this.getPhones() != null)
			data.put("phone", this.getPhones());
		if (this.getFaxes() != null)
			data.put("fax", this.getFaxes());
		if (this.getWebsites() != null)
			data.put("websiteAddress", this.getWebsites());
		if (this.getPrincipalInvestigators() != null)
			data.put("principalInvestigator", this.getPrincipalInvestigators());
		if (this.getSupervisors() != null)
			data.put("supervisor", this.getSupervisors());
		if (this.getCollaborators() != null)
			data.put("collaborator", this.getCollaborators());

		if (this.getAnzForCodes() != null)
			data.put("anzforSubject", this.getAnzForCodes());
		if (this.getAnzSeoCodes() != null)
			data.put("anzseoSubject", this.getAnzSeoCodes());
		if (this.getKeywords() != null)
			data.put("locSubject", this.getKeywords());
		if (this.getResearchType() != null)
			data.put("anztoaSubject", new ArrayList<String>(Arrays.asList(this.getResearchType())));

		if (this.getAccessRights() != null)
			data.put("accessRights", new ArrayList<String>(Arrays.asList(this.getAccessRights())));
		if (this.getRightsStatement() != null)
			data.put("rightsStatement", new ArrayList<String>(Arrays.asList(this.getRightsStatement())));
		if (this.getLicenceType() != null)
			data.put("licenceType", new ArrayList<String>(Arrays.asList(this.getLicenceType())));
		if (this.getLicence() != null)
			data.put("licence", new ArrayList<String>(Arrays.asList(this.getLicence())));

		if (this.getDataLocation() != null)
			data.put("dataLocation", new ArrayList<String>(Arrays.asList(this.getDataLocation())));
		if (this.getRetentionPeriod() != null)
			data.put("dataRetention", new ArrayList<String>(Arrays.asList(this.getRetentionPeriod())));
		if (this.getDisposalDate() != null)
			data.put("disposalDate", new ArrayList<String>(Arrays.asList(this.getDisposalDate())));
		if (this.getDataExtent() != null)
			data.put("dataExtent", new ArrayList<String>(Arrays.asList(this.getDataExtent())));
		if (this.getDataSize() != null)
			data.put("dataSize", new ArrayList<String>(Arrays.asList(this.getDataSize())));
		if (this.getDataMgmtPlan() != null)
			data.put("dataMgmtPlan", new ArrayList<String>(Arrays.asList(this.getDataMgmtPlan().toString())));

		return data;
	}

}
