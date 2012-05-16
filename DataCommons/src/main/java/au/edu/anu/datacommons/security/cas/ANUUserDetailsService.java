package au.edu.anu.datacommons.security.cas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

import au.edu.anu.datacommons.data.db.dao.UsersDAO;
import au.edu.anu.datacommons.data.db.dao.UsersDAOImpl;
import au.edu.anu.datacommons.data.db.model.Users;
import au.edu.anu.datacommons.security.CustomUser;


/**
 * ANUUserDetailsService
 * 
 * Australian National University Data Commons
 * 
 * The ANUUserDetailsService class adds default roles to an ANU User logged in via CAS.
 * The roles currently include 'ROLE_ANU_USER' and 'ROLE_REGISTERED'.
 * 
 * <pre>
 * Version	Date		Developer				Description
 * 0.1		26/04/2012	Genevieve Turner (GT)	Initial
 * 0.2		16/05/2012	Genevieve Turner (GT)	Updated to use a custom user
 * </pre>
 * 
 */
public class ANUUserDetailsService extends JdbcDaoImpl {
	static final Logger LOGGER = LoggerFactory.getLogger(ANUUserDetailsService.class);
	
	private boolean enableAuthorities = true;
	private boolean enableGroups = false;
	
	/**
	 * loadUserByUsername
	 * 
	 * Overrides the loadUserByUsername class so that the user is not required to be
	 * in the database to be able to log in.  It still retrieves additional permissions
	 * for the user if they exist.
	 * 
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.1		29/03/2012	Genevieve Turner (GT)	Added
	 * 0.2		16/05/2012	Genevieve Turner (GT)	Updated to use a custom user
	 * </pre>
	 * 
	 * @param username The username of the person logging in
	 * @return Returns information about the user
	 */
	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		Set<GrantedAuthority> dbAuthsSet = new HashSet<GrantedAuthority>();

        if (enableAuthorities) {
            dbAuthsSet.addAll(loadUserAuthorities(username));
        }

        if (enableGroups) {
            dbAuthsSet.addAll(loadGroupAuthorities(username));
        }

        List<GrantedAuthority> dbAuths = new ArrayList<GrantedAuthority>(dbAuthsSet);

        addCustomAuthorities(username, dbAuths);

        if (dbAuths.size() == 0) {
            logger.debug("User '" + username + "' has no authorities and will be treated as 'not found'");

            throw new UsernameNotFoundException(
                    messages.getMessage("JdbcDaoImpl.noAuthority",
                            new Object[] {username}, "User {0} has no GrantedAuthority"), username);
        }
        CustomUser user = loadCustomUser(username, dbAuths);
		return user;
        //return createUserDetails(username, user, dbAuths);
	}

	/**
	 * loadCustomUser
	 * 
	 * Loads the details of a custom user
	 * 
	 * <pre>
	 * Version	Date		Developer				Description
	 * 0.2		16/05/2012	Genevieve Turner (GT)	Updated to use a custom user
	 * </pre>
	 * 
	 * @param username The username of the person logging in
	 * @param authorities The authorities for the user logging in
	 * @return The custom user
	 */
	private CustomUser loadCustomUser(String username, List<GrantedAuthority> authorities) {
		UsersDAO usersDAO = new UsersDAOImpl(Users.class);
		Users users = usersDAO.getUserByName(username);
		CustomUser user = null;
		if (users != null) {
			user = new CustomUser(users.getUsername(), users.getPassword(), true, true, true, true, authorities, users.getId());
		}
		else {
			user = new CustomUser(username, username, true, true, true, true, authorities, new Long(-1));
		}
		return user;
	}

	/**
	 * addCustomAuthorities
	 * 
	 * Adds custom authorities to the logged in user.  Currently these include 'ROLE_ANU_USER'
	 * and 'ROLE_REGISTERED'
	 * 
	 * Version	Date		Developer				Description
	 * 0.1		26/04/2012	Genevieve Turner (GT)	Added
	 * 
	 * @param username The username of the person logging in
	 * @param authorities A list of the users authorities
	 */
	protected void addCustomAuthorities(String username, List<GrantedAuthority> authorities) {
		authorities.add(new GrantedAuthorityImpl("ROLE_ANU_USER"));
		authorities.add(new GrantedAuthorityImpl("ROLE_REGISTERED"));
	}
}
