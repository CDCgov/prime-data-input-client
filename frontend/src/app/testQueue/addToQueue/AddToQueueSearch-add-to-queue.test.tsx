import React from "react";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MockedProvider } from "@apollo/client/testing";

import { AoEAnswersDelivery } from "../AoEForm/AoEForm";
import { Patient } from "../../patients/ManagePatients";
import { TestResult } from "../QueueItem";

import AddToQueueSearch, {
  ADD_PATIENT_TO_QUEUE,
  QUERY_PATIENT,
} from "./AddToQueueSearch";
import { QueueProps } from "./SearchResults";

let refetchQueueMock;

const facilityId = "fake-facility-id";
const dummyTest = {
  dateAdded: "2020-01-01",
  result: "NEGATIVE" as TestResult,
  dateTested: "2020-01-01",
  deviceTypeModel: "MegaTester2000",
};

const mockPatient: Patient = {
  internalId: "48c523e8-7c65-4047-955c-e3f65bb8b58a",
  firstName: "Barb",
  middleName: "Whitaker",
  lastName: "Cragell",
  birthDate: "1960-11-07",
  isDeleted: false,
  role: "somerole",
  lastTest: dummyTest,
};

const mockAoeAnswers: AoEAnswersDelivery = {
  noSymptoms: true,
  symptoms: '{"something":"true"}',
  symptomOnset: "2021-06-14",
  firstTest: true,
  priorTestType: "fake-test-type",
  priorTestDate: "2021-05-14",
  priorTestResult: "NEGATIVE",
  pregnancy: "77386006",
  testResultDelivery: "SMS",
};

const patientNotInQueue = {
  internalId: "48c523e8-7c65-4047-955c-e3f65bb8b58a",
  firstName: "Barb",
  middleName: "Whitaker",
  lastName: "Cragell",
  birthDate: "1960-11-07",
  gender: "male",
  lookupId: null,
  telephone: "800 232-4636",
  testResultDelivery: "SMS",
  __typename: "Patient",
};

let queryPatientMockIsDone = false;
let addPatientMockIsDone = false;

const mockFetchGraphQLResult = jest.fn(() => {
  queryPatientMockIsDone = true;
  return {
    data: {
      patient: [patientNotInQueue],
    },
  };
});

const mockGraphQLResult = jest.fn(() => {
  addPatientMockIsDone = true;
  return {
    data: {
      addPatientToQueue: mockPatient.internalId,
    },
  };
});

const mocks = [
  {
    request: {
      query: QUERY_PATIENT,
      variables: {
        facilityId,
        namePrefixMatch: "bar",
      },
    },
    result: mockFetchGraphQLResult(),
  },
  {
    request: {
      query: ADD_PATIENT_TO_QUEUE,
      variables: {
        patientId: mockPatient.internalId,
        facilityId,
        ...mockAoeAnswers,
      },
    },
    result: mockGraphQLResult(),
  },
];

jest.mock("./SearchResults", () => {
  return (sr: QueueProps) => {
    return (
      <button
        type="submit"
        onClick={() => sr.onAddToQueue(mockPatient, mockAoeAnswers, "create")}
      >
        I'm a magic fake button click me please
      </button>
    );
  };
});

describe("AddToSearchQueue - add to queue", () => {
  beforeEach(async () => {
    refetchQueueMock = jest.fn();

    render(
      <MockedProvider mocks={mocks} addTypename={false}>
        <AddToQueueSearch
          refetchQueue={refetchQueueMock}
          facilityId={facilityId}
          patientsInQueue={[]}
        />
      </MockedProvider>
    );
  });

  it("adds patient to queue from search form", async () => {
    fireEvent.change(screen.getByRole("searchbox", { exact: false }), {
      target: { value: "bar" },
    });

    await waitFor(async () => {
      fireEvent.click(screen.getAllByRole("button")[1]);
    });

    expect(queryPatientMockIsDone).toBe(true);
    expect(addPatientMockIsDone).toBe(true);
  });
});
