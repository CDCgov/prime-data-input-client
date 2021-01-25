import { gql, useQuery, useMutation } from "@apollo/client";
import React from "react";
import { NavLink } from "react-router-dom";
import moment from "moment";
import { displayFullName, showError, showNotification } from "../utils";
import { toast } from "react-toastify";

// this can't be the best way to handle this?
import {
  PATIENT_TERM_CAP,
  PATIENT_TERM_PLURAL_CAP,
} from "../../config/constants";
import { daysSince } from "../utils/date";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import Alert from "../commonComponents/Alert";


const uploadPatients = gql`
  mutation UploadPatients($patientList: Upload!) {
    uploadPatients(patientList: $patientList)
  }
`;

const patientQuery = gql`
  query GetPatientsByFacility($facilityId: String!) {
    patients(facilityId: $facilityId) {
      internalId
      lookupId
      firstName
      lastName
      middleName
      birthDate
      lastTest {
        dateAdded
      }
    }
  }
`;

interface Patient {
  internalId: string;
  lookupId: string;
  firstName: string;
  lastName: string;
  middleName: string;
  birthDate: string;
  lastTest: {
    dateAdded: string;
  };
}

interface Data {
  patients: Patient[];
}

interface Props {
  activeFacilityId: string;
  canEditUser: boolean;
}

const ManagePatients = ({ activeFacilityId, canEditUser }: Props) => {
  const [upload] = useMutation(uploadPatients);

  const { data, loading, error, refetch } = useQuery<Data, {}>(patientQuery, {
    fetchPolicy: "no-cache",
    variables: {
      facilityId: activeFacilityId,
    },
  });

  const patientRows = (patients: Patient[]) => {
    return patients.map((patient: Patient) => {
      let fullName = displayFullName(
        patient.firstName,
        patient.middleName,
        patient.lastName
      );

      let editUserLink = canEditUser ? (
        <NavLink
          to={`/patient/${patient.internalId}?facility=${activeFacilityId}`}
        >
          {fullName}
        </NavLink>
      ) : (
        <span>{fullName}</span>
      );

      return (
        <tr key={patient.internalId}>
          <th scope="row">{editUserLink}</th>
          <td>{patient.lookupId}</td>
          <td> {patient.birthDate}</td>
          <td>
            {patient.lastTest
              ? `${daysSince(moment(patient.lastTest.dateAdded))}`
              : "N/A"}
          </td>
        </tr>
      );
    });
  };

  const bulkUpload = async ({target: {files }}: React.ChangeEvent<HTMLInputElement>) => {
    const fileList = files;
    if (fileList === null) {
      showError(toast, "Error", "File not found");
      return;
    } 
    upload({variables: {patientList: fileList[0]}}).then(() => {
      showNotification(
        toast,
        <Alert
          type="success"
          title={`Patients uploaded`}
          body="Successfully uploaded patients."
        />
      );
      refetch();
    },
    (_error) => {
      showError(
        toast,
        `${PATIENT_TERM_CAP} Data Error`,
        "Please check for missing data or typos."
      );
    });
  }

  return (
    <main className="prime-home">
      <div className="grid-container">
        <div className="grid-row">
          <div className="prime-container usa-card__container">
            <div className="usa-card__header">
              <h2> All {PATIENT_TERM_PLURAL_CAP}</h2>
              {canEditUser ? (<>
                <NavLink
                  className="usa-button usa-button--outline"
                  to={`/add-patient/?facility=${activeFacilityId}`}
                  id="add-patient-button"
                >
                  <FontAwesomeIcon icon="plus" />
                  {` New ${PATIENT_TERM_CAP}`}
                </NavLink>
                              <input
                              type="file"
                              name="file"
                              placeholder='UploadCSV...'
                              onChange={bulkUpload}
                            /></>
              ) : null}

            </div>
            <div className="usa-card__body">
              {error ? (
                <p>Error in loading patients</p>
              ) : loading ? (
                <p>Loading patients...</p>
              ) : data ? (
                <table className="usa-table usa-table--borderless width-full">
                  <thead>
                    <tr>
                      <th scope="col">Name</th>
                      <th scope="col">Unique ID</th>
                      <th scope="col">Date of Birth</th>
                      <th scope="col">Days since last test</th>
                    </tr>
                  </thead>
                  <tbody>{patientRows(data.patients)}</tbody>
                </table>
              ) : (
                <p> no patients found</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </main>
  );
};

export default ManagePatients;
