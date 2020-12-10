package gov.cdc.usds.simplereport.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.cdc.usds.simplereport.api.model.errors.IllegalGraphqlArgumentException;
import gov.cdc.usds.simplereport.db.model.DeviceType;
import gov.cdc.usds.simplereport.db.repository.DeviceTypeRepository;
import gov.cdc.usds.simplereport.service.model.DeviceTypeHolder;

/**
 * Service for fetching the device-type reference list (<i>not</i> the device types available for a
 * specific facility or organization).
 */
@Service
@Transactional(readOnly = true)
public class DeviceTypeService {

	private DeviceTypeRepository _repo;
	public DeviceTypeService(DeviceTypeRepository repo) {
		_repo = repo;
	}

	@Transactional(readOnly = false)
	public void removeDeviceType(DeviceType d) {
		_repo.delete(d);
	}

	public List<DeviceType> fetchDeviceTypes() {
		return _repo.findAll();
	}


	public DeviceType getDeviceType(String internalId) {
		UUID actualId = UUID.fromString(internalId);
		return _repo.findById(actualId).orElseThrow(()->new IllegalGraphqlArgumentException("invalid device type ID"));
	}

	@Transactional(readOnly = false)
	public DeviceType createDeviceType(String name, String model, String manufacturer, String loincCode) {
		return _repo.save(new DeviceType(name, manufacturer, model, loincCode));
	}

	public DeviceTypeHolder getTypesForFacility(String defaultDeviceTypeId, List<String> configuredDeviceTypeIds) {
		if (!configuredDeviceTypeIds.contains(defaultDeviceTypeId)) {
			throw new IllegalGraphqlArgumentException("default device type must be included in device type list");
		}
		List<DeviceType> configuredTypes = configuredDeviceTypeIds.stream()
				.map(this::getDeviceType)
				.collect(Collectors.toList());
		UUID defaultId = UUID.fromString(defaultDeviceTypeId);
		DeviceType defaultType = configuredTypes.stream()
			.filter(dt->dt.getInternalId().equals(defaultId))
			.findFirst()
			.orElseThrow(()->new RuntimeException("Inexplicable inability to find device for ID " + defaultId.toString()))
			;
		return new DeviceTypeHolder(defaultType, configuredTypes);
	}
}
