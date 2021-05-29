package gov.cdc.usds.simplereport.db.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gov.cdc.usds.simplereport.db.model.*;
import gov.cdc.usds.simplereport.db.model.auxiliary.PersonRole;
import gov.cdc.usds.simplereport.db.model.auxiliary.PhoneType;
import gov.cdc.usds.simplereport.db.model.auxiliary.TestResult;
import gov.cdc.usds.simplereport.test_util.TestDataFactory;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("checkstyle:MagicNumber")
class TestOrderRepositoryTest extends BaseRepositoryTest {

  @Autowired private TestOrderRepository _repo;
  @Autowired private PersonRepository _personRepo;
  @Autowired private PhoneNumberRepository _phoneRepo;
  @Autowired private OrganizationRepository _orgRepo;
  @Autowired private TestEventRepository _events;
  @Autowired private TestDataFactory _dataFactory;

  @Test
  void runChanges() {
    Organization gwu = _dataFactory.createValidOrg("George Washington", "gwu", true);
    Organization gtown = _dataFactory.createValidOrg("Georgetown", "gt", true);
    Facility site = _dataFactory.createValidFacility(gtown);
    Facility otherSite = _dataFactory.createValidFacility(gwu);
    Person hoya =
        _personRepo.save(
            new Person(
                gtown,
                "lookupId",
                "Joe",
                null,
                "Schmoe",
                null,
                LocalDate.now(),
                null,
                PersonRole.RESIDENT,
                "",
                null,
                "",
                null,
                "",
                false,
                false));
    PhoneNumber pn = new PhoneNumber(PhoneType.LANDLINE, "5555555555");
    pn.setPerson(hoya);
    _phoneRepo.save(pn);
    hoya.setPrimaryPhone(pn);
    hoya = _personRepo.save(hoya);
    TestOrder order = _repo.save(new TestOrder(hoya, site));
    List<TestOrder> queue = _repo.fetchQueue(gwu, otherSite);
    assertEquals(0, queue.size());
    queue = _repo.fetchQueue(gtown, site);
    assertEquals(1, queue.size());
    TestEvent event = _dataFactory.doTest(order, TestResult.NEGATIVE);
    flush();
    TestOrder lookuporder = _repo.findByTestEventId(order.getOrganization(), event.getInternalId());
    assertNotNull(lookuporder);
    assertEquals(lookuporder.getInternalId(), order.getInternalId());
    assertEquals(0, _repo.fetchQueue(gtown, site).size());
    assertEquals(1, _repo.fetchPastResults(gtown, site).size());
  }

  @Test
  void testLifeCycle() {
    DeviceType device = _dataFactory.getGenericDevice();
    Organization gtown = _dataFactory.createValidOrg("Georgetown", "gt", true);
    Person hoya =
        _personRepo.save(
            new Person(
                gtown,
                "lookupId",
                "Joe",
                null,
                "Schmoe",
                null,
                LocalDate.now(),
                null,
                PersonRole.RESIDENT,
                "",
                null,
                "",
                null,
                "",
                false,
                false));
    PhoneNumber pn = new PhoneNumber(PhoneType.LANDLINE, "5555555555");
    pn.setPerson(hoya);
    _phoneRepo.save(pn);
    hoya.setPrimaryPhone(pn);
    hoya = _personRepo.save(hoya);
    Facility site = _dataFactory.createValidFacility(gtown);
    TestOrder order = _repo.save(new TestOrder(hoya, site));
    assertNotNull(order);
    flush();
    TestEvent ev =
        _events.save(
            new TestEvent(TestResult.POSITIVE, site.getDefaultDeviceSpecimen(), hoya, site, order));
    assertNotNull(ev);
    order.setTestEventRef(ev);
    _repo.save(order);
    flush();

    assertNotNull(_repo.findByTestEventId(gtown, ev.getInternalId()));
    assertEquals(ev.getInternalId(), order.getTestEventId());

    // LocalDate.now() makes it random.
    String unitTestCorrectionStr = "Correction unit test: " + LocalDate.now().toString();
    order.setReasonForCorrection(unitTestCorrectionStr);
    _repo.save(order);
    flush();
    assertEquals(
        unitTestCorrectionStr,
        _repo.findByTestEventId(gtown, ev.getInternalId()).getReasonForCorrection());
  }

