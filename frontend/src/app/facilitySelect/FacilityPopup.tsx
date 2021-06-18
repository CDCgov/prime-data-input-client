import { useReactiveVar } from "@apollo/client";
import React from "react";

import { appConfig } from "../../storage/store";
import { formatFullName } from "../utils/user";

import "./FacilitySelect.scss";

const FacilityPopup: React.FC = ({ children }) => {
  const { user, organization } = useReactiveVar(appConfig);

  return (
    <main className="prime-home" id="facility-select">
      <div className="grid-container">
        <div className="grid-row position-relative">
          <div className="prime-container card-container">
            <div className="usa-card__header">
              <div>
                <h2>SimpleReport</h2>
                <div className="organization-name">{organization.name}</div>
              </div>
            </div>
            <div className="usa-card__body">
              <p className="welcome">Welcome, {formatFullName(user)}</p>
              {children}
            </div>
          </div>
        </div>
      </div>
    </main>
  );
};

export default FacilityPopup;
