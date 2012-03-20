package au.edu.anu.datacommons.xml.transform;

import java.io.InputStream;
import java.io.Writer;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * JAXBTransform
 * 
 * Australian National University Data Commons
 * 
 * Helper class that marshals and unmarshals JAXB objects
 * 
 * JUnit coverage:
 * JAXBTransformTest
 * 
 * Version	Date		Developer			Description
 * 0.1		19/03/2012	Genevieve Turner	Initial build
 * 
 */
public class JAXBTransform {
	
	/**
	 * unmarshalStream
	 * 
	 * Unmarshal the given InputStream into the type of the given class
	 * 
	 * Version	Date		Developer			Description
	 * 0.1		19/03/2012	Genevieve Turner	Initial build
	 * 
	 * @param transformStream The stream to  transform
	 * @param classToBeBound The class type to transform
	 * @return The unmarshalled object
	 * @throws JAXBException
	 */
	public Object unmarshalStream(InputStream transformStream, Class classToBeBound)
			throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(classToBeBound);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return unmarshaller.unmarshal(transformStream);
	}
	
	/**
	 * marshalStream
	 * 
	 * Marshal the given object into the given writer.
	 * 
	 * Version	Date		Developer			Description
	 * 0.1		19/03/2012	Genevieve Turner	Initial build
	 * 
	 * @param out The stream to marshal the object in to
	 * @param object The object to marshal
	 * @param classToBeBound The class type of the object
	 * @throws JAXBException
	 */
	public void marshalStream(Writer out, Object object, Class classToBeBound)
			throws JAXBException {
		marshalStream(out, object, classToBeBound, null);
	}
	
	/**
	 * 
	 * marshalStream
	 * 
	 * Marshal the given object into the given writer.
	 * 
	 * Version	Date		Developer			Description
	 * 0.1		19/03/2012	Genevieve Turner	Initial build
	 * 
	 * @param out The stream to marshal the object in to
	 * @param object The object to marshal
	 * @param classToBeBound The class type of the object
	 * @param properties Additional marshal properties to be used when marshalling the object
	 * @throws JAXBException
	 */
	public void marshalStream(Writer out, Object object, Class classToBeBound, Map<String, Object> properties)
			throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(classToBeBound);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.marshal(object, out);
	}
}