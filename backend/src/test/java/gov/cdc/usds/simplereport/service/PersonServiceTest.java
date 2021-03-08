package gov.cdc.usds.simplereport.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import gov.cdc.usds.simplereport.db.model.Facility;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.Person;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonName;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonRole;
import gov.cdc.usds.simplereport.test_util.SliceTestConfiguration.WithSimpleReportEntryOnlyUser;
import gov.cdc.usds.simplereport.test_util.SliceTestConfiguration.WithSimpleReportOrgAdminUser;
import gov.cdc.usds.simplereport.test_util.SliceTestConfiguration.WithSimpleReportStandardUser;
import gov.cdc.usds.simplereport.test_util.TestDataFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("checkstyle:MagicNumber")
class PersonServiceTest extends BaseServiceTest<PersonService> {

  public static final int PATIENT_PAGEOFFSET = 0;
  public static final int PATIENT_PAGESIZE = 1000;

  // I'll have you know that I didn't actually mean to do this...
  private static final PersonName AMOS = new PersonName("Amos", null, "Quint", null);
  private static final PersonName BRAD = new PersonName("Bradley", "Z.", "Jones", "Jr.");
  private static final PersonName CHARLES = new PersonName("Charles", null, "Albemarle", "Sr.");
  private static final PersonName DEXTER = new PersonName("Dexter", null, "Jones", null);
  private static final PersonName ELIZABETH =
      new PersonName("Elizabeth", null, "Merriwether", null);
  private static final PersonName FRANK = new PersonName("Frank", null, "Bones", "3");

  // used for pagination
  private static final PersonName GALE = new PersonName("Gale", "S", "Vittorio", "PhD");
  private static final PersonName HEINRICK = new PersonName("Heinrick", "M", "Silver", "III");
  private static final PersonName IAN = new PersonName("Ian", "Brou", "Rutter", null);
  private static final PersonName JANNELLE = new PersonName("Jannelle", "T", "Cromack", null);
  private static final PersonName KACEY = new PersonName("Kacey", "G", "Marthe", null);
  private static final PersonName LEELOO = new PersonName("Leeloo", "Dallas", "Multipass", null);

  @Autowired private OrganizationService _orgService;
  @Autowired private TestDataFactory _dataFactory;

  private Organization _org;
  private Facility _site1;
  private Facility _site2;

  @BeforeEach
  void setupData() {
    initSampleData();
  }

  @Test
  @WithSimpleReportStandardUser
  void roundTrip() {
    _service.addPatient(
        null,
        "FOO",
        "Fred",
        null,
        "Fosbury",
        "Sr.",
        LocalDate.of(1865, 12, 25),
        _dataFactory.getAddress(),
        "5555555555",
        PersonRole.STAFF,
        null,
        null,
        null,
        null,
        false,
        false);
    _service.addPatient(
        null,
        "BAR",
        "Basil",
        null,
        "Barnacle",
        "4th",
        LocalDate.of(1865, 12, 25),
        _dataFactory.getAddress(),
        "5555555555",
        PersonRole.STAFF,
        null,
        null,
        null,
        null,
        false,
        false);
    List<Person> all = _service.getAllPatients(PATIENT_PAGEOFFSET, PATIENT_PAGESIZE);
    assertEquals(2, all.size());
  }

  @Test
  @WithSimpleReportStandardUser
  void deletePatient_standardUser_error() {
    Facility fac =
        _dataFactory.createValidFacility(_orgService.getCurrentOrganization(), "First One");
    UUID facilityId = fac.getInternalId();

    Person p =
        _service.addPatient(
            facilityId,
            "FOO",
            "Fred",
            null,
            "Fosbury",
            "Sr.",
            LocalDate.of(1865, 12, 25),
            _dataFactory.getAddress(),
            "5555555555",
            PersonRole.STAFF,
            null,
            null,
            null,
            null,
            false,
            false);

    assertSecurityError(() -> _service.setIsDeleted(p.getInternalId(), true));
    assertEquals(
        "Fred",
        _service.getAllPatients(PATIENT_PAGEOFFSET, PATIENT_PAGESIZE).get(0).getFirstName());
  }

