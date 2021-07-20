package gov.cdc.usds.simplereport.api.accountrequest;

import static gov.cdc.usds.simplereport.config.AuthorizationConfiguration.AUTHORIZER_BEAN;
import static gov.cdc.usds.simplereport.config.WebConfiguration.ACCOUNT_REQUEST;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.usds.simplereport.api.Translators;
import gov.cdc.usds.simplereport.api.accountrequest.errors.AccountRequestFailureException;
import gov.cdc.usds.simplereport.api.model.Role;
import gov.cdc.usds.simplereport.api.model.accountrequest.AccountRequest;
import gov.cdc.usds.simplereport.api.model.accountrequest.AccountResponse;
import gov.cdc.usds.simplereport.api.model.accountrequest.WaitlistRequest;
import gov.cdc.usds.simplereport.db.model.DeviceType;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonName;
import gov.cdc.usds.simplereport.db.model.auxiliary.StreetAddress;
import gov.cdc.usds.simplereport.properties.SendGridProperties;
import gov.cdc.usds.simplereport.service.AddressValidationService;
import gov.cdc.usds.simplereport.service.ApiUserService;
import gov.cdc.usds.simplereport.service.DeviceTypeService;
import gov.cdc.usds.simplereport.service.OrganizationService;
import gov.cdc.usds.simplereport.service.crm.CrmService;
import gov.cdc.usds.simplereport.service.email.EmailProviderTemplate;
import gov.cdc.usds.simplereport.service.email.EmailService;
import gov.cdc.usds.simplereport.service.model.DeviceSpecimenTypeHolder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Note that this controller is automatically authorized. */
@PreAuthorize("@" + AUTHORIZER_BEAN + ".permitAllAccountRequests()")
@PostAuthorize("@restAuditLogManager.logAnonymousRestSuccess(#request, returnObject)")
@RestController
@RequestMapping(ACCOUNT_REQUEST)
public class AccountRequestController {
  private final OrganizationService _os;
  private final DeviceTypeService _dts;
  private final AddressValidationService _avs;
  private final ApiUserService _aus;
  private final EmailService _es;
  private final SendGridProperties sendGridProperties;
  private final CrmService _crm;
  private final ObjectMapper objectMapper;

  private static final Logger LOG = LoggerFactory.getLogger(AccountRequestController.class);

  public AccountRequestController(
      SendGridProperties sendGridProperties,
      OrganizationService os,
      DeviceTypeService dts,
      AddressValidationService avs,
      ApiUserService aus,
      EmailService es,
      CrmService crm) {
    this.sendGridProperties = sendGridProperties;
    this._os = os;
    this._dts = dts;
    this._avs = avs;
    this._aus = aus;
    this._es = es;
    this._crm = crm;
    this.objectMapper = new ObjectMapper();
  }

  @PostConstruct
  private void init() {
    LOG.info("Account request REST endpoint enabled");
  }

  /** Read the waitlist request and generate an email body, then send with the emailService */
  @PostMapping("/waitlist")
  public void submitWaitlistRequest(
      @Valid @RequestBody WaitlistRequest body, HttpServletRequest request) throws IOException {
    String subject = "New waitlist request";
    if (LOG.isInfoEnabled()) {
      LOG.info("Waitlist request submitted: {}", objectMapper.writeValueAsString(body));
    }
    _es.send(sendGridProperties.getWaitlistRecipient(), subject, body);
  }

