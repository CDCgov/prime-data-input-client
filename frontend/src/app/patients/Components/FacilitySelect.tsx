import React from "react";

import { useFacilities } from "../../../hooks/useFacilities";
import Select from "../../commonComponents/Select";

interface Props {
  facilityId: string | null;
  onChange: (facilityId: string | null) => void;
  validateField: () => void;
  validationStatus: (name: keyof PersonFormData) => "error" | undefined;
  errors: { [key: string]: string | undefined };
  hidden?: boolean;
}

const ALL_FACILITIES = "~~ALL-FACILITIES~~";
const NAME = "facilityId";

const FacilitySelect: React.FC<Props> = (props) => {
  const {
    facilities: { availableFacilities },
  } = useFacilities();

  if (props.hidden) {
    return null;
  }

  const facilityList = availableFacilities.map((f) => ({
    label: f.name,
    value: f.id,
  }));
  facilityList.unshift({ label: "All facilities", value: ALL_FACILITIES });

  const onChange = (value: string | null) => {
    props.onChange(value === ALL_FACILITIES ? null : value);
  };

  return (
    <Select
      label="Facility"
      name={NAME}
      value={props.facilityId === null ? ALL_FACILITIES : props.facilityId}
      onChange={onChange}
      onBlur={props.validateField}
      validationStatus={props.validationStatus(NAME)}
      errorMessage={props.errors[NAME]}
      options={facilityList}
      defaultSelect={true}
      required
    />
  );
};

export default FacilitySelect;
