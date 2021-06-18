import React, { useEffect, useState } from "react";
import { Redirect, useHistory } from "react-router-dom";
import { toast } from "react-toastify";

import { PxpApi } from "../../patientApp/PxpApiService";
import PersonForm, {
  PersonFormView,
} from "../../app/patients/Components/PersonForm";
import PatientTimeOfTestContainer from "../PatientTimeOfTestContainer";
import { showNotification } from "../../app/utils";
import Alert from "../../app/commonComponents/Alert";
import Button from "../../app/commonComponents/Button/Button";
import { usePatient } from "../../hooks/usePatient";
import { useFacilities } from "../../hooks/useFacilities";
import { useAppConfig } from "../../hooks/useAppConfig";

const PatientFormContainer = () => {
  const history = useHistory();
  const [nextPage, setNextPage] = useState(false);

  const {
    config: { plid },
  } = useAppConfig();
  const { patient, setCurrentPatient } = usePatient();
  const {
    facilities: { selectedFacility },
  } = useFacilities();

  const facility = selectedFacility;
  const patientInStore = patient as PersonFormData;

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  if (nextPage) {
    return (
      <Redirect
        to={{
          pathname: "/patient-info-symptoms",
          search: `?plid=${plid}`,
        }}
      />
    );
  }

  const savePerson = async (person: Nullable<PersonFormData>) => {
    const {
      street,
      streetTwo,
      city,
      state,
      county,
      zipCode,
      firstName,
      middleName,
      lastName,
      birthDate,
      ...withoutAddress
    } = person;
    const updatedPatientFromApi = await PxpApi.updatePatient(
      plid as string,
      patientInStore.birthDate.toString(),
      {
        ...withoutAddress,
        address: {
          street: [street, streetTwo],
          city,
          state,
          county,
          zipCode,
        },
      }
    );
    showNotification(
      toast,
      <Alert type="success" title={`Your profile changes have been saved`} />
    );
    setCurrentPatient({
      ...updatedPatientFromApi,
    });
    setNextPage(true);
  };
  if (!patient) {
    return <>Patient is not selected </>;
  }
  return (
    <PatientTimeOfTestContainer currentPage={"profile"}>
      <main className={"prime-edit-patient prime-home padding-top-0"}>
        <div>
          <PersonForm
            patient={{
              ...patient,
              facilityId:
                patient?.facility === null
                  ? null
                  : (patient?.facility?.id as string),
            }}
            activeFacilityId={facility?.id || ""}
            patientId={patient.internalId}
            hideFacilitySelect={true}
            savePerson={savePerson}
            view={PersonFormView.PXP}
            getFooter={(onSave, formChanged) => (
              <div className="mobile-lg:display-flex flex-justify-end margin-top-2">
                <Button
                  id="edit-patient-save-lower"
                  disabled={!formChanged}
                  onClick={onSave}
                  label="Save and continue"
                />
                <Button
                  className="margin-top-1 mobile-lg:margin-top-0 margin-right-0"
                  variant="outline"
                  label={"Back"}
                  onClick={() => history.goBack()}
                />
              </div>
            )}
          />
        </div>
      </main>
    </PatientTimeOfTestContainer>
  );
};

export default PatientFormContainer;
