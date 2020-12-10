package gov.cdc.usds.simplereport.api.model;

import gov.cdc.usds.simplereport.db.model.ApiUser;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonName;

public class User {

	private String id;
	private Organization org;
	private PersonName nameInfo;

	public User(ApiUser currentUser, Organization currentOrg) {
		super();
		this.id = currentUser.getInternalId().toString();
		this.org = currentOrg;
		this.nameInfo = currentUser.getNameInfo();
	}

	public String getId() {
		return id;
	}

	public Organization getOrganization() {
		return org;
	}

	public String getFirstName() {
		return nameInfo.getFirstName();
	}

	public String getMiddleName() {
		return nameInfo.getMiddleName();
	}

	public String getLastName() {
		return nameInfo.getLastName();
	}

	public String getSuffix() {
		return nameInfo.getSuffix();
	}
}
