package gov.cdc.usds.simplereport.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sendgrid.helpers.mail.Mail;
import gov.cdc.usds.simplereport.api.accountrequest.AccountRequestController;
import gov.cdc.usds.simplereport.api.accountrequest.errors.AccountRequestFailureException;
import gov.cdc.usds.simplereport.api.model.Role;
import gov.cdc.usds.simplereport.api.model.TemplateVariablesProvider;
import gov.cdc.usds.simplereport.api.model.accountrequest.AccountRequest;
import gov.cdc.usds.simplereport.api.pxp.CurrentPatientContextHolder;
import gov.cdc.usds.simplereport.config.TemplateConfiguration;
import gov.cdc.usds.simplereport.config.WebConfiguration;
import gov.cdc.usds.simplereport.config.authorization.TenantDataAuthenticationProvider;
import gov.cdc.usds.simplereport.db.model.ApiUser;
import gov.cdc.usds.simplereport.db.model.DeviceType;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonName;
import gov.cdc.usds.simplereport.db.model.auxiliary.StreetAddress;
import gov.cdc.usds.simplereport.db.repository.ApiUserRepository;
import gov.cdc.usds.simplereport.idp.repository.OktaRepository;
import gov.cdc.usds.simplereport.logging.AuditLoggingAdvice;
import gov.cdc.usds.simplereport.service.AddressValidationService;
import gov.cdc.usds.simplereport.service.ApiUserService;
import gov.cdc.usds.simplereport.service.AuthorizationService;
import gov.cdc.usds.simplereport.service.DeviceTypeService;
import gov.cdc.usds.simplereport.service.OrganizationService;
import gov.cdc.usds.simplereport.service.TenantDataAccessService;
import gov.cdc.usds.simplereport.service.crm.CrmService;
import gov.cdc.usds.simplereport.service.email.EmailProvider;
import gov.cdc.usds.simplereport.service.email.EmailProviderTemplate;
import gov.cdc.usds.simplereport.service.email.EmailService;
import gov.cdc.usds.simplereport.service.model.DeviceSpecimenTypeHolder;
import gov.cdc.usds.simplereport.service.model.IdentityAttributes;
import gov.cdc.usds.simplereport.service.model.IdentitySupplier;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.thymeleaf.spring5.SpringTemplateEngine;

@WebMvcTest(
    controllers = AccountRequestController.class,
    includeFilters =
        @Filter(
            classes = {TemplateConfiguration.class},
            type = FilterType.ASSIGNABLE_TYPE),
    excludeFilters =
        @Filter(
            classes = {AuditLoggingAdvice.class, WebConfiguration.class},
            type = FilterType.ASSIGNABLE_TYPE))
class AccountRequestControllerTest {

  @Autowired private MockMvc _mockMvc;

  @Autowired
  @Qualifier("simpleReportTemplateEngine")
  SpringTemplateEngine _templateEngine;

  // Mocked dependencies of ApiUserService
  @MockBean private ApiUserRepository apiUserRepository;
  @MockBean private AuthorizationService authorizationService;
  @MockBean private IdentitySupplier identitySupplier;
  @MockBean private CurrentPatientContextHolder currentPatientContextHolder;
  @MockBean private TenantDataAccessService tenantDataAccessService;

  @MockBean private OrganizationService orgService;
  @MockBean private DeviceTypeService deviceTypeService;
  @MockBean private AddressValidationService addressValidationService;
  @SpyBean private ApiUserService apiUserService;
  @MockBean private CurrentAccountRequestContextHolder contextHolder;
  @MockBean private CurrentTenantDataAccessContextHolder tenantDataAccessContextHolder;
  @MockBean private TenantDataAuthenticationProvider tenantDataAuthProvider;

  @MockBean private CrmService crmService;
  @MockBean private OktaRepository oktaRepository;

  @MockBean private EmailProvider mockSendGrid;
  @SpyBean private EmailService emailService;

  @Captor private ArgumentCaptor<TemplateVariablesProvider> contentCaptor;
  @Captor private ArgumentCaptor<Mail> mail;
  @Captor private ArgumentCaptor<String> externalIdCaptor;
  @Captor private ArgumentCaptor<PersonName> nameCaptor;
  @Captor private ArgumentCaptor<StreetAddress> addressCaptor;
  @Captor private ArgumentCaptor<AccountRequest> accountRequestCaptor;

