//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.24 at 09:50:34 AM AEDT 
//


package net.opengis.gml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FileType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FileType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}rangeParameters"/>
 *         &lt;choice>
 *           &lt;element name="fileName" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *           &lt;element name="fileReference" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;/choice>
 *         &lt;element name="fileStructure" type="{http://www.opengis.net/gml}FileValueModelType"/>
 *         &lt;element name="mimeType" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="compression" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FileType", propOrder = {
    "rangeParameters",
    "fileName",
    "fileReference",
    "fileStructure",
    "mimeType",
    "compression"
})
public class FileType {

    @XmlElement(required = true)
    protected RangeParametersType rangeParameters;
    @XmlSchemaType(name = "anyURI")
    protected String fileName;
    @XmlSchemaType(name = "anyURI")
    protected String fileReference;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected FileValueModelType fileStructure;
    @XmlSchemaType(name = "anyURI")
    protected String mimeType;
    @XmlSchemaType(name = "anyURI")
    protected String compression;

    /**
     * Gets the value of the rangeParameters property.
     * 
     * @return
     *     possible object is
     *     {@link RangeParametersType }
     *     
     */
    public RangeParametersType getRangeParameters() {
        return rangeParameters;
    }

    /**
     * Sets the value of the rangeParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link RangeParametersType }
     *     
     */
    public void setRangeParameters(RangeParametersType value) {
        this.rangeParameters = value;
    }

    /**
     * Gets the value of the fileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the value of the fileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileName(String value) {
        this.fileName = value;
    }

    /**
     * Gets the value of the fileReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileReference() {
        return fileReference;
    }

    /**
     * Sets the value of the fileReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileReference(String value) {
        this.fileReference = value;
    }

    /**
     * Gets the value of the fileStructure property.
     * 
     * @return
     *     possible object is
     *     {@link FileValueModelType }
     *     
     */
    public FileValueModelType getFileStructure() {
        return fileStructure;
    }

    /**
     * Sets the value of the fileStructure property.
     * 
     * @param value
     *     allowed object is
     *     {@link FileValueModelType }
     *     
     */
    public void setFileStructure(FileValueModelType value) {
        this.fileStructure = value;
    }

    /**
     * Gets the value of the mimeType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the value of the mimeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMimeType(String value) {
        this.mimeType = value;
    }

    /**
     * Gets the value of the compression property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompression() {
        return compression;
    }

    /**
     * Sets the value of the compression property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompression(String value) {
        this.compression = value;
    }

}