  @Test
  @WithSimpleReportStandardUser
  void accessArchievedPatient_standardUser_error() {
    Facility fac =
        _dataFactory.createValidFacility(_orgService.getCurrentOrganization(), "First One");
    UUID facilityId = fac.getInternalId();

    Person p =
        _service.addPatient(
            facilityId,
            "FOO",
            "Fred",
            null,
            "Fosbury",
            "Sr.",
            LocalDate.of(1865, 12, 25),
            _dataFactory.getAddress(),
            "5555555555",
            PersonRole.STAFF,
            null,
            null,
            null,
            null,
            false,
            false);

    assertSecurityError(
        () -> _service.getAllArchivedPatients(PATIENT_PAGEOFFSET, PATIENT_PAGESIZE));
    assertSecurityError(
        () -> _service.getArchivedPatients(facilityId, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE));
  }

  @Test
  @WithSimpleReportOrgAdminUser
  void deletePatient_adminUser_success() {
    Facility fac =
        _dataFactory.createValidFacility(_orgService.getCurrentOrganization(), "First One");
    UUID facilityId = fac.getInternalId();

    Person p =
        _service.addPatient(
            facilityId,
            "FOO",
            "Fred",
            null,
            "Fosbury",
            "Sr.",
            LocalDate.of(1865, 12, 25),
            _dataFactory.getAddress(),
            "5555555555",
            PersonRole.STAFF,
            null,
            null,
            null,
            null,
            false,
            false);

    assertEquals(1, _service.getAllPatients(PATIENT_PAGEOFFSET, PATIENT_PAGESIZE).size());
    Person deletedPerson = _service.setIsDeleted(p.getInternalId(), true);

    assertTrue(deletedPerson.isDeleted());
    assertEquals(0, _service.getAllPatients(PATIENT_PAGEOFFSET, PATIENT_PAGESIZE).size());
    assertEquals(0, _service.getPatients(facilityId, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE).size());

    List<Person> result = _service.getAllArchivedPatients(PATIENT_PAGEOFFSET, PATIENT_PAGESIZE);
    assertEquals(1, result.size());
    assertTrue(result.get(0).isDeleted());
    assertEquals(
        1, _service.getArchivedPatients(facilityId, PATIENT_PAGEOFFSET, PATIENT_PAGESIZE).size());
  }

  @Test
  @WithSimpleReportOrgAdminUser
  void getPatients_noFacility_allFetchedAndSorted() {
    makedata(false);
    // gets all patients across the org
    List<Person> patients = _service.getAllPatients(PATIENT_PAGEOFFSET, PATIENT_PAGESIZE);
    assertPatientList(patients, CHARLES, FRANK, BRAD, DEXTER, ELIZABETH, AMOS);
  }

  @Test
  @WithSimpleReportOrgAdminUser
  void getPatients_facilitySpecific_nullsAndSpecifiedFetchedAndSorted() {
    makedata(false);
    List<Person> patients =
        _service.getPatients(_site1.getInternalId(), PATIENT_PAGEOFFSET, PATIENT_PAGESIZE);
    assertPatientList(patients, CHARLES, BRAD, ELIZABETH, AMOS);
    patients = _service.getPatients(_site2.getInternalId(), PATIENT_PAGEOFFSET, PATIENT_PAGESIZE);
    assertPatientList(patients, FRANK, BRAD, DEXTER, AMOS);
  }

