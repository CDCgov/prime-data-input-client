package gov.cdc.usds.simplereport.api.organization;

import gov.cdc.usds.simplereport.api.Translators;
import gov.cdc.usds.simplereport.api.model.ApiFacility;
import gov.cdc.usds.simplereport.api.model.ApiOrganization;
import gov.cdc.usds.simplereport.db.model.Facility;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonName;
import gov.cdc.usds.simplereport.db.model.auxiliary.StreetAddress;
import gov.cdc.usds.simplereport.service.AddressValidationService;
import gov.cdc.usds.simplereport.service.DeviceTypeService;
import gov.cdc.usds.simplereport.service.OrganizationService;
import gov.cdc.usds.simplereport.service.model.DeviceSpecimenTypeHolder;
import graphql.kickstart.tools.GraphQLMutationResolver;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Created by nickrobison on 11/17/20 */
@Component
public class OrganizationMutationResolver implements GraphQLMutationResolver {

  private final OrganizationService _os;
  private final DeviceTypeService _dts;
  private final AddressValidationService _avs;

  public OrganizationMutationResolver(
      OrganizationService os, DeviceTypeService dts, AddressValidationService avs) {
    _os = os;
    _dts = dts;
    _avs = avs;
  }

  public ApiFacility addFacility(
      String testingFacilityName,
      String cliaNumber,
      String street,
      String streetTwo,
      String city,
      String county,
      String state,
      String zipCode,
      String phone,
      String email,
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
      List<String> deviceIds,
      String defaultDeviceId) {
    _os.assertFacilityNameAvailable(testingFacilityName);
    DeviceSpecimenTypeHolder deviceSpecimenTypes =
        _dts.getTypesForFacility(defaultDeviceId, deviceIds);
    StreetAddress facilityAddress =
        _avs.getValidatedAddress(
            street, streetTwo, city, state, zipCode, _avs.FACILITY_DISPLAY_NAME);
    StreetAddress providerAddress =
        new StreetAddress(
            Translators.parseString(orderingProviderStreet),
            Translators.parseString(orderingProviderStreetTwo),
            Translators.parseString(orderingProviderCity),
            Translators.parseState(orderingProviderState),
            Translators.parseString(orderingProviderZipCode),
            Translators.parseString(orderingProviderCounty));
    PersonName providerName =
        new PersonName(
            orderingProviderFirstName,
            orderingProviderMiddleName,
            orderingProviderLastName,
            orderingProviderSuffix);
    Facility created =
        _os.createFacility(
            testingFacilityName,
            cliaNumber,
            facilityAddress,
            phone,
            Translators.parseEmail(email),
            deviceSpecimenTypes,
            providerName,
            providerAddress,
            orderingProviderTelephone,
            orderingProviderNPI);
    return new ApiFacility(created);
  }

  public ApiFacility updateFacility(
      UUID facilityId,
      String testingFacilityName,
      String cliaNumber,
      String street,
      String streetTwo,
      String city,
      String county,
      String state,
      String zipCode,
      String phone,
      String email,
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
      List<String> deviceIds,
      String defaultDeviceId) {
    DeviceSpecimenTypeHolder deviceSpecimenTypes =
        _dts.getTypesForFacility(defaultDeviceId, deviceIds);
    StreetAddress facilityAddress =
        _avs.getValidatedAddress(
            street, streetTwo, city, state, zipCode, _avs.FACILITY_DISPLAY_NAME);
    StreetAddress providerAddress =
        new StreetAddress(
            Translators.parseString(orderingProviderStreet),
            Translators.parseString(orderingProviderStreetTwo),
            Translators.parseString(orderingProviderCity),
            Translators.parseState(orderingProviderState),
            Translators.parseString(orderingProviderZipCode),
            Translators.parseString(orderingProviderCounty));
    Facility facility =
        _os.updateFacility(
            facilityId,
            testingFacilityName,
            cliaNumber,
            facilityAddress,
            phone,
            Translators.parseEmail(email),
            orderingProviderFirstName,
            orderingProviderMiddleName,
            orderingProviderLastName,
            orderingProviderSuffix,
            orderingProviderNPI,
            providerAddress,
            orderingProviderTelephone,
            deviceSpecimenTypes);
    return new ApiFacility(facility);
  }

  public ApiOrganization createOrganization(
      String name,
      String externalId,
      String testingFacilityName,
      String cliaNumber,
      String street,
      String streetTwo,
      String city,
      String county,
      String state,
      String zipCode,
      String phone,
      String email,
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
      List<String> deviceIds,
      String defaultDeviceId) {
    DeviceSpecimenTypeHolder deviceSpecimenTypes =
        _dts.getTypesForFacility(defaultDeviceId, deviceIds);
    StreetAddress facilityAddress =
        _avs.getValidatedAddress(
            street, streetTwo, city, state, zipCode, _avs.FACILITY_DISPLAY_NAME);
    StreetAddress providerAddress =
        new StreetAddress(
            Translators.parseString(orderingProviderStreet),
            Translators.parseString(orderingProviderStreetTwo),
            Translators.parseString(orderingProviderCity),
            Translators.parseState(orderingProviderState),
            Translators.parseString(orderingProviderZipCode),
            Translators.parseString(orderingProviderCounty));
    PersonName providerName =
        new PersonName(
            orderingProviderFirstName,
            orderingProviderMiddleName,
            orderingProviderLastName,
            orderingProviderSuffix);
    Organization org =
        _os.createOrganization(
            name,
            externalId,
            testingFacilityName,
            cliaNumber,
            facilityAddress,
            phone,
            Translators.parseEmail(email),
            deviceSpecimenTypes,
            providerName,
            providerAddress,
            orderingProviderTelephone,
            orderingProviderNPI);
    List<Facility> facilities = _os.getFacilities(org);
    return new ApiOrganization(org, facilities);
  }

  public void updateOrganization(String name) {
    _os.updateOrganization(name);
  }
}
