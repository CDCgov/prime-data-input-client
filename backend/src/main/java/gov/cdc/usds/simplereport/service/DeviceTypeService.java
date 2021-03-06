package gov.cdc.usds.simplereport.service;

import static gov.cdc.usds.simplereport.utils.DeviceTestLengthConverter.determineTestLength;

import gov.cdc.usds.simplereport.api.model.errors.IllegalGraphqlArgumentException;
import gov.cdc.usds.simplereport.config.AuthorizationConfiguration;
import gov.cdc.usds.simplereport.db.model.DeviceSpecimenType;
import gov.cdc.usds.simplereport.db.model.DeviceType;
import gov.cdc.usds.simplereport.db.model.SpecimenType;
import gov.cdc.usds.simplereport.db.repository.DeviceSpecimenTypeRepository;
import gov.cdc.usds.simplereport.db.repository.DeviceTypeRepository;
import gov.cdc.usds.simplereport.db.repository.SpecimenTypeRepository;
import gov.cdc.usds.simplereport.service.model.DeviceSpecimenTypeHolder;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for fetching the device-type reference list (<i>not</i> the device types available for a
 * specific facility or organization).
 */
@Service
@Transactional(readOnly = true)
public class DeviceTypeService {

  private DeviceTypeRepository _repo;
  private DeviceSpecimenTypeRepository _deviceSpecimenRepo;
  private SpecimenTypeRepository _specimenTypeRepo;

  public DeviceTypeService(
      DeviceTypeRepository repo,
      DeviceSpecimenTypeRepository deviceSpecimenRepo,
      SpecimenTypeRepository specimenTypeRepo) {
    _repo = repo;
    _deviceSpecimenRepo = deviceSpecimenRepo;
    _specimenTypeRepo = specimenTypeRepo;
  }

  @Transactional(readOnly = false)
  @AuthorizationConfiguration.RequireGlobalAdminUser
  public void removeDeviceType(DeviceType d) {
    _repo.delete(d);
  }

  public List<DeviceType> fetchDeviceTypes() {
    return _repo.findAll();
  }

  public DeviceType getDeviceType(String internalId) {
    UUID actualId = UUID.fromString(internalId);
    return _repo
        .findById(actualId)
        .orElseThrow(() -> new IllegalGraphqlArgumentException("invalid device type ID"));
  }

  /**
   * Find the original device/specimen type combination created for this DeviceType, since that will
   * be the one that is assumed by callers who aren't aware that you can have multiple specimen
   * types for a given device type.
   */
  @Deprecated // this is a backward-compatibility shim!
  public DeviceSpecimenType getDefaultForDeviceId(String deviceId) {
    UUID actualDeviceId = UUID.fromString(deviceId);
    return _deviceSpecimenRepo
        .findFirstByDeviceTypeInternalIdOrderByCreatedAt(actualDeviceId)
        .orElseThrow(
            () ->
                new IllegalGraphqlArgumentException(
                    "Device is not configured with a specimen type"));
  }

  @Transactional(readOnly = false)
  @AuthorizationConfiguration.RequireGlobalAdminUser
  public DeviceType updateDeviceType(
      UUID id, String name, String model, String manufacturer, String loincCode, String swabType) {
    DeviceType d = getDeviceType(id.toString());
    if (name != null) {
      d.setName(name);
      d.setTestLength((determineTestLength(name)));
    }
    if (manufacturer != null) {
      d.setManufacturer(manufacturer);
    }
    if (model != null) {
      d.setModel(model);
    }
    if (loincCode != null) {
      d.setLoincCode(loincCode);
    }
    if (swabType != null) {
      throw new IllegalGraphqlArgumentException("swab type editing is temporarily unavailable");
    }
    return _repo.save(d);
  }

  @Transactional(readOnly = false)
  @AuthorizationConfiguration.RequireGlobalAdminUser
  public DeviceType createDeviceType(
      String name, String model, String manufacturer, String loincCode, String swabType) {
    @SuppressWarnings("deprecation") // this is a shim
    SpecimenType st =
        _specimenTypeRepo
            .findByTypeCode(swabType)
            .orElseGet(
                () ->
                    _specimenTypeRepo.save(
                        new SpecimenType("Auto-generated " + swabType, swabType)));
    if (st.isDeleted()) {
      throw new IllegalGraphqlArgumentException("swab type has been deleted and cannot be used");
    }
    DeviceType dt =
        _repo.save(
            new DeviceType(
                name, manufacturer, model, loincCode, swabType, determineTestLength(name)));
    _deviceSpecimenRepo.save(new DeviceSpecimenType(dt, st));
    return dt;
  }

  /**
   * Retrieve the {@link DeviceSpecimenTypeHolder} that this operation needs based on the <b>DEVICE
   * TYPE</b> IDs supplied to a mutation.
   *
   * @deprecated in favor of (eventually) using device-specimen-type IDs instead of device-type IDs.
   * @param defaultDeviceTypeId the default DEVICE TYPE id to configure for this operation.
   * @param configuredDeviceTypeIds the list of device type IDS to configure. <b>Must contain the
   *     default value.</b>
   * @return a {@link DeviceSpecimenTypeHolder} that holds the default {@link DeviceSpecimenType}
   *     for each of the device types that was supplied either in the configured list or as the
   *     default.
   */
  @Deprecated
  public DeviceSpecimenTypeHolder getTypesForFacility(
      String defaultDeviceTypeId, List<String> configuredDeviceTypeIds) {
    if (!configuredDeviceTypeIds.contains(defaultDeviceTypeId)) {
      throw new IllegalGraphqlArgumentException(
          "default device type must be included in device type list");
    }
    List<DeviceSpecimenType> configuredTypes =
        configuredDeviceTypeIds.stream()
            .map(this::getDefaultForDeviceId)
            .collect(Collectors.toList());
    UUID defaultId = UUID.fromString(defaultDeviceTypeId);
    DeviceSpecimenType defaultType =
        configuredTypes.stream()
            .filter(dt -> dt.getDeviceType().getInternalId().equals(defaultId))
            .findFirst()
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Inexplicable inability to find device for ID " + defaultId.toString()));
    return new DeviceSpecimenTypeHolder(defaultType, configuredTypes);
  }
}
