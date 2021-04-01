import React, { useState } from "react";
import { gql, useMutation } from "@apollo/client";
import { toast } from "react-toastify";
import { Redirect } from "react-router-dom";
import { useSelector } from "react-redux";

import {
  PATIENT_TERM_CAP,
  PATIENT_TERM_PLURAL_CAP,
} from "../../config/constants";
import { showNotification } from "../utils";
import Alert from "../commonComponents/Alert";
import Breadcrumbs from "../commonComponents/Breadcrumbs";
import Button from "../commonComponents/Button";
import { RootState } from "../store";

import PersonForm from "./Components/PersonForm";

export const ADD_PATIENT = gql`
  mutation AddPatient(
    $facilityId: ID
    $firstName: String!
    $middleName: String
    $lastName: String!
    $birthDate: LocalDate!
    $street: String!
    $streetTwo: String
    $city: String
    $state: String!
    $zipCode: String!
    $telephone: TelephoneNumber!
    $role: String
    $lookupId: String
    $email: String
    $county: String
    $race: String
    $ethnicity: String
    $gender: String
    $residentCongregateSetting: Boolean!
    $employedInHealthcare: Boolean!
  ) {
    addPatient(
      facilityId: $facilityId
      firstName: $firstName
      middleName: $middleName
      lastName: $lastName
      birthDate: $birthDate
      street: $street
      streetTwo: $streetTwo
      city: $city
      state: $state
      zipCode: $zipCode
      telephone: $telephone
      role: $role
      lookupId: $lookupId
      email: $email
      county: $county
      race: $race
      ethnicity: $ethnicity
      gender: $gender
      residentCongregateSetting: $residentCongregateSetting
      employedInHealthcare: $employedInHealthcare
    ) {
      internalId
    }
  }
`;

type AddPatientParams = Nullable<Omit<PersonFormData, "lookupId">>;

interface AddPatientResponse {
  internalId: string;
}

const AddPatient = () => {
  const [addPatient, { loading }] = useMutation<
    AddPatientResponse,
    AddPatientParams
  >(ADD_PATIENT);
  const activeFacilityId: string = useSelector<RootState, string>(
    (state) => state.facility.id
  );
  const personPath = `/patients/?facility=${activeFacilityId}`;
  const [redirect, setRedirect] = useState<string | undefined>(undefined);

  if (redirect) {
    return <Redirect to={redirect} />;
  }

  if (activeFacilityId.length < 1) {
    return <div>No facility selected</div>;
  }

  const savePerson = async (person: Nullable<PersonFormData>) => {
    await addPatient({ variables: { ...person } });
    showNotification(
      toast,
      <Alert
        type="success"
        title={`${PATIENT_TERM_CAP} Record Created`}
        body="New information record has been created."
      />
    );
    setRedirect(personPath);
  };

  return (
    <main className={"prime-edit-patient prime-home"}>
      <div className={"grid-container margin-bottom-4"}>
        <PersonForm
          patient={{
            facilityId: "",
            firstName: null,
            middleName: null,
            lastName: null,
            lookupId: null,
            role: null,
            race: null,
            ethnicity: null,
            gender: null,
            residentCongregateSetting: null,
            employedInHealthcare: null,
            birthDate: null,
            telephone: null,
            county: null,
            email: null,
            street: null,
            streetTwo: null,
            city: null,
            state: null,
            zipCode: null,
          }}
          activeFacilityId={activeFacilityId}
          savePerson={savePerson}
          getHeader={(_, onSave, formChanged) => (
            <>
              <Breadcrumbs
                crumbs={[
                  {
                    link: personPath,
                    text: PATIENT_TERM_PLURAL_CAP,
                  },
                  {
                    link: "",
                    text: `Add New ${PATIENT_TERM_CAP}`,
                  },
                ]}
              />
              <div className="prime-edit-patient-heading">
                <div>
                  <h1>Add New {PATIENT_TERM_CAP}</h1>
                </div>
                <button
                  className="usa-button prime-save-patient-changes"
                  disabled={loading || !formChanged}
                  onClick={onSave}
                >
                  {loading ? "Saving..." : "Save changes"}
                </button>
              </div>
            </>
          )}
          getFooter={(onSave, formChanged) => (
            <div className="prime-edit-patient-heading">
              <Button
                id="edit-patient-save-lower"
                className="prime-save-patient-changes"
                disabled={loading || !formChanged}
                onClick={onSave}
                label={loading ? "Saving..." : "Save changes"}
              />
            </div>
          )}
        />
      </div>
    </main>
  );
};

export default AddPatient;
