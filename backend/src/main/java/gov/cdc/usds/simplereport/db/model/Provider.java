package gov.cdc.usds.simplereport.db.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import gov.cdc.usds.simplereport.db.model.auxiliary.PersonName;

@Entity
public class Provider extends EternalEntity {

	@Embedded
	@JsonUnwrapped
	private PersonName nameInfo;
	@Column(nullable = false)
	private String providerId;
	@Embedded
	private StreetAddress address;
	@Column
	private String telephone;

	protected Provider() { /* for hibernate */ }

	public Provider(String firstName, String middleName, String lastName, String suffix, String providerId, StreetAddress address, String telephone) {
		this.nameInfo = new PersonName(firstName, middleName, lastName, suffix);
		this.providerId = providerId;
		this.address = address;
		this.telephone = telephone;
	}

	public PersonName getNameInfo() {
		return nameInfo;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public StreetAddress getAddress() {
		return address;
	}

	public void setAddress(StreetAddress address) {
		this.address = address;
	}

	@JsonIgnore
	public String getStreet() {
		if(address == null || address.getStreet() == null || address.getStreet().isEmpty()) {
			return "";
		}
		return address.getStreet().get(0);
	}

	@JsonIgnore
	public String getStreetTwo() {
		if(address == null || address.getStreet() == null || address.getStreet().size() < 2) {
			return "";
		}
		return address.getStreet().get(1);
	}

	@JsonIgnore
	public String getCity() {
		if(address == null) {
			return "";
		}
		return address.getCity();
	}

	@JsonIgnore
	public String getState() {
		if(address == null) {
			return "";
		}
		return address.getState();
	}

	@JsonIgnore
	public String getZipCode() {
		if(address == null) {
			return "";
		}
		return address.getPostalCode();
	}

	@JsonIgnore
	public String getCounty() {
		if(address == null) {
			return "";
		}
		return address.getCounty();
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
}
