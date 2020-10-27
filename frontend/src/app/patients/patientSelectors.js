import { createSelector } from "reselect";
import { getTestResults } from "../testResults/testResultsSelector";
import { displayFullName } from "../utils";
import moment from "moment";
var _ = require("lodash");

export const getPatients = (state) => state.patients;

// basically u8st a-
export const getPatientsWithLastTestResult = (state) =>
  createSelector(getPatients, getTestResults, (patients, testResults) => {
    return Object.entries(patients).map(([patientId, patient]) => {
      console.log("testReslts", testResults);
      let testResultDates = testResults[patientId].map((testResult) =>
        moment(testResult.dateTested)
      );
      let daysSinceMostRecentTestResult = moment
        .max(testResultDates)
        .diff(moment(), "days");
      let { firstName, middleName, lastName, birthDate } = { ...patient };
      return {
        displayName: displayFullName(firstName, middleName, lastName),
        birthDate,
        lastTestDate: daysSinceMostRecentTestResult,
      };
    });
  });

// gets a single patient by its id
export const getPatientById = (patientId) =>
  createSelector(getPatients, (patients) => patients[patientId] || null);

// gets multiple patients given an array of patientIds
export const getPatientsByIds = (patientIds) =>
  createSelector(getPatients, (patients) => _.pick(patients, patientIds));