  private static final String FAKE_ORG_EXTERNAL_ID_PREFIX = "RI-Day-Hayes-Trading-";
  private static final String FAKE_ORG_EXTERNAL_ID =
      FAKE_ORG_EXTERNAL_ID_PREFIX + "09e05f77-8765-48bb-adcb-96819af7aa32";

  @Test
  void waitlistIsOk() throws Exception {
    String requestBody =
        "{\"name\":\"Angela Chan\",\"email\":\"qasas@mailinator.com\",\"phone\":\"+1 (157) 294-1842\",\"state\":\"Exercitation odit pr\",\"organization\":\"Lane Moss LLC\",\"referral\":\"Ea error voluptate v\"}";

    MockHttpServletRequestBuilder builder =
        post(ResourceLinks.WAITLIST_REQUEST)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(requestBody);

    this._mockMvc.perform(builder).andExpect(status().isOk());

    verify(emailService)
        .send(
            eq(List.of("support@simplereport.gov")),
            eq("New waitlist request"),
            contentCaptor.capture());
    assertThat(contentCaptor.getValue().getTemplateName()).isEqualTo("waitlist-request");
    assertThat(contentCaptor.getValue().toTemplateVariables()).containsEntry("name", "Angela Chan");

    verify(mockSendGrid, times(1)).send(mail.capture());
    assertThat(mail.getValue().getContent().get(0).getValue())
        .contains(
            "new SimpleReport waitlist request",
            "Angela Chan",
            "qasas@mailinator.com",
            "Exercitation odit pr");
  }

  @Test
  void waitlistValidatesInput() throws Exception {
    String requestBody =
        "{\"name\":\"Angela Chan\",\"phone\":\"+1 (157) 294-1842\",\"state\":\"Exercitation odit pr\",\"organization\":\"Lane Moss LLC\",\"referral\":\"Ea error voluptate v\"}";

    MockHttpServletRequestBuilder builder =
        post(ResourceLinks.WAITLIST_REQUEST)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(requestBody);

    this._mockMvc.perform(builder).andExpect(status().isBadRequest());
    verifyNoInteractions(emailService);
  }

