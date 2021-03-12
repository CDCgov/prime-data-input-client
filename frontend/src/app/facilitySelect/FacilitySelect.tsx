import React from "react";

import Button from "../commonComponents/Button";

import FacilityPopup from "./FacilityPopup";

import "./FacilitySelect.scss";

interface Props {
  setActiveFacility: (facility: Facility) => void;
  facilities: Facility[];
}

const FacilitySelect: React.FC<Props> = ({ facilities, setActiveFacility }) => {
  return (
    <FacilityPopup>
      <p className="select-text">
        Please select which facility you are working at today
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
