package gov.cdc.usds.simplereport.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.cdc.usds.simplereport.db.model.DeviceType;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.Provider;
import gov.cdc.usds.simplereport.db.model.StreetAddress;
import gov.cdc.usds.simplereport.db.repository.OrganizationRepository;

@Service
@Transactional(readOnly=false)
public class OrganizationService {

	private OrganizationRepository _repo;
	private OrganizationInitializingService _initService;

	public OrganizationService(OrganizationRepository repo,
			OrganizationInitializingService initService) {
		_repo = repo;
		_initService = initService;
	}

	public Organization getCurrentOrganization() {
    	_initService.initAll();
		Optional<Organization> maybe = _repo.findByExternalId(_initService.getDefaultOrganizationId());
		if (maybe.isPresent()) {
			return maybe.get();
		} else {
			throw new RuntimeException("Default organization not found: serious troubles");
		}
	}

	public Organization updateOrganization(
		String testingFacilityName,
		String cliaNumber,
		String orderingProviderFirstName,
		String orderingProviderMiddleName,
		String orderingProviderLastName,
		String orderingProviderSuffix,
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
		org.setCliaNumber(cliaNumber);
		org.addDefaultDeviceType(defaultDeviceType);

		Provider p = org.getOrderingProvider();
		p.getNameInfo().setFirstName(orderingProviderFirstName);
		p.getNameInfo().setMiddleName(orderingProviderMiddleName);
		p.getNameInfo().setLastName(orderingProviderLastName);
		p.getNameInfo().setSuffix(orderingProviderSuffix);
		p.setProviderId(orderingProviderNPI);
		p.setTelephone(orderingProviderTelephone);

		StreetAddress a = p.getAddress() == null ? new StreetAddress(
			orderingProviderStreet,
			orderingProviderStreetTwo,
			orderingProviderCity,
			orderingProviderState,
			orderingProviderZipCode,
			orderingProviderCounty
		 ) : p.getAddress();
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
		org.setDefaultDeviceType(defaultDeviceType);
		return _repo.save(org);

	}
}