  @Test
  void accountRequestIsOk() throws Exception {
    // also need to add default devices and other
    String requestBody =
        "{\"first-name\":\"Mary\",\"last-name\":\"Lopez\",\"email\":\"kyvuzoxy@mailinator.com\",\"work-phone-number\":\"+1 (969) 768-2863\",\"cell-phone-number\":\"+1 (319) 682-3114\",\"street-address1\":\"707 White Milton Extension\",\"street-address2\":\"Apt 3\",\"city\":\"Reprehenderit nostr\",\"state\":\"RI\",\"zip\":\"13046\",\"county\":\"Et consectetur sunt\",\"organization-name\":\"Day Hayes Trading\",\"organization-type\":\"Homeless Shelter\",\"facility-name\":\"Fiona Payne\",\"facility-phone-number\":\"800-888-8888\",\"clia-number\":\"474\",\"workflow\":\"Aut ipsum aute aute\",\"op-first-name\":\"Sawyer\",\"op-last-name\":\"Sears\",\"npi\":\"Quis sit eiusmod Nam\",\"op-phone-number\":\"+1 (583) 883-4172\",\"op-street-address1\":\"290 East Rocky Second Street\",\"op-street-address2\":\"UNAVAILABLE\",\"op-city\":\"Dicta cumque sit ip\",\"op-state\":\"AR\",\"op-zip\":\"43675\",\"op-county\":\"Asperiores illum in\",\"records-test-results\":\"No\",\"process-time\":\"15–30 minutes\",\"submitting-results-time\":\"Less than 30 minutes\",\"browsers\":\"Other\",\"testing-devices\":\"Abbott IDNow, BD Veritor, Cue, LumiraDX\",\"default-testing-device\":\"LumiraDX\",\"access-devices\":\"Smartphone\"}";

    Organization organization = mock(Organization.class);
    ApiUser apiUser = mock(ApiUser.class);
    ApiUser acctRequestApiUser = mock(ApiUser.class);
    DeviceType device1 = mock(DeviceType.class);
    DeviceType device2 = mock(DeviceType.class);
    DeviceType device3 = mock(DeviceType.class);
    DeviceType device4 = mock(DeviceType.class);
    UUID deviceUuid1 = UUID.randomUUID();
    UUID deviceUuid2 = UUID.randomUUID();
    UUID deviceUuid3 = UUID.randomUUID();
    UUID deviceUuid4 = UUID.randomUUID();
    UUID acctRequestApiUserUuid = UUID.randomUUID();
    when(orgService.createOrganization(
            any(),
            any(),
            startsWith(FAKE_ORG_EXTERNAL_ID_PREFIX),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any()))
        .thenReturn(organization);
    when(orgService.getOrganization(any())).thenReturn(organization);
    when(organization.getExternalId()).thenReturn(FAKE_ORG_EXTERNAL_ID);
    when(apiUserRepository.save(any())).thenReturn(apiUser);
    when(apiUserRepository.findByLoginEmail("account-request-noreply@simplereport.gov"))
        .thenReturn(Optional.of(acctRequestApiUser));
    when(acctRequestApiUser.getInternalId()).thenReturn(acctRequestApiUserUuid);
    when(contextHolder.isAccountRequest()).thenReturn(true);
    when(device1.getName()).thenReturn("Abbott IDNow");
    when(device1.getModel()).thenReturn("ID Now");
    when(device1.getInternalId()).thenReturn(deviceUuid1);
    when(device2.getName()).thenReturn("BD Veritor");
    when(device2.getModel()).thenReturn("BD Veritor System for Rapid Detection of SARS-CoV-2*");
    when(device2.getInternalId()).thenReturn(deviceUuid2);
    when(device3.getName()).thenReturn("Cue");
    when(device3.getModel()).thenReturn("CueTM COVID-19 Test*");
    when(device3.getInternalId()).thenReturn(deviceUuid3);
    when(device4.getName()).thenReturn("LumiraDX");
    when(device4.getModel()).thenReturn("LumiraDx SARS-CoV-2 Ag Test*");
    when(device4.getInternalId()).thenReturn(deviceUuid4);
    when(deviceTypeService.fetchDeviceTypes())
        .thenReturn(List.of(device1, device2, device3, device4));

    DeviceSpecimenTypeHolder ds = mock(DeviceSpecimenTypeHolder.class);
    when(deviceTypeService.getTypesForFacility(
            eq(deviceUuid4.toString()),
            eq(
                List.of(
                    deviceUuid1.toString(),
                    deviceUuid2.toString(),
                    deviceUuid3.toString(),
                    deviceUuid4.toString()))))
        .thenReturn(ds);

    StreetAddress facilityAddress = mock(StreetAddress.class);
    when(addressValidationService.getValidatedAddress(
            "707 White Milton Extension",
            "Apt 3",
            "Reprehenderit nostr",
            "RI",
            "13046",
            "facility"))
        .thenReturn(facilityAddress);

    MockHttpServletRequestBuilder builder =
        post(ResourceLinks.ACCOUNT_REQUEST)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(requestBody);

    this._mockMvc.perform(builder).andExpect(status().isOk());
    //        .andExpect(
    //            jsonPath("$.orgExternalId", org.hamcrest.Matchers.equalTo(FAKE_ORG_EXTERNAL_ID)));

    // mail 1: to us (contains formatted request data)
    verify(emailService, times(1))
        .send(
            eq(List.of("support@simplereport.gov")),
            eq("New account request"),
            contentCaptor.capture());
    assertThat(contentCaptor.getValue().getTemplateName()).isEqualTo("account-request");
    assertThat(contentCaptor.getValue().toTemplateVariables()).containsEntry("firstName", "Mary");

    // mail 2: to requester (simplereport new user email)
    verify(emailService, times(1))
        .sendWithProviderTemplate("kyvuzoxy@mailinator.com", EmailProviderTemplate.ACCOUNT_REQUEST);

    verify(mockSendGrid, times(2)).send(mail.capture());
    List<Mail> sentMails = mail.getAllValues();

    // mail 1: to us (contains formatted request data)
    assertThat(sentMails.get(0).getContent().get(0).getValue())
        .contains(
            "new SimpleReport account request",
            "Mary",
            "Lopez",
            "kyvuzoxy@mailinator.com",
            "Reprehenderit nostr");
    assertNull(sentMails.get(0).getAttachments());
    assertNull(sentMails.get(0).getTemplateId());

    // mail 2: to requester (simplereport new user email)
    assertThat(sentMails.get(1).getPersonalization().get(0).getTos().get(0).getEmail())
        .isEqualTo("kyvuzoxy@mailinator.com");
    assertNotNull(sentMails.get(1).getTemplateId());

    verify(deviceTypeService, times(1))
        .getTypesForFacility(
            deviceUuid4.toString(),
            List.of(
                deviceUuid1.toString(),
                deviceUuid2.toString(),
                deviceUuid3.toString(),
                deviceUuid4.toString()));

    verify(addressValidationService, times(1))
        .getValidatedAddress(
            "707 White Milton Extension",
            "Apt 3",
            "Reprehenderit nostr",
            "RI",
            "13046",
            "facility");

    verify(apiUserService, times(1))
        .createUser(
            eq("kyvuzoxy@mailinator.com"),
            nameCaptor.capture(),
            externalIdCaptor.capture(),
            eq(Role.ADMIN));

    assertThat(nameCaptor.getValue().getFirstName()).isEqualTo("Mary");
    assertNull(nameCaptor.getValue().getMiddleName());
    assertThat(nameCaptor.getValue().getLastName()).isEqualTo("Lopez");
    assertNull(nameCaptor.getValue().getSuffix());
    assertThat(externalIdCaptor.getValue()).isEqualTo(FAKE_ORG_EXTERNAL_ID);

    verify(orgService, times(1))
        .createOrganization(
            eq("Day Hayes Trading"),
            eq("shelter"),
            externalIdCaptor.capture(),
            eq("Fiona Payne"),
            eq("474"),
            eq(facilityAddress),
            eq("(800) 888-8888"),
            eq(null),
            eq(ds),
            nameCaptor.capture(),
            addressCaptor.capture(),
            eq("(583) 883-4172"),
            eq("Quis sit eiusmod Nam"));

    assertThat(externalIdCaptor.getValue()).startsWith("RI-Day-Hayes-Trading-");
    assertThat(nameCaptor.getValue().getFirstName()).isEqualTo("Sawyer");
    assertNull(nameCaptor.getValue().getMiddleName());
    assertThat(nameCaptor.getValue().getLastName()).isEqualTo("Sears");
    assertNull(nameCaptor.getValue().getSuffix());
    assertThat(addressCaptor.getValue().getStreetOne()).isEqualTo("290 East Rocky Second Street");
    assertThat(addressCaptor.getValue().getStreetTwo()).isEqualTo("UNAVAILABLE");
    assertThat(addressCaptor.getValue().getCity()).isEqualTo("Dicta cumque sit ip");
    assertThat(addressCaptor.getValue().getState()).isEqualTo("AR");
    assertThat(addressCaptor.getValue().getPostalCode()).isEqualTo("43675");
    assertThat(addressCaptor.getValue().getCounty()).isEqualTo("Asperiores illum in");

    // make sure we passed the data along to our CRM
    verify(crmService, times(1)).submitAccountRequestData(accountRequestCaptor.capture());
    assertThat(accountRequestCaptor.getValue().getOrganizationName())
        .isEqualTo("Day Hayes Trading");

    // new user should be disabled in okta
    verify(oktaRepository)
        .createUser(any(IdentityAttributes.class), eq(organization), anySet(), anySet(), eq(false));
  }

