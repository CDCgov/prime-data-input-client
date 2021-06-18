import React, { useEffect, useState } from "react";
import { Redirect } from "react-router-dom";
import { toast } from "react-toastify";

import AoEForm from "../../app/testQueue/AoEForm/AoEForm";
import { showError } from "../../app/utils";
import PatientTimeOfTestContainer from "../PatientTimeOfTestContainer";
import { PxpApi } from "../PxpApiService";
import { useAppConfig } from "../../hooks/useAppConfig";
import { usePatient } from "../../hooks/usePatient";

interface Props {
  page: string;
}

const AoEPatientFormContainer: React.FC<Props> = ({ page }: Props) => {
  const [nextPage, setNextPage] = useState(false);
  const {
    config: { plid },
  } = useAppConfig();
  const { patient } = usePatient();

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  const saveCallback = async (data: any) => {
    try {
      if (patient) {
        await PxpApi.submitQuestions(
          plid as string,
          patient.birthDate.toString(),
          data
        );
        setNextPage(true);
      }
    } catch (e) {
      showError(toast, "There was an error submitting your responses");
      return;
    }
  };

  if (nextPage) {
    return (
      <Redirect
        to={{
          pathname: "/success",
          search: `?plid=${plid}`,
        }}
      />
    );
  }

  if (!patient) {
    return <>Patient is not selected</>;
  }

  return (
    <PatientTimeOfTestContainer currentPage={"symptoms"}>
      <AoEForm
        patient={patient}
        isModal={false}
        saveButtonText="Submit"
        noValidation={false}
        lastTest={patient.lastTest}
        saveCallback={saveCallback}
      />
    </PatientTimeOfTestContainer>
  );
};

export default AoEPatientFormContainer;