  @Test
  @WithSimpleReportOrgAdminUser
  void getPatients_pagination() {
    makedata(true);
    List<Person> patients_org_page0 = _service.getAllPatients(0, 5);
    List<Person> patients_org_page1 = _service.getAllPatients(1, 5);
    List<Person> patients_org_page2 = _service.getAllPatients(2, 5);
    List<Person> patients_org_page3 = _service.getAllPatients(3, 5);

    assertPatientList(patients_org_page0, CHARLES, FRANK, JANNELLE, BRAD, DEXTER);
    assertPatientList(patients_org_page1, KACEY, ELIZABETH, LEELOO, AMOS, IAN);
    assertPatientList(patients_org_page2, HEINRICK, GALE);
    assertEquals(0, patients_org_page3.size());

    List<Person> patients_site2_page0 = _service.getPatients(_site2.getInternalId(), 0, 4);
    List<Person> patients_site2_page1 = _service.getPatients(_site2.getInternalId(), 1, 4);
    List<Person> patients_site2_page2 = _service.getPatients(_site2.getInternalId(), 2, 4);

    assertPatientList(patients_site2_page0, FRANK, JANNELLE, BRAD, DEXTER);
    assertPatientList(patients_site2_page1, KACEY, LEELOO, AMOS);
    assertEquals(0, patients_site2_page2.size());
  }

  @Test
  @WithSimpleReportOrgAdminUser
  void getPatients_counts() {
    makedata(true);

    List<Person> patients_org_page0 = _service.getAllPatients(0, 100);
    assertEquals(patients_org_page0.size(), _service.getAllPatientsCount());
    assertEquals(12, _service.getAllPatientsCount());
    // count includes patients for site2 AND facilityId=null
    assertEquals(7, _service.getPatientsCount(_site2.getInternalId()));

    // delete a couple, verify counts
    List<Person> patients_site2 = _service.getPatients(_site2.getInternalId(), 0, 100);

    // delete Charles (_site1)
    _service.setIsDeleted(patients_org_page0.get(0).getInternalId(), true);
    // Delete Frank (_site2)
    _service.setIsDeleted(patients_site2.get(0).getInternalId(), true);

    assertEquals(10, _service.getAllPatientsCount());
    assertEquals(6, _service.getPatientsCount(_site2.getInternalId()));
    assertEquals(2, _service.getAllArchivedPatientsCount());
    assertEquals(1, _service.getArchivedPatientsCount(_site2.getInternalId()));
  }

  @Test
  @WithSimpleReportEntryOnlyUser
  void addPatient_entryOnlyUser_error() {
    assertSecurityError(
        () ->
            _service.addPatient(
                null,
                null,
                "Fred",
                null,
                "Flintstone",
                "Jr.",
                LocalDate.of(1950, 1, 1),
                _dataFactory.getAddress(),
                null,
                PersonRole.RESIDENT,
                null,
                null,
                null,
                null,
                false,
                false));
  }

  private void makedata(boolean extraPatients) {
    _org = _orgService.getCurrentOrganization();
    _site1 = _dataFactory.createValidFacility(_org, "First One");
    _site2 = _dataFactory.createValidFacility(_org, "Second One");

    // patients without a facility appear in ALL of the Org's facilities
    _dataFactory.createMinimalPerson(_org, null, AMOS);
    _dataFactory.createMinimalPerson(_org, null, BRAD);

    _dataFactory.createMinimalPerson(_org, _site1, ELIZABETH);
    _dataFactory.createMinimalPerson(_org, _site1, CHARLES);
    _dataFactory.createMinimalPerson(_org, _site2, DEXTER);
    _dataFactory.createMinimalPerson(_org, _site2, FRANK);
    if (extraPatients) {
      _dataFactory.createMinimalPerson(_org, _site1, GALE);
      _dataFactory.createMinimalPerson(_org, _site1, HEINRICK);
      _dataFactory.createMinimalPerson(_org, _site1, IAN);
      _dataFactory.createMinimalPerson(_org, _site2, JANNELLE);
      _dataFactory.createMinimalPerson(_org, _site2, KACEY);
      _dataFactory.createMinimalPerson(_org, _site2, LEELOO);
    }
  }

  private static void assertPatientList(List<Person> found, PersonName... expected) {
    // check common elements first
    for (int i = 0; i < expected.length && i < found.size(); i++) {
      assertEquals(expected[i], found.get(i).getNameInfo());
    }
    // *then* check if there are extras
    if (expected.length != found.size()) {
      fail("Expected" + expected.length + " items but found " + found.size());
    }
  }
}
