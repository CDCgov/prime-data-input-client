import { useReactiveVar } from "@apollo/client";

import { patient as patientVar } from "../storage/store";

export const usePatient = () => {
  const patient = useReactiveVar(patientVar);

  const setCurrentPatient = (newPatient: Patient) => {
    patientVar(newPatient);
  };

  const updatePatientField = (field: string, value: any) => {
    patientVar({ ...(patientVar() as Patient), [field]: value });
  };

  return {
    patient,
    setCurrentPatient,
    updatePatientField,
  };
};
