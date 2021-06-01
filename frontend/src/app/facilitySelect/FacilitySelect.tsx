import React from "react";

import Button from "../commonComponents/Button/Button";

import FacilityPopup from "./FacilityPopup";

import "./FacilitySelect.scss";

export interface FacilitySelectProps {
  setActiveFacility: (facility: Facility) => void;
  facilities: Facility[];
}

const FacilitySelect: React.FC<FacilitySelectProps> = ({
  facilities,
  setActiveFacility,
}) => {
  return (
    <FacilityPopup>
      <p className="select-text">
        Please select the testing facility where you are working today.
      </p>
      {facilities.map((f) => (
        <Button
          key={f.id}
          onClick={() => setActiveFacility(f)}
          variant="outline"
        >
          {f.name}
        </Button>
      ))}
    </FacilityPopup>
  );
};

export default FacilitySelect;
