import React from "react";
import { gql, useQuery } from "@apollo/client";

import PatientForm from "./PatientForm";

const GET_PATIENT = gql`
  query Patient($id: String!) {
    patient(id: $id) {
      lookupId
      firstName
      middleName
      lastName
      birthDate
      street
      streetTwo
      city
      state
      zipCode
      telephone
      role
      email
      county
      race
      ethnicity
      gender
      residentCongregateSetting
      employedInHealthcare
      testResults {
        internalId
        dateTested
        result
      }
    }
  }
`;

const EditPatient = (props) => {
  const { data, loading, error } = useQuery(GET_PATIENT, {
    variables: { id: props.patientId || "" },
  });

  if (loading) {
    return <p>Loading...</p>;
  }
  if (error) {
    return <p>error loading patient with id {props.patientId}...</p>;
  }

  return <PatientForm patient={data.patient} patientId={props.patientId} />;
};

export default EditPatient;
