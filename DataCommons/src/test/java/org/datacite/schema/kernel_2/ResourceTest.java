package org.datacite.schema.kernel_2;

import static org.junit.Assert.*;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.datacite.schema.kernel_2.Resource.Creators;
import org.datacite.schema.kernel_2.Resource.Creators.Creator;
import org.datacite.schema.kernel_2.Resource.Titles;
import org.datacite.schema.kernel_2.Resource.Titles.Title;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import au.edu.anu.datacommons.doi.DoiResponse;

public class ResourceTest
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceTest.class);

	private static JAXBContext context;
	private static Marshaller marshaller;
	private static Unmarshaller unmarshaller;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		context = JAXBContext.newInstance(Resource.class);
		marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://datacite.org/schema/kernel-2.2 http://schema.datacite.org/meta/kernel-2.2/metadata.xsd");
//		marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
//			@Override
//			public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix)
//			{
//				return "";
//			}
//			
//			@Override
//			public String[] getPreDeclaredNamespaceUris()
//			{
//				return new String[] {"http://datacite.org/schema/kernel-2.2"};
//			}
//		});
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void test()
	{
		Resource metadata = new Resource();
		
		Titles titles = new Titles();
		Title title1 = new Title();
		title1.setValue("Some title without a type");

		titles.getTitle().add(title1);
		
		metadata.setTitles(titles);
		
		Creators creators = new Creators();
		metadata.setCreators(creators);
		Creator creator = new Creator();
		creator.setCreatorName("Professor John Smith");
		
		metadata.getCreators().getCreator().add(creator);
		
		metadata.setPublisher("Some random publisher");
		metadata.setPublicationYear("2010");
		
		try
		{
			LOGGER.trace(getMetadataAsStr(metadata));
		}
		catch (JAXBException e)
		{
			failOnException(e);
		}
	}
	
	private String getMetadataAsStr(Resource metadata) throws JAXBException
	{
		StringWriter xmlSw = new StringWriter();
		marshaller.marshal(metadata, xmlSw);
		return xmlSw.toString();
	}
	
	private void failOnException(Throwable e)
	{
		LOGGER.error(e.getMessage(), e);
		fail(e.getMessage());
	}
}