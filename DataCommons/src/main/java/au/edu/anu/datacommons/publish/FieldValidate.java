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

package au.edu.anu.datacommons.publish;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.anu.datacommons.data.fedora.FedoraBroker;
import au.edu.anu.datacommons.util.Constants;
import au.edu.anu.datacommons.util.Util;
import au.edu.anu.datacommons.xml.data.Data;
import au.edu.anu.datacommons.xml.data.DataItem;
import au.edu.anu.datacommons.xml.template.Template;
import au.edu.anu.datacommons.xml.template.TemplateItem;
import au.edu.anu.datacommons.xml.transform.JAXBTransform;

import com.yourmediashelf.fedora.client.FedoraClientException;

/**
 * FieldValidate
 * 
 * Australian National University Data Commons
 * 
 * Validates that required fields and their types
 *
 * JUnit Coverage:
 * None
 * 
 * <pre>
 * Version	Date		Developer				Description
 * 0.1		17/07/2012	Genevieve Turner (GT)	Initial
 * 0.2		12/10/2012	Genevieve Turner (GT)	Added label to required fields
 * 0.3		15/10/2012	Genevieve Turner (GT)	Modified to cater for the 'needed' class
 * </pre>
 *
 */
public class FieldValidate implements Validate {
	static final Logger LOGGER = LoggerFactory.getLogger(FieldValidate.class);
	private List<String> errorMessages_;
	
	/**
	 * Constructor
	 * 
	 * Initialises default fields
	 * 
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		17/07/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 *
	 */
	public FieldValidate() {
		errorMessages_ = new ArrayList<String>();
	}
	
	/**
	 * isValid
	 * 
	 * Validates the xml for the given pid
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		17/07/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param pid
	 * @return
	 * @see au.edu.anu.datacommons.publish.Validate#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String pid) {
		errorMessages_ = new ArrayList<String>();
		boolean isValid = true;
		try {
			InputStream xmlSource = FedoraBroker.getDatastreamAsStream(pid, Constants.XML_SOURCE);
			InputStream xmlTemplate = FedoraBroker.getDatastreamAsStream(pid, Constants.XML_TEMPLATE);
			
			JAXBTransform jaxbTransform = new JAXBTransform();
			Data data = (Data)jaxbTransform.unmarshalStream(xmlSource, Data.class);
			Template template = (Template) jaxbTransform.unmarshalStream(xmlTemplate, Template.class);
			isValid = validate(template, data);
		}
		catch (FedoraClientException e) {
			LOGGER.error("Unable to retrieve item.", e);
			isValid = false;
		}
		catch (JAXBException e) {
			LOGGER.info("Unable to transform item.", e);
			isValid = false;
		}
		
		return isValid;
	}
	
	/**
	 * validate
	 *
	 * Validates the template against the data
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		17/07/2012	Genevieve Turner(GT)	Initial
	 * 0.2		12/10/2012	Genevieve Turner (GT)	Added label to required fields
	 * 0.3		15/10/2012	Genevieve Turner (GT)	Modified to cater for the 'needed' class
	 * </pre>
	 * 
	 * @param template
	 * @param data
	 * @return
	 */
	private boolean validate(Template template, Data data) {
		boolean isValid = true;
		for (TemplateItem item : template.getItems()) {
			if (Util.isNotEmpty(item.getClassValue())) {
				String[] validationTypes = item.getClassValue().split(" ");
				String fieldName = item.getName();
				List<DataItem> fieldItems = getFieldItems(fieldName, data);
				for (String validationType : validationTypes) {
					if ("required".equals(validationType) || "needed".equals(validationType)) {
						if(!hasRequired(fieldName, fieldItems, item.getLabel())) {
							isValid = false;
						}
					}
					else if ("email".equals(validationType)) {
						if(!hasValidEmail(fieldName, fieldItems)) {
							isValid = false;
						}
					}
					else if ("date".equals(validationType)) {
						if(!hasValidDate(fieldName, fieldItems)) {
							isValid = false;
						}
					}
				}
			}
		}
		
		return isValid;
	}
	
	/**
	 * getFieldItems
	 *
	 * Gets the fields with the given name
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		17/07/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param fieldName
	 * @param data
	 * @return
	 */
	private List<DataItem> getFieldItems(String fieldName, Data data) {
		List<DataItem> items = new ArrayList<DataItem>();
		
		for (DataItem dataItem : data.getItems()) {
			if (dataItem.getName().equals(fieldName)) {
				items.add(dataItem);
			}
		}
		
		return items;
	}
	
	/**
	 * hasRequired
	 *
	 * Validates required fields
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		17/07/2012	Genevieve Turner(GT)	Initial
	 * 0.2		12/10/2012	Genevieve Turner (GT)	Added label to required fields
	 * </pre>
	 * 
	 * @param fieldName
	 * @param items
	 * @return
	 */
	private boolean hasRequired(String fieldName, List<DataItem> items, String label) {
		if (items.size() > 0) {
			return true;
		}
		errorMessages_.add(label + " is required");
		return false;
	}
	
	/**
	 * hasValidEmail
	 *
	 * Validates email addresses
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		17/07/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param fieldName
	 * @param items
	 * @return
	 */
	private boolean hasValidEmail(String fieldName, List<DataItem> items) {
		boolean isValid = true;
		for (DataItem item : items) {
			try {
				InternetAddress email = new InternetAddress(item.getValue());
				email.validate();
			}
			catch (AddressException e) {
				isValid = false;
				errorMessages_.add(item.getValue() + " is not a valid email address");
			}
		}
		return isValid;
	}
	
	/**
	 * hasValidDate
	 *
	 * Validates dates
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		17/07/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param fieldName
	 * @param items
	 * @return
	 */
	private boolean hasValidDate(String fieldName, List<DataItem> items) {
		boolean isValid = true;
		for (DataItem item : items) {
			try {
				XMLGregorianCalendar date1 = DatatypeFactory.newInstance().newXMLGregorianCalendar(item.getValue());
			}
			catch (DatatypeConfigurationException e) {
				errorMessages_.add(item.getValue() + " is not a valid date");
				isValid = false;
			}
			catch (IllegalArgumentException e) {
				errorMessages_.add(item.getValue() + " is not a valid date");
				isValid = false;
			}
		}
		return isValid;
	}

	/**
	 * getErrorMessages
	 * 
	 * Returns the error messages
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		17/07/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @return
	 * @see au.edu.anu.datacommons.publish.Validate#getErrorMessages()
	 */
	@Override
	public List<String> getErrorMessages() {
		return errorMessages_;
	}
}
