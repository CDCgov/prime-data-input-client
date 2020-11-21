package gov.cdc.usds.simplereport.service;

import java.util.Optional;
import java.util.List;
import java.util.Iterator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.cdc.usds.simplereport.db.model.DeviceType;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.Provider;
import gov.cdc.usds.simplereport.db.model.StreetAddress;
import gov.cdc.usds.simplereport.db.repository.OrganizationRepository;
import gov.cdc.usds.simplereport.db.repository.ProviderRepository;

@Service
@Transactional(readOnly=false)
public class OrganizationService {

	private OrganizationRepository _repo;
	private ProviderRepository _providerRepo;

	public static final String FAKE_ORG_ID = "123";

	public OrganizationService(OrganizationRepository repo, ProviderRepository providers) {
		_repo = repo;
		_providerRepo = providers;
	}

	public Organization getCurrentOrganization() {
		Optional<Organization> maybe = _repo.findByExternalId(FAKE_ORG_ID);
		if (maybe.isPresent()) {
			return maybe.get();
		} else {
			Provider p = _providerRepo.save(new Provider("Doctor Dread", "XXXXX", null, "(202) 555-4321"));
			return _repo.save(new Organization("This Place", FAKE_ORG_ID, null, p));
		}
	}

	public Organization updateOrganization(
		String testingFacilityName,
		String cliaNumber,
		String orderingProviderName,
		String orderingProviderNPI,
		String orderingProviderStreet,
		String orderingProviderStreetTwo,
		String orderingProviderCity,
		String orderingProviderCounty,
		String orderingProviderState,
		String orderingProviderZipCode,
		String orderingProviderTelephone,
		List<DeviceType> devices,
		DeviceType defaultDeviceType
	) {
		Organization org = this.getCurrentOrganization();
		org.setFacilityName(testingFacilityName);
		org.setExternalId(cliaNumber);
		org.addDefaultDeviceType(defaultDeviceType);

		Provider p = org.getOrderingProvider();
		p.setName(orderingProviderName);
		p.setProviderId(orderingProviderNPI);
		p.setTelephone(orderingProviderTelephone);

		StreetAddress a = p.getAddress();
		a.setStreet(orderingProviderStreet, orderingProviderStreetTwo);
		a.setCity(orderingProviderCity);
		a.setCounty(orderingProviderCounty);
		a.setState(orderingProviderState);
		a.setPostalCode(orderingProviderZipCode);

		// remove all existing devices
		for(DeviceType d : org.getDeviceTypes()) {
			org.removeDeviceType(d);
		}

		// add new devices
		for(DeviceType d : devices) {
			org.addDeviceType(d);
		}
		return _repo.save(org);

	}
}
