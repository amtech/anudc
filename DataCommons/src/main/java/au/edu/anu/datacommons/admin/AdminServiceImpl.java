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
package au.edu.anu.datacommons.admin;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import au.edu.anu.datacommons.data.db.dao.GenericDAO;
import au.edu.anu.datacommons.data.db.dao.GenericDAOImpl;
import au.edu.anu.datacommons.data.db.model.Domains;
import au.edu.anu.datacommons.data.db.model.Groups;
import au.edu.anu.datacommons.exception.DataCommonsException;
import au.edu.anu.datacommons.security.acl.PermissionService;

/**
 * AdminServiceImpl
 *
 * Australian National University Data Commons
 * 
 * Implementation class for the Administrative service.
 *
 * JUnit coverage:
 * None
 * 
 * @author Genevieve Turner
 *
 */
@Service("adminServiceImpl")
public class AdminServiceImpl implements AdminService {
	
	@Resource(name="permissionService")
	PermissionService permissionService;

	@Override
	public List<Domains> getDomains() {
		GenericDAO<Domains, Long> domainsDAO = new GenericDAOImpl<Domains, Long>(Domains.class);
		return domainsDAO.getAll();
	}
	
	@Override
	public void createDomain(String domainName) {
		if (domainName == null || "".equals(domainName)) {
			throw new DataCommonsException(400, "No Domain Name specified");
		}
		Domains domain = new Domains();
		domain.setDomain_name(domainName);
		
		GenericDAO<Domains, Long> domainsDAO = new GenericDAOImpl<Domains, Long>(Domains.class);
		domain = domainsDAO.create(domain);
		
		permissionService.initializeDomainPermissions(domain);
	}
	
	public List<Groups> getGroups() {
		GenericDAO<Groups, Long> groupsDAO = new GenericDAOImpl<Groups, Long>(Groups.class);
		return groupsDAO.getAll();
	}

	@Override
	public void createGroup(String groupName, Long domainId) {
		if (groupName == null || "".equals(groupName)) {
			throw new DataCommonsException(400, "No group name specified");
		}
		if (domainId == null) {
			throw new DataCommonsException(400, "No associated domain specified");
		}
		GenericDAO<Domains, Long> domainsDAO = new GenericDAOImpl<Domains, Long>(Domains.class);
		Domains domain = domainsDAO.getSingleById(domainId);
		if (domain == null) {
			throw new DataCommonsException(400, "No domain found");
		}
		
		Groups group = new Groups();
		group.setGroup_name(groupName);
		
		GenericDAO<Groups, Long> groupsDAO = new GenericDAOImpl<Groups, Long>(Groups.class);
		group = groupsDAO.create(group);
		
		permissionService.initializeGroupPermissions(group, domain);
	}
}
