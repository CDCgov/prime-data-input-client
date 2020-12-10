package gov.cdc.usds.simplereport.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.cdc.usds.simplereport.db.model.Person;
import gov.cdc.usds.simplereport.db.model.TestEvent;
import gov.cdc.usds.simplereport.db.repository.TestEventRepository;


@Service
@Transactional(readOnly = true)
public class TestEventService {
    private TestEventRepository _terepo;

    public TestEventService(TestEventRepository terepo) {
        _terepo = terepo;
    }

    public TestEvent getLastTestResultsForPatient(Person person) {
        return _terepo.findFirst1ByPatientOrderByCreatedAtDesc(person);
    }
}
