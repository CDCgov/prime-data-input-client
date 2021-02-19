package gov.cdc.usds.simplereport.api.patientExperience;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static gov.cdc.usds.simplereport.api.Translators.parseSymptoms;
import static gov.cdc.usds.simplereport.api.Translators.parseEmail;
import static gov.cdc.usds.simplereport.api.Translators.parseEthnicity;
import static gov.cdc.usds.simplereport.api.Translators.parseGender;
import static gov.cdc.usds.simplereport.api.Translators.parsePhoneNumber;
import static gov.cdc.usds.simplereport.api.Translators.parseRace;
import static gov.cdc.usds.simplereport.api.Translators.parseState;
import static gov.cdc.usds.simplereport.api.Translators.parseString;

import gov.cdc.usds.simplereport.api.exceptions.FeatureFlagDisabledException;
import gov.cdc.usds.simplereport.api.exceptions.InvalidPatientLinkException;
import gov.cdc.usds.simplereport.api.model.AoEQuestions;
import gov.cdc.usds.simplereport.api.model.pxp.PxpApiWrapper;
import gov.cdc.usds.simplereport.api.model.pxp.PxpPersonWrapper;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.PatientLink;
import gov.cdc.usds.simplereport.db.model.Person;
import gov.cdc.usds.simplereport.db.model.TestEvent;
import gov.cdc.usds.simplereport.db.model.auxiliary.TestResult;
import gov.cdc.usds.simplereport.service.PatientLinkService;
import gov.cdc.usds.simplereport.service.PersonService;
import gov.cdc.usds.simplereport.service.TestEventService;
import gov.cdc.usds.simplereport.service.TestOrderService;

@RestController
@RequestMapping(value = "/pxp")
@Validated
public class PatientExperienceController {
  private static final Logger LOG = LoggerFactory.getLogger(PatientExperienceController.class);

  @Autowired
  private PersonService ps;

  @Autowired
  private PatientLinkService pls;

  @Autowired
  private TestOrderService tos;

  @Autowired
  private TestEventService tes;

  @Value("${feature-flags.patient-links:false}")
  private boolean patientLinksEnabled;

  /**
   * This endpoint is used to determine whether or not the patient link is still
   * valid and has not yet expired. It does not require birthdate verification
   */
  @GetMapping("/link/{plid}")
  public Organization getPatientLinkCurrent(@PathVariable("plid") String internalId) throws Exception {
    if (!patientLinksEnabled) {
      throw new FeatureFlagDisabledException("Patient links not enabled");
    }
    return pls.getPatientLinkCurrent(internalId);
  }

  /**
   * Verify that the patient-provided DOB matches the patient on file for the
   * patient link id. It returns the full patient object if so, otherwise it
   * throws an exception
   */
  @PutMapping("/link/verify")
  public PxpPersonWrapper getPatientLinkVerify(@RequestBody PxpApiWrapper<Void> body)
      throws InvalidPatientLinkException {
    if (!patientLinksEnabled) {
      throw new FeatureFlagDisabledException("Patient links not enabled");
    }
    Person p = pls.getPatientLinkVerify(body.getPlid(), body.getDob());
    TestEvent te = tes.getLastTestResultsForPatient(p);
    return new PxpPersonWrapper(p, te);
  }

  @PutMapping("/patient")
  public Person updatePatient(@RequestBody PxpApiWrapper<Person> body) throws InvalidPatientLinkException {
    if (!patientLinksEnabled) {
      throw new FeatureFlagDisabledException("Patient links not enabled");
    }
    pls.getPatientLinkVerify(body.getPlid(), body.getDob()); // gotta verify!

    PatientLink pl = pls.getPatientLink(body.getPlid());
    UUID facilityId = pl.getTestOrder().getFacility().getInternalId();
    String patientId = pls.getPatientLinkVerify(body.getPlid(), body.getDob()).getInternalId().toString();
    Person person = body.getData();

    return ps.updatePatient(facilityId, patientId, parseString(person.getLookupId()),
        parseString(person.getFirstName()), parseString(person.getMiddleName()), parseString(person.getLastName()),
        parseString(person.getSuffix()), person.getBirthDate(), parseString(person.getStreet()),
        parseString(person.getStreetTwo()), parseString(person.getCity()), parseState(person.getState()),
        parseString(person.getZipCode()), parsePhoneNumber(person.getTelephone()), person.getRole(),
        parseEmail(person.getEmail()), parseString(person.getCounty()), parseRace(person.getRace()),
        parseEthnicity(person.getEthnicity()), parseGender(person.getGender()), person.getResidentCongregateSetting(),
        person.getEmployedInHealthcare());
  }

  @PutMapping("/questions")
  public void patientLinkSubmit(@RequestBody PxpApiWrapper<AoEQuestions> body) throws InvalidPatientLinkException {
    if (!patientLinksEnabled) {
      throw new FeatureFlagDisabledException("Patient links not enabled");
    }
    Person patient = pls.getPatientLinkVerify(body.getPlid(), body.getDob());
    String patientID = patient.getInternalId().toString();

    AoEQuestions data = body.getData();
    Map<String, Boolean> symptomsMap = parseSymptoms(data.getSymptoms());

    tos.updateTimeOfTestQuestions(patientID, data.getPregnancy(), symptomsMap, data.isFirstTest(),
        data.getPriorTestDate(), data.getPriorTestType(),
        data.getPriorTestResult() == null ? null : TestResult.valueOf(data.getPriorTestResult()),
        data.getSymptomOnset(), data.getNoSymptoms());
  }
}