  @Test
  void createOrder_duplicatesFound_error() {
    Organization org = _dataFactory.createValidOrg();
    Person patient0 = _dataFactory.createMinimalPerson(org);
    Facility site = _dataFactory.createValidFacility(org);
    TestOrder order1 = new TestOrder(patient0, site);
    _repo.save(order1);
    flush();
    TestOrder order2 = new TestOrder(patient0, site);
    PersistenceException caught =
        assertThrows(
            PersistenceException.class,
            () -> {
              _repo.save(order2);
              flush();
            });
    assertEquals(ConstraintViolationException.class, caught.getCause().getClass());
  }

  @Test
  void createOrder_duplicateCanceled_ok() {
    Organization org = _dataFactory.createValidOrg();
    Person patient0 = _dataFactory.createMinimalPerson(org);
    Facility site = _dataFactory.createValidFacility(org);
    TestOrder order1 = new TestOrder(patient0, site);
    _repo.save(order1);
    flush();
    order1.cancelOrder();
    _repo.save(order1);
    flush();
    TestOrder order2 = new TestOrder(patient0, site);
    order2 = _repo.save(order2);
    flush();
    assertNotNull(order2.getInternalId());
    assertNotNull(order1.getInternalId());
    assertNotEquals(order1.getInternalId(), order2.getInternalId());
    List<TestOrder> queue = _repo.fetchQueue(org, site);
    assertEquals(1, queue.size());
    assertEquals(order2.getInternalId(), queue.get(0).getInternalId());
  }

  @Test
  void createOrder_duplicateSubmitted_ok() {
    Organization org = _dataFactory.createValidOrg();
    Person patient0 = _dataFactory.createMinimalPerson(org);
    Facility site = _dataFactory.createValidFacility(org);
    TestOrder order1 = new TestOrder(patient0, site);
    _repo.save(order1);
    flush();
    TestEvent didit =
        _events.save(
            new TestEvent(
                TestResult.NEGATIVE, site.getDefaultDeviceSpecimen(), patient0, site, order1));
    order1.setTestEventRef(didit);
    order1.setResult(didit.getResult());
    order1.markComplete();
    _repo.save(order1);
    flush();
    TestOrder order2 = new TestOrder(patient0, site);
    order2 = _repo.save(order2);
    flush();
    assertNotNull(order2.getInternalId());
    assertNotNull(order1.getInternalId());
    assertNotEquals(order1.getInternalId(), order2.getInternalId());
    List<TestOrder> queue = _repo.fetchQueue(org, site);
    assertEquals(1, queue.size());
    assertEquals(order2.getInternalId(), queue.get(0).getInternalId());
  }

  @Test
  void fetchQueue_multipleEntries_sortedFifo() {
    Organization org = _dataFactory.createValidOrg();
    Person adam = _dataFactory.createMinimalPerson(org, null, "Adam", "A.", "Astaire", "Jr.");
    Person brad = _dataFactory.createMinimalPerson(org, null, "Bradley", "B.", "Bones", null);
    Person charlie =
        _dataFactory.createMinimalPerson(org, null, "Charles", "C.", "Crankypants", "3rd");
    Facility facility = _dataFactory.createValidFacility(org);
    _repo.save(new TestOrder(charlie, facility));
    pause();
    _repo.save(new TestOrder(adam, facility));
    pause();
    _repo.save(new TestOrder(brad, facility));
    List<TestOrder> orders = _repo.fetchQueue(org, facility);
    assertEquals("Charles", orders.get(0).getPatient().getFirstName());
    assertEquals("Adam", orders.get(1).getPatient().getFirstName());
    assertEquals("Bradley", orders.get(2).getPatient().getFirstName());
  }
}
