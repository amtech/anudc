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

package gov.loc.repository.bagit.v0_96;

import static org.junit.Assert.*;

import java.text.ParseException;

import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.impl.AbstractBagInfoTxtImplTest;

import org.junit.Test;


public class BagInfoTxtImplTest extends AbstractBagInfoTxtImplTest {
	
	@Override
	public Version getVersion() {
		return Version.V0_96;
	}
	
	@Override
	public String getTestBagInfoTxtBagInfoTxtString() {
		return "Source-Organization: Spengler University\n" +
		"Organization-Address: 1400 Elm St., Cupertino, California, 95014\n" +
		"Contact-Name: Edna Janssen\n" +
		"Contact-Phone: +1 408-555-1212\n" +
		"Contact-Email: ej@spengler.edu\n" +
		"External-Description: Uncompressed greyscale TIFF images from the\n" +
		"     Yoshimuri papers collection.\n" +
		"Bagging-Date: 2008-01-15\n" +  //Name changed
		"External-Identifier: spengler_yoshimuri_001\n" +
		"Bag-Size: 260 GB\n" +  //Name changed
		"Payload-Oxum: 279164409832.1198\n" +  //Added
		"Bag-Group-Identifier: spengler_yoshimuri\n" +
		"Bag-Count: 1 of 15\n" +
		"Internal-Sender-Identifier: /storage/images/yoshimuri\n" +
		"Internal-Sender-Description: Uncompressed greyscale TIFFs created from\n" +
		"     microfilm.\n";		
	}
	
	@Override
	public void addlTestBagInfoTxt(BagInfoTxt bagInfo) {
		assertEquals("279164409832.1198", bagInfo.getPayloadOxum());
	}
		
	@Test
	public void testPayloadOxum() throws Exception {
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt();
		bagInfo.setPayloadOxum("279164409832.1198");
		assertEquals("279164409832.1198", bagInfo.getPayloadOxum());
		assertEquals(Long.valueOf(279164409832L), bagInfo.getOctetCount());
		assertEquals(Long.valueOf(1198L), bagInfo.getStreamCount());
		
		bagInfo.setPayloadOxum(279164409833L, 1199L);
		assertEquals("279164409833.1199", bagInfo.getPayloadOxum());
	}

	@Test(expected=ParseException.class)
	public void testBadPayloadOxum() throws Exception {
		BagInfoTxt bagInfo = this.factory.createBagInfoTxt();
		bagInfo.setPayloadOxum("279164409832");
		bagInfo.getOctetCount();
	}
}
