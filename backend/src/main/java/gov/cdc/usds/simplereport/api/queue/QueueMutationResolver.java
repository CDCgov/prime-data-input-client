package gov.cdc.usds.simplereport.api.queue;

import static gov.cdc.usds.simplereport.api.Translators.parseUserDate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import gov.cdc.usds.simplereport.api.model.ApiTestOrder;
import gov.cdc.usds.simplereport.db.model.auxiliary.TestResult;
import gov.cdc.usds.simplereport.service.PersonService;
import gov.cdc.usds.simplereport.service.TestOrderService;
import graphql.kickstart.tools.GraphQLMutationResolver;

/**
 * Mutations for creating and updating test queue entries.
 */
@Component
public class QueueMutationResolver implements GraphQLMutationResolver  {

    private final TestOrderService _tos;
    private final PersonService _ps;

    public QueueMutationResolver(TestOrderService tos, PersonService ps) {
      _tos = tos;
      _ps = ps;
    }

    public void addTestResult(String deviceID, String result, String patientID, Date dateTested) {
      _tos.addTestResult(
        deviceID,
        TestResult.valueOf(result),
        patientID,
        dateTested
      );
    }

    public ApiTestOrder editQueueItem(String id, String deviceId, String result, Date dateTested) {
        return new ApiTestOrder(_tos.editQueueItem(
            id,
            deviceId,
            result,
            dateTested
        ));
    }

    public void addPatientToQueue(
        String facilityID,
        String patientID,
        String pregnancy,
        String symptoms,
        boolean firstTest,
        String priorTestDate,
        String priorTestType,
        String priorTestResult,
        String symptomOnset,
        boolean noSymptoms
    ) throws JSONException {
      LocalDate localPriorTestDate = parseUserDate(priorTestDate);
      LocalDate localSymptomOnset =  parseUserDate(symptomOnset);

      Map<String, Boolean> symptomsMap = new HashMap<String, Boolean>();
      JSONObject symptomsJSONObject = new JSONObject(symptoms);
      Iterator<?> keys = symptomsJSONObject.keys();
      while(keys.hasNext() ){
          String key = (String)keys.next();
          Boolean value = symptomsJSONObject.getBoolean(key); 
          symptomsMap.put(key, value);
      }

      _tos.addPatientToQueue(
        UUID.fromString(facilityID),
        _ps.getPatient(patientID),
        pregnancy,
        symptomsMap,
        firstTest,
        localPriorTestDate,
        priorTestType,
        (priorTestResult == null) ? null : TestResult.valueOf(priorTestResult),
        localSymptomOnset,
        noSymptoms
      );
    }

    public void removePatientFromQueue(String patientID) {
      _tos.removePatientFromQueue(patientID);
    }

    public int clearQueue() {
      return _tos.cancelAll();
    }

    public void updateTimeOfTestQuestions(
      String patientID,
      String pregnancy,
      String symptoms,
      boolean firstTest,
      String priorTestDate,
      String priorTestType,
      String priorTestResult,
      String symptomOnset,
      boolean noSymptoms
    ) {
      LocalDate localPriorTestDate = parseUserDate(priorTestDate);
      LocalDate localSymptomOnset =  parseUserDate(symptomOnset);

      Map<String, Boolean> symptomsMap = new HashMap<String, Boolean>();
      JSONObject symptomsJSONObject = new JSONObject(symptoms);
      Iterator<?> keys = symptomsJSONObject.keys();
      while(keys.hasNext() ){
          String key = (String)keys.next();
          Boolean value = symptomsJSONObject.getBoolean(key); 
          symptomsMap.put(key, value);
      }

      _tos.updateTimeOfTestQuestions(
        patientID,
        pregnancy,
        symptomsMap,
        firstTest,
        localPriorTestDate,
        priorTestType,
        priorTestResult == null ? null : TestResult.valueOf(priorTestResult),
        localSymptomOnset,
        noSymptoms
      );
    }

}
