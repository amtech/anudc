package au.edu.anu.datacommons.data.db.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * ReviewReject
 * 
 * Australian National University Data Commons
 * 
 * A class that indicates that the object has been rejected
 *
 * JUnit Coverage:
 * None
 * 
 * <pre>
 * Version	Date		Developer				Description
 * 0.1		25/07/2012	Genevieve Turner (GT)	Initial
 * </pre>
 *
 */
@Entity
@Table(name="review_reject")
public class ReviewReject {
	private Long id;
	private Date date_submitted;
	private String reason;
	
	/**
	 * getId
	 *
	 * Get the id
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * X.X		24/07/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @return the id
	 */
	@Id
	public Long getId() {
		return id;
	}

	/**
	 * setId
	 *
	 * Set the id
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * X.X		24/07/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * getDate_submitted
	 *
	 * Get the date submitted
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * X.X		24/07/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @return the date_submitted
	 */
	@Column(name="date_submitted")
	public Date getDate_submitted() {
		return date_submitted;
	}

	/**
	 * setDate_submitted
	 *
	 * Set the date submitted
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * X.X		24/07/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param date_submitted the date_submitted to set
	 */
	public void setDate_submitted(Date date_submitted) {
		this.date_submitted = date_submitted;
	}

	/**
	 * getReason
	 *
	 * Get the rejection reason
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * X.X		24/07/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @return the reason
	 */
	@Column(name="reason")
	public String getReason() {
		return reason;
	}

	/**
	 * setReason
	 *
	 * Set the rejection reason
	 *
	 * <pre>
	 * Version	Date		Developer				Description
	 * X.X		24/07/2012	Genevieve Turner(GT)	Initial
	 * </pre>
	 * 
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}
}
