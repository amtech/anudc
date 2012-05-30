package au.edu.anu.datacommons.properties;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.xml.bind.PropertyException;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.anu.datacommons.util.Util;

/**
 * GlobalProps
 * 
 * Australian National University Data Commons
 * 
 * Contains static methods that reads (no write functions) values for keys in the global.properties file.
 * 
 * Usage example: To return the value for the key containing Fedora's URI.
 * 
 * <pre>
 * GlobalProps.getProperty(GlobalProps.PROP_FEDORA_SERVER);
 * 
 * <pre>
 * 
 * <pre>
 * Version	Date		Developer				Description
 * 0.1		8/03/2012	Rahul Khanna (RK)		Initial.
 * 0.2		14/03/2012	Rahul Khanna (RK)		Added Search properties.
 * 0.3		20/03/2012	Rahul Khanna (RK)		Added File Upload properties.
 * 0.4		4/05/2012	Rahul Khanna (RK)		Added Random Password chars property.
 * 0.5		12/05/2012	Genevieve Turner (GT)	Changed case of properties, related uri and save namespace
 * 
 * <pre>
 * 
 */
public final class GlobalProps
{
	private static final Properties globalProperties;
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalProps.class);

	// Name of the properties file from which properties will be read.
	private static final String GLOBAL_PROPERTIES_FILENAME = "global.properties";

	// List of valid Keys in global.properties file that can be used as parameters in the methods of this class.
	public static final String PROP_FEDORA_URI = "fedora.baseURI";
	public static final String PROP_FEDORA_USERNAME = "fedora.username";
	public static final String PROP_FEDORA_PASSWORD = "fedora.password";
	public static final String PROP_FEDORA_RISEARCHURL = "fedora.riSearchURI";
	public static final String PROP_FEDORA_RELATEDURI = "fedora.relatedURI";
	public static final String PROP_FEDORA_SAVENAMESPACE = "fedora.saveNamespace";
	public static final String PROP_FEDORA_OAIPROVIDER_URL = "fedora.oaiprovider.url";
	public static final String PROP_FEDORA_NAMEFIELDS = "fedora.nameFields";
	public static final String PROP_LDAP_URI = "ldap.uri";
	public static final String PROP_LDAP_BASEDN = "ldap.baseDn";
	public static final String PROP_LDAP_ATTRLIST = "ldap.person.attrList";
	public static final String PROP_LDAPATTR_UNIID = "ldap.attr.uniId";
	public static final String PROP_LDAPATTR_DISPLAYNAME = "ldap.attr.displayName";
	public static final String PROP_LDAPATTR_GIVENNAME = "ldap.attr.givenName";
	public static final String PROP_LDAPATTR_FAMILYNAME = "ldap.attr.familyName";
	public static final String PROP_LDAPATTR_EMAIL = "ldap.attr.email";
	public static final String PROP_LDAPATTR_PHONE = "ldap.attr.phone";
	public static final String PROP_SEARCH_SEARCHFIELDS = "search.dcSearchFields";
	public static final String PROP_SEARCH_RETURNFIELDS = "search.dcReturnFields";
	public static final String PROP_SEARCH_URIREPLACE = "search.uriReplace";
	public static final String PROP_UPLOAD_DIR = "upload.uploadDir";
	public static final String PROP_UPLOAD_TEMPDIR = "upload.tempDir";
	public static final String PROP_UPLOAD_MAXSIZEINMEM = "upload.maxSizeInMemInBytes";
	public static final String PROP_UPLOAD_HTTPBASEURI = "upload.uploadHttpBaseURI";
	public static final String PROP_UPLOAD_BAGSDIR = "upload.bagsDir";
	public static final String PROP_PASSWORDGENERATOR_CHARS = "passwordGenerator.chars";
	public static final String PROP_DROPBOX_PASSWORDLENGTH = "dropbox.passwordLength";
	public static final String PROP_EMAIL_DEBUG_SEND = "email.debug.sendmail";

	static
	{
		globalProperties = new Properties();

		try
		{
			globalProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(GLOBAL_PROPERTIES_FILENAME));
		}
		catch (IOException e)
		{
			LOGGER.error("Unable to read configuration file", e);
		}
	}

	/**
	 * @see Properties.getProperty()
	 * @param key
	 * @return value of the key, null if the key doesn't exist.
	 */
	public static String getProperty(String key)
	{
		return globalProperties.getProperty(key);
	}

	/**
	 * @see Properties.getProperty()
	 * @param key
	 * @param defaultValue
	 * @return value of the key or defaultValue if key doesn't exist.
	 */
	public static String getProperty(String key, String defaultValue)
	{
		return globalProperties.getProperty(key, defaultValue);
	}

	public static String getUploadDirAsString() throws PropertyException
	{
		String uploadDirString = getProperty(PROP_UPLOAD_DIR);
		if (!Util.isNotEmpty(uploadDirString))
			throw new PropertyException("Property " + PROP_UPLOAD_DIR + " not specified in Global Properties.");
		return uploadDirString;
	}

	public static File getUploadDirAsFile() throws PropertyException
	{
		return new File(getUploadDirAsString());
	}

	public static String getBagsDirAsString() throws PropertyException
	{
		String bagsDirString = getProperty(PROP_UPLOAD_BAGSDIR);
		if (!Util.isNotEmpty(bagsDirString))
			throw new PropertyException("Property " + PROP_UPLOAD_BAGSDIR + " not specified in Global Properties.");
		return bagsDirString;
	}

	public static File getBagsDirAsFile() throws PropertyException
	{
		return new File(getBagsDirAsString());
	}

	public static String getTempDirAsString() throws PropertyException
	{
		String tempDirString = getProperty(PROP_UPLOAD_TEMPDIR);
		if (!Util.isNotEmpty(tempDirString))
			throw new PropertyException("Property " + PROP_UPLOAD_TEMPDIR + " not specified in Global Properties.");
		return tempDirString;
	}

	public static File getTempDirAsFile() throws PropertyException
	{
		return new File(getTempDirAsString());
	}

	public static boolean getEmailDebugSend()
	{
		String emailDebugSendString = getProperty(PROP_EMAIL_DEBUG_SEND);
		boolean emailDebugSend = false;

		if (emailDebugSendString != null)
			emailDebugSend = Boolean.parseBoolean(getProperty(PROP_EMAIL_DEBUG_SEND));
		else
			LOGGER.warn("Property " + PROP_EMAIL_DEBUG_SEND + " not specified in Global Properties.");

		return emailDebugSend;
	}

	public static int getMaxSizeInMem()
	{
		try
		{
			return Integer.parseInt(getProperty(PROP_UPLOAD_MAXSIZEINMEM));
		}
		catch (NumberFormatException e)
		{
			LOGGER.warn("Property " + PROP_UPLOAD_MAXSIZEINMEM + " not specified in Global Properties.");
			return DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD;
		}
	}
}
