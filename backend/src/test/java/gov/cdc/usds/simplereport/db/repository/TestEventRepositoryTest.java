package gov.cdc.usds.simplereport.db.repository;

import gov.cdc.usds.simplereport.db.model.Facility;
import gov.cdc.usds.simplereport.db.model.Organization;
import gov.cdc.usds.simplereport.db.model.Person;
import gov.cdc.usds.simplereport.db.model.TestEvent;
import gov.cdc.usds.simplereport.db.model.TestOrder;
import gov.cdc.usds.simplereport.db.model.auxiliary.AskOnEntrySurvey;
import gov.cdc.usds.simplereport.db.model.auxiliary.TestCorrectionStatus;
import gov.cdc.usds.simplereport.db.model.auxiliary.TestResult;
import gov.cdc.usds.simplereport.test_util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestEventRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEventRepository _repo;
    @Autowired
    private TestDataFactory _dataFactory;

    @Test
    public void testFindByPatient() {
        Organization org = _dataFactory.createValidOrg();
        Facility place = _dataFactory.createValidFacility(org);
        Person patient = _dataFactory.createMinimalPerson(org);
        TestOrder order = _dataFactory.createTestOrder(patient, place);
        _repo.save(new TestEvent(TestResult.POSITIVE, place.getDefaultDeviceType(), patient, place, order));
        _repo.save(new TestEvent(TestResult.UNDETERMINED, place.getDefaultDeviceType(), patient, place, order));
        flush();
        List<TestEvent> found = _repo.findAllByPatient(patient);
        assertEquals(2, found.size());
    }

    @Test
    public void testLatestTestEventForPerson() {
        Date d1 = Date.from(Instant.parse("2000-01-01T00:00:00Z"));
        final Date DATE_1MIN_FUTURE = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(3));
        List<TestEvent> foundTestReports1 = _repo.queryMatchAllBetweenDates(d1, DATE_1MIN_FUTURE);
        Organization org = _dataFactory.createValidOrg();
        Facility place = _dataFactory.createValidFacility(org);
        Person patient = _dataFactory.createMinimalPerson(org);
        TestOrder order = _dataFactory.createTestOrder(patient, place);
        TestEvent first = new TestEvent(TestResult.POSITIVE, place.getDefaultDeviceType(), patient, place, order);
        TestEvent second = new TestEvent(TestResult.UNDETERMINED, place.getDefaultDeviceType(), patient, place, order);
        _repo.save(first);
        _repo.save(second);
        flush();
        TestEvent found = _repo.findFirst1ByPatientOrderByCreatedAtDesc(patient);
        assertEquals(second.getResult(), TestResult.UNDETERMINED);
        List<TestEvent> foundTestReports2 = _repo.queryMatchAllBetweenDates(d1, DATE_1MIN_FUTURE);
        assertEquals(2, foundTestReports2.size() - foundTestReports1.size());

        testTestEventUnitTests(order, first);  // just leverage existing order, event to test on newer columns
    }

    @Test
    public void fetchResults_multipleEntries_sortedLifo() throws InterruptedException {
        Organization org = _dataFactory.createValidOrg();
        Person adam = _dataFactory.createMinimalPerson(org, null, "Adam", "A.", "Astaire", "Jr.");
        Person brad = _dataFactory.createMinimalPerson(org, null, "Bradley", "B.", "Bones", null);
        Person charlie = _dataFactory.createMinimalPerson(org, null, "Charles", "C.", "Crankypants", "3rd");
        Facility facility = _dataFactory.createValidFacility(org);

        
        TestOrder charlieOrder = _dataFactory.createTestOrder(charlie, facility);
        pause();
        TestOrder adamOrder = _dataFactory.createTestOrder(adam, facility);
        pause();
        TestOrder bradleyOrder = _dataFactory.createTestOrder(brad, facility);

        List<TestEvent> results = _repo.getTestEventResults(facility.getInternalId(), new Date(0));
        assertEquals(0, results.size());

        _dataFactory.doTest(bradleyOrder, TestResult.NEGATIVE);
        pause();
        _dataFactory.doTest(charlieOrder, TestResult.POSITIVE);
        pause();
        _dataFactory.doTest(adamOrder, TestResult.UNDETERMINED);

        results = _repo.getTestEventResults(facility.getInternalId(), new Date(0));
        assertEquals(3, results.size());
        assertEquals("Adam", results.get(0).getPatient().getFirstName());
        assertEquals("Charles", results.get(1).getPatient().getFirstName());
        assertEquals("Bradley", results.get(2).getPatient().getFirstName());
    }

    private void compareAskOnEntrySurvey(AskOnEntrySurvey a1, AskOnEntrySurvey a2) {
        assertEquals(a1.getFirstTest(), a2.getFirstTest());
        assertEquals(a1.getNoSymptoms(), a2.getNoSymptoms());
        assertEquals(a1.getFirstTest(), a2.getFirstTest());
        assertEquals(a1.getPregnancy(), a2.getPregnancy());
        assertEquals(a1.getPriorTestDate(), a2.getPriorTestDate());
        assertEquals(a1.getPriorTestResult(), a2.getPriorTestResult());
        assertEquals(a1.getPriorTestType(), a2.getPriorTestType());
        assertEquals(a1.getSymptomOnsetDate(), a2.getSymptomOnsetDate());
    }

    private void testTestEventUnitTests(TestOrder startingOrder, TestEvent startingEvent) {
        assertEquals(startingOrder.getInternalId(), startingEvent.getTestOrderId());
        assertEquals(TestCorrectionStatus.ORIGINAL ,startingEvent.getCorrectionStatus());
        assertNull(startingEvent.getPriorCorrectedTestEventId());
        assertNotNull(startingOrder.getAskOnEntrySurvey().getSurvey());
        assertNotNull(startingEvent.getPatientData());

        compareAskOnEntrySurvey(startingOrder.getAskOnEntrySurvey().getSurvey(), startingEvent.getSurveyData());

        // repo level test. Higher level tests done in TestOrderServiceTest
        String reason = "Unit Test Correction " + LocalDateTime.now().toString();
        TestEvent correctionEvent = new TestEvent(startingEvent, TestCorrectionStatus.REMOVED, reason);
        _repo.save(correctionEvent);

        Optional<TestEvent> eventReloadOptional = _repo.findById(correctionEvent.getInternalId());
        assertTrue(eventReloadOptional.isPresent());
        TestEvent eventReloaded = eventReloadOptional.get();

        assertEquals(reason, eventReloaded.getReasonForCorrection());
        assertEquals(TestCorrectionStatus.REMOVED, eventReloaded.getCorrectionStatus());
        assertEquals(startingEvent.getInternalId(), eventReloaded.getPriorCorrectedTestEventId());
        // compare with starting event. Verify that was correctly copied
        assertEquals(startingEvent.getTestOrderId(), eventReloaded.getTestOrderId());
        assertEquals(startingEvent.getOrganization().getInternalId(), eventReloaded.getOrganization().getInternalId());
        assertEquals(startingEvent.getFacility().getInternalId(), eventReloaded.getFacility().getInternalId());
        assertEquals(startingEvent.getResult(), eventReloaded.getResult());
        assertEquals(startingEvent.getProviderData(), eventReloaded.getProviderData());
        assertEquals(startingEvent.getPatientData(), eventReloaded.getPatientData());
        compareAskOnEntrySurvey(startingEvent.getSurveyData(), eventReloaded.getSurveyData());
    }
}
