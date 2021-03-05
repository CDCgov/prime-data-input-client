import React, { useState } from "react";
import { toast } from "react-toastify";
import { gql, useQuery, useMutation } from "@apollo/client";
import {
  useAppInsightsContext,
  useTrackEvent,
} from "@microsoft/applicationinsights-react-js";

import Alert from "../../commonComponents/Alert";
import { QUEUE_NOTIFICATION_TYPES, ALERT_CONTENT } from "../constants";
import { showNotification } from "../../utils";

import SearchResults from "./SearchResults";
import SearchInput from "./SearchInput";

const MIN_SEARCH_CHARACTER_COUNT = 2;

export const QUERY_PATIENT = gql`
  query GetPatientsByFacility($facilityId: String!, $searchTerm: String) {
    patients(
      facilityId: $facilityId
      pageNumber: 0
      pageSize: 20
      showDeleted: false
      searchTerm: $searchTerm
    ) {
      internalId
      firstName
      lastName
      middleName
      birthDate
      gender
      telephone
    }
  }
`;

const ADD_PATIENT_TO_QUEUE = gql`
  mutation AddPatientToQueue(
    $facilityId: String!
    $patientId: String!
    $symptoms: String
    $symptomOnset: LocalDate
    $pregnancy: String
    $firstTest: Boolean
    $priorTestDate: LocalDate
    $priorTestType: String
    $priorTestResult: String
    $noSymptoms: Boolean
  ) {
    addPatientToQueue(
      facilityId: $facilityId
      patientId: $patientId
      pregnancy: $pregnancy
      noSymptoms: $noSymptoms
      symptoms: $symptoms
      firstTest: $firstTest
      priorTestDate: $priorTestDate
      priorTestType: $priorTestType
      priorTestResult: $priorTestResult
      symptomOnset: $symptomOnset
    )
  }
`;

const UPDATE_AOE = gql`
  mutation UpdateAOE(
    $patientId: String!
    $symptoms: String
    $symptomOnset: LocalDate
    $pregnancy: String
    $firstTest: Boolean
    $priorTestDate: LocalDate
    $priorTestType: String
    $priorTestResult: String
    $noSymptoms: Boolean
  ) {
    updateTimeOfTestQuestions(
      patientId: $patientId
      pregnancy: $pregnancy
      symptoms: $symptoms
      noSymptoms: $noSymptoms
      firstTest: $firstTest
      priorTestDate: $priorTestDate
      priorTestType: $priorTestType
      priorTestResult: $priorTestResult
      symptomOnset: $symptomOnset
    )
  }
`;

const AddToQueueSearchBox = ({ refetchQueue, facilityId, patientsInQueue }) => {
  const appInsights = useAppInsightsContext();
  const trackAddPatientToQueue = useTrackEvent(
    appInsights,
    "Add Patient to Queue"
  );
  const [queryString, setQueryString] = useState("");
  const { data, error } = useQuery(QUERY_PATIENT, {
    fetchPolicy: "no-cache",
    variables: { facilityId, searchTerm: queryString },
  });
  const [mutationError, updateMutationError] = useState(null);
  const [addPatientToQueue] = useMutation(ADD_PATIENT_TO_QUEUE);
  const [updateAoe] = useMutation(UPDATE_AOE);

  if (error) {
    throw error;
  }
  if (mutationError) {
    throw mutationError;
  }

  const onInputChange = (event) => {
    let newValue = event.target.value;
    // debouncing helps reduce traffic, but introduces a lag in results.
    // until performance becomes an issue, don't debounce.
    // (FYI lodash.debounce() is available)
    setQueryString(newValue);
  };

  const onSearchClick = (event) => {
    event.preventDefault();
  };

  const onAddToQueue = (
    patient,
    {
      noSymptoms,
      symptoms,
      symptomOnset,
      pregnancy,
      firstTest,
      priorTestResult,
      priorTestDate,
      priorTestType,
    },
    createOrUpdate = "create"
  ) => {
    setQueryString("");
    trackAddPatientToQueue();
    let callback;
    const variables = {
      patientId: patient.internalId,
      noSymptoms,
      symptoms,
      symptomOnset,
      pregnancy,
      firstTest,
      priorTestDate,
      priorTestType,
      priorTestResult,
    };
    if (createOrUpdate === "create") {
      callback = addPatientToQueue;
      variables.facilityId = facilityId;
    } else {
      callback = updateAoe;
    }
    return callback({ variables })
      .then((res) => {
        let { type, title, body } = {
          ...ALERT_CONTENT[QUEUE_NOTIFICATION_TYPES.ADDED_TO_QUEUE__SUCCESS](
            patient
          ),
        };
        let alert = <Alert type={type} title={title} body={body} />;
        showNotification(toast, alert);
        refetchQueue();
        if (createOrUpdate === "create") {
          return res.data.addPatientToQueue;
        }
      })
      .catch((error) => {
        updateMutationError(error);
      });
  };

  return (
    <React.Fragment>
      <SearchInput
        onSearchClick={onSearchClick}
        onInputChange={onInputChange}
        queryString={queryString}
        disabled={queryString.length < MIN_SEARCH_CHARACTER_COUNT}
      />
      <SearchResults
        patients={data?.patients || []}
        onAddToQueue={onAddToQueue}
        facilityId={facilityId}
        patientsInQueue={patientsInQueue}
        shouldShowSuggestions={queryString.length >= MIN_SEARCH_CHARACTER_COUNT}
      />
    </React.Fragment>
  );
};

export default AddToQueueSearchBox;
