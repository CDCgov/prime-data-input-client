import { useState } from "react";
import { Redirect } from "react-router-dom";

import Button from "../../app/commonComponents/Button/Button";
import PatientTimeOfTestContainer from "../PatientTimeOfTestContainer";
import { useAppConfig } from "../../hooks/useAppConfig";
import { usePatient } from "../../hooks/usePatient";

import PatientProfile from "./PatientProfile";

const PatientProfileContainer = () => {
  const [nextPage, setNextPage] = useState(false);
  const [editPage, setEditPage] = useState(false);
  const {
    config: { plid },
  } = useAppConfig();
  const { patient } = usePatient();

  if (editPage) {
    return (
      <Redirect
        push
        to={{
          pathname: "/patient-info-edit",
          search: `?plid=${plid}`,
        }}
      />
    );
  }

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

  const buttonGroup = (
    <>
      <div className="margin-top-3">
        <Button
          id="patient-confirm-and-continue"
          label={"Confirm and continue"}
          onClick={() => {
            setNextPage(true);
          }}
        />
      </div>
      <Button
        id="edit-patient-profile-button"
        className="margin-top-1"
        variant="outline"
        label={"Edit information"}
        onClick={() => {
          setEditPage(true);
        }}
      />
    </>
  );

  return (
    <PatientTimeOfTestContainer currentPage={"profile"}>
      <PatientProfile patient={patient} />
      {buttonGroup}
    </PatientTimeOfTestContainer>
  );
};

export default PatientProfileContainer;