  @Test
  void accountRequestValidatesInput() throws Exception {
    String requestBody =
        "{\"first-name\":\"Mary\",\"last-name\":\"Lopez\",\"work-phone-number\":\"+1 (969) 768-2863\",\"cell-phone-number\":\"+1 (319) 682-3114\",\"street-address1\":\"707 White Milton Extension\",\"apt-suite-other\":\"FL\",\"apt-floor-suite-no\":\"694\",\"city\":\"Reprehenderit nostr\",\"state\":\"RI\",\"zip\":\"13046\",\"county\":\"Et consectetur sunt\",\"organization-name\":\"Day Hayes Trading\",\"organization-type\":\"Homeless Shelter\",\"facility-name\":\"Fiona Payne\",\"facility-phone-number\":\"800-888-8888\",\"clia-number\":\"474\",\"workflow\":\"Aut ipsum aute aute\",\"op-first-name\":\"Sawyer\",\"op-last-name\":\"Sears\",\"npi\":\"Quis sit eiusmod Nam\",\"op-phone-number\":\"+1 (583) 883-4172\",\"op-street-address1\":\"290 East Rocky Second Street\",\"op-apt-suite-other\":\"UNAVAILABLE\",\"op-apt-floor-suite-no\":\"546\",\"op-city\":\"Dicta cumque sit ip\",\"op-state\":\"AR\",\"op-zip\":\"43675\",\"op-county\":\"Asperiores illum in\",\"records-test-results\":\"No\",\"process-time\":\"15–30 minutes\",\"submitting-results-time\":\"Less than 30 minutes\",\"browsers\":\"Other\",\"testing-devices\":\"Abbott IDNow, BD Veritor, LumiraDX\",\"default-testing-device\":\"LumiraDX\",\"access-devices\":\"Smartphone\"}";

    MockHttpServletRequestBuilder builder =
        post(ResourceLinks.ACCOUNT_REQUEST)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(requestBody);

    this._mockMvc.perform(builder).andExpect(status().isBadRequest());
    verifyNoInteractions(emailService);
    verifyNoInteractions(deviceTypeService);
    verifyNoInteractions(addressValidationService);
    verifyNoInteractions(orgService);
    verifyNoInteractions(apiUserService);
  }

