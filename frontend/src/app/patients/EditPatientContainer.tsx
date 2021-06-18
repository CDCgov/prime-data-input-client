import React from "react";

import { useFacilities } from "../../hooks/useFacilities";

import EditPatient from "./EditPatient";

interface Props {
  patientId: string;
}

const EditPatientContainer: React.FC<Props> = ({ patientId }) => {
  const {
    facilities: { selectedFacility },
  } = useFacilities();

  const activeFacilityId: string | undefined = selectedFacility?.id;

  if (!activeFacilityId) {
    return <div>No facility selected</div>;
  }
  return <EditPatient facilityId={activeFacilityId} patientId={patientId} />;
};

export default EditPatientContainer;