  /**
   * Read the account request and generate an email body, then send with the emailService and create
   * org
   */
  @PostMapping("")
  @Transactional(readOnly = false)
  public AccountResponse submitAccountRequest(@Valid @RequestBody AccountRequest body)
      throws IOException {
    try {
      String subject = "New account request";
      if (LOG.isInfoEnabled()) {
        LOG.info("Account request submitted: {}", objectMapper.writeValueAsString(body));
      }

      Map<String, String> reqVars =
          body.toTemplateVariables().entrySet().stream()
              .collect(
                  HashMap::new,
                  (m, e) ->
                      m.put(e.getKey(), e.getValue() == null ? null : e.getValue().toString()),
                  HashMap::putAll);

      List<DeviceType> devices = _dts.fetchDeviceTypes();
      Map<String, String> deviceNamesToIds =
          devices.stream()
              .collect(Collectors.toMap(d -> d.getName(), d -> d.getInternalId().toString()));
      Map<String, String> deviceModelsToIds =
          devices.stream()
              .collect(Collectors.toMap(d -> d.getModel(), d -> d.getInternalId().toString()));

      List<String> testingDevicesSubmitted =
          new ArrayList<>(Arrays.asList(reqVars.get("testingDevices").split(", ")));
      testingDevicesSubmitted.removeIf(d -> d.toLowerCase().startsWith("other"));
      List<String> unregisteredTestingDevices = new ArrayList<>();
      List<String> testingDeviceIds =
          testingDevicesSubmitted.stream()
              .map(
                  d -> {
                    String deviceId =
                        Optional.ofNullable(deviceNamesToIds.get(d))
                            .orElse(deviceModelsToIds.get(d));
                    if (deviceId == null) {
                      unregisteredTestingDevices.add(d);
                    }
                    return deviceId;
                  })
              .collect(Collectors.toList());
      // Can't easily catch a thrown exception in a stream, so move the throw outside of the stream:
      if (!unregisteredTestingDevices.isEmpty()) {
        throw new IOException(
            String.format(
                "Submitted device=%s not registered in DB.",
                unregisteredTestingDevices.iterator().next()));
      }
      String defaultTestingDeviceId =
          Optional.ofNullable(deviceNamesToIds.get(reqVars.get("defaultTestingDevice")))
              .orElse(deviceModelsToIds.get(reqVars.get("defaultTestingDevice")));
      if (defaultTestingDeviceId == null) {
        throw new IOException(
            String.format(
                "Submitted default device=%s not registered in DB.",
                reqVars.get("defaultTestingDevice")));
      }
      DeviceSpecimenTypeHolder deviceSpecimenTypes =
          _dts.getTypesForFacility(defaultTestingDeviceId, testingDeviceIds);

      StreetAddress facilityAddress =
          _avs.getValidatedAddress(
              reqVars.get("streetAddress1"),
              reqVars.get("streetAddress2"),
              reqVars.get("city"),
              reqVars.get("state"),
              reqVars.get("zip"),
              _avs.FACILITY_DISPLAY_NAME);
      StreetAddress providerAddress =
          new StreetAddress(
              Translators.parseString(reqVars.get("opStreetAddress1")),
              Translators.parseString(reqVars.get("opStreetAddress2")),
              Translators.parseString(reqVars.get("opCity")),
              Translators.parseState(reqVars.get("opState")),
              Translators.parseString(reqVars.get("opZip")),
              Translators.parseString(reqVars.get("opCounty")));

      PersonName providerName =
          Translators.consolidateNameArguments(
              null, reqVars.get("opFirstName"), null, reqVars.get("opLastName"), null, true);
      PersonName adminName =
          Translators.consolidateNameArguments(
              null, reqVars.get("firstName"), null, reqVars.get("lastName"), null);

      String orgExternalId =
          String.format(
              "%s-%s-%s",
              reqVars.get("state"),
              reqVars.get("organizationName").replace(' ', '-').replace(':', '-'),
              UUID.randomUUID().toString());

      Organization org =
          _os.createOrganization(
              reqVars.get("organizationName"),
              Translators.parseOrganizationTypeFromName(reqVars.get("organizationType")),
              orgExternalId,
              reqVars.get("facilityName"),
              reqVars.get("cliaNumber"),
              facilityAddress,
              Translators.parsePhoneNumber(reqVars.get("facilityPhoneNumber")),
              null,
              deviceSpecimenTypes,
              providerName,
              providerAddress,
              Translators.parsePhoneNumber(reqVars.get("opPhoneNumber")),
              reqVars.get("npi"));

      /**
       * Note: we are sending the emails *after* creating the organization so the validation logic
       * in the organization creation process functions as a CAPTCHA. But we are sending the emails
       * *before* creating the user so that if sendgrid etc fails, we aren't left with an Okta
       * entity created for the user that can't be automatically rolled back. (When we add automatic
       * Okta rollback mechanisms down the road, this won't be an issue.)
       */
      // send summary email to SR support
      _es.send(sendGridProperties.getAccountRequestRecipient(), subject, body);
      // send next-steps email to requester
      _es.sendWithProviderTemplate(body.getEmail(), EmailProviderTemplate.ACCOUNT_REQUEST);

      _aus.createUser(reqVars.get("email"), adminName, orgExternalId, Role.ADMIN);

      _crm.submitAccountRequestData(body);

      return new AccountResponse(org.getExternalId());
    } catch (IOException e) {
      throw new AccountRequestFailureException(e);
    }
  }
}
