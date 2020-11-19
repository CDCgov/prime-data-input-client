package gov.cdc.usds.simplereport.db.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.NaturalId;

@Entity
public class Organization extends EternalEntity {

	@Column(nullable = false, unique = true)
	private String facilityName;

	@Column(name="organization_external_id", nullable=false, unique=true)
	@NaturalId
	private String externalId;

	@ManyToOne(optional = true)
	@JoinColumn(name = "default_device_type")
	private DeviceType defaultDeviceType;

	@ManyToMany
	@JoinTable(
		name = "facility_device_type",
		joinColumns = @JoinColumn(name="organization_id"),
		inverseJoinColumns = @JoinColumn(name="device_type_id")
	)
	private Set<DeviceType> configuredDevices = new HashSet<>();

	public Organization(String facilityName, String externalId) {
		super();
		this.facilityName = facilityName;
		this.externalId = externalId;
	}
	public Organization(String facilityName, String externalId, DeviceType defaultDeviceType) {
		this(facilityName, externalId);
		this.defaultDeviceType = defaultDeviceType;
	}

	public Organization(String facilityName, String externalId, DeviceType defaultDeviceType,
			Collection<DeviceType> configuredDevices) {
		this(facilityName, externalId, defaultDeviceType);
		this.configuredDevices.addAll(configuredDevices);
	}

	public String getFacilityName() {
		return facilityName;
	}

	public String getExternalId() {
		return externalId;
	}

	public DeviceType getDefaultDeviceType() {
		return defaultDeviceType;
	}

	public Set<DeviceType> getDeviceTypes() {
		// this might be better done on the DB side, but that seems like a recipe for weird behaviors
		return configuredDevices.stream()
			.filter(e -> !e.isDeleted())
			.collect(Collectors.toSet());
	}

	public void addDeviceType(DeviceType newDevice) {
		configuredDevices.add(newDevice);
	}

	public void addDefaultDeviceType(DeviceType newDevice) {
		this.defaultDeviceType = newDevice;
		addDeviceType(newDevice);
	}

	public void removeDeviceType(DeviceType existingDevice) {
		configuredDevices.remove(existingDevice);
		if (defaultDeviceType != null && existingDevice.getInternalId().equals(defaultDeviceType.getInternalId())) {
			defaultDeviceType = null;
		}
	}
}
