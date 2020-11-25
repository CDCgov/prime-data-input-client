package gov.cdc.usds.simplereport.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.cdc.usds.simplereport.db.model.DeviceType;
import gov.cdc.usds.simplereport.db.repository.BaseRepositoryTest;
import gov.cdc.usds.simplereport.db.repository.DeviceTypeRepository;

public class DeviceTypeServiceTest extends BaseRepositoryTest {

	@Autowired
	private DeviceTypeRepository _repo;

	@Test
	public void insertAndFindAndDeleteAndSoForth() {
		DeviceTypeService _service = new DeviceTypeService(_repo);
		DeviceType devA = _service.createDeviceType("A", "B", "C", "DUMMY");
		DeviceType devB = _service.createDeviceType("D", "E", "F", "DUMMY");
		assertNotNull(devA);
		assertNotNull(devB);
		assertNotEquals(devA.getInternalId(), devB.getInternalId());
		List<DeviceType> found = _service.fetchDeviceTypes();
		assertEquals(2, found.size());
		_service.removeDeviceType(devB);
		found = _service.fetchDeviceTypes();
		assertEquals(1, found.size());
	}
}