  @Test
  void accountRequestSubmittedDeviceNotRegistered() throws Exception {
    String requestBody =
        "{\"first-name\":\"Mary\",\"last-name\":\"Lopez\",\"email\":\"kyvuzoxy@mailinator.com\",\"work-phone-number\":\"+1 (969) 768-2863\",\"cell-phone-number\":\"+1 (319) 682-3114\",\"street-address1\":\"707 White Milton Extension\",\"street-address2\":\"Apt 3\",\"city\":\"Reprehenderit nostr\",\"state\":\"RI\",\"zip\":\"13046\",\"county\":\"Et consectetur sunt\",\"organization-name\":\"Day Hayes Trading\",\"organization-type\":\"Homeless Shelter\",\"facility-name\":\"Fiona Payne\",\"facility-phone-number\":\"800-888-8888\",\"clia-number\":\"474\",\"workflow\":\"Aut ipsum aute aute\",\"op-first-name\":\"Sawyer\",\"op-last-name\":\"Sears\",\"npi\":\"Quis sit eiusmod Nam\",\"op-phone-number\":\"+1 (583) 883-4172\",\"op-street-address1\":\"290 East Rocky Second Street\",\"op-street-address2\":\"UNAVAILABLE\",\"op-city\":\"Dicta cumque sit ip\",\"op-state\":\"AR\",\"op-zip\":\"43675\",\"op-county\":\"Asperiores illum in\",\"records-test-results\":\"No\",\"process-time\":\"15–30 minutes\",\"submitting-results-time\":\"Less than 30 minutes\",\"browsers\":\"Other\",\"testing-devices\":\"Invalid Device\",\"default-testing-device\":\"LumiraDX\",\"access-devices\":\"Smartphone\"}";

    DeviceType device1 = mock(DeviceType.class);
    UUID deviceUuid1 = UUID.randomUUID();
    when(device1.getName()).thenReturn("Abbott IDNow");
    when(device1.getModel()).thenReturn("ID Now");
    when(device1.getInternalId()).thenReturn(deviceUuid1);
    when(deviceTypeService.fetchDeviceTypes()).thenReturn(List.of(device1));

    MockHttpServletRequestBuilder builder =
        post(ResourceLinks.ACCOUNT_REQUEST)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(requestBody);

    this._mockMvc
        .perform(builder)
        .andExpect(status().isInternalServerError())
        .andExpect(
            result ->
                assertTrue(
                    result.getResolvedException() instanceof AccountRequestFailureException));
  }
}
