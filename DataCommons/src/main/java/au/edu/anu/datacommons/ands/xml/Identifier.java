package au.edu.anu.datacommons.ands.xml;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * Identifier
 * 
 * Australian National University Data Commons
 * 
 * Class for the identifier element in the ANDS RIF-CS schema
 *
 * JUnit Coverage:
 * None
 * 
 * <pre>
 * Version	Date		Developer				Description
 * 0.1		15/10/2012	Genevieve Turner (GT)	Initial
 * </pre>
 *
 */
public class Identifier {
	private String type;
	private String value;

	/**
	 * getType
	 *
	 * Get the identifier type
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		03/10/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @return the type
	 */
	@NotNull(message="An identifier type is missing")
	@XmlAttribute
	public String getType() {
		return type;
	}

	/**
	 * setType
	 *
	 * Set the identifier type
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		03/10/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * getValue
	 *
	 * Get the identifier value
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		05/10/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @return the value
	 */
	@NotNull(message="An identifier value is missing")
	@XmlValue
	public String getValue() {
		return value;
	}

	/**
	 * setValue
	 *
	 * Set the identifier value
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		05/10/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}