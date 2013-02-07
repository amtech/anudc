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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.10.04 at 12:06:16 PM EST 
//


package org.datacite.schema.kernel_2;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for contributorType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="contributorType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ContactPerson"/>
 *     &lt;enumeration value="DataCollector"/>
 *     &lt;enumeration value="DataManager"/>
 *     &lt;enumeration value="Distributor"/>
 *     &lt;enumeration value="Editor"/>
 *     &lt;enumeration value="Funder"/>
 *     &lt;enumeration value="HostingInstitution"/>
 *     &lt;enumeration value="Producer"/>
 *     &lt;enumeration value="ProjectLeader"/>
 *     &lt;enumeration value="ProjectMember"/>
 *     &lt;enumeration value="RegistrationAgency"/>
 *     &lt;enumeration value="RegistrationAuthority"/>
 *     &lt;enumeration value="RelatedPerson"/>
 *     &lt;enumeration value="RightsHolder"/>
 *     &lt;enumeration value="Researcher"/>
 *     &lt;enumeration value="Sponsor"/>
 *     &lt;enumeration value="Supervisor"/>
 *     &lt;enumeration value="WorkPackageLeader"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "contributorType")
@XmlEnum
public enum ContributorType {

    @XmlEnumValue("ContactPerson")
    CONTACT_PERSON("ContactPerson"),
    @XmlEnumValue("DataCollector")
    DATA_COLLECTOR("DataCollector"),
    @XmlEnumValue("DataManager")
    DATA_MANAGER("DataManager"),
    @XmlEnumValue("Distributor")
    DISTRIBUTOR("Distributor"),
    @XmlEnumValue("Editor")
    EDITOR("Editor"),
    @XmlEnumValue("Funder")
    FUNDER("Funder"),
    @XmlEnumValue("HostingInstitution")
    HOSTING_INSTITUTION("HostingInstitution"),
    @XmlEnumValue("Producer")
    PRODUCER("Producer"),
    @XmlEnumValue("ProjectLeader")
    PROJECT_LEADER("ProjectLeader"),
    @XmlEnumValue("ProjectMember")
    PROJECT_MEMBER("ProjectMember"),
    @XmlEnumValue("RegistrationAgency")
    REGISTRATION_AGENCY("RegistrationAgency"),
    @XmlEnumValue("RegistrationAuthority")
    REGISTRATION_AUTHORITY("RegistrationAuthority"),
    @XmlEnumValue("RelatedPerson")
    RELATED_PERSON("RelatedPerson"),
    @XmlEnumValue("RightsHolder")
    RIGHTS_HOLDER("RightsHolder"),
    @XmlEnumValue("Researcher")
    RESEARCHER("Researcher"),
    @XmlEnumValue("Sponsor")
    SPONSOR("Sponsor"),
    @XmlEnumValue("Supervisor")
    SUPERVISOR("Supervisor"),
    @XmlEnumValue("WorkPackageLeader")
    WORK_PACKAGE_LEADER("WorkPackageLeader");
    private final String value;

    ContributorType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ContributorType fromValue(String v) {
        for (ContributorType c: ContributorType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
