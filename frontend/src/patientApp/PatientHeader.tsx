import { useSelector } from "react-redux";
import { useTranslation } from "react-i18next";

import siteLogo from "../img/simplereport-logomark-color.svg";

const PatientHeader = () => {
  const organization = useSelector(
    (state) => (state as any).organization as Organization
  );

  const { t } = useTranslation("translation");

  return (
    <header className="border-bottom border-base-lighter">
      <div className="display-flex flex-align-center maxw-tablet grid-container">
        <div className="padding-y-1">
          <div className="margin-bottom-0" id="basic-logo">
            <div
              className="display-flex flex-align-center"
              title="Home"
              aria-label="Home"
            >
              <img
                className="width-4"
                src={siteLogo}
                alt="{process.env.REACT_APP_TITLE}"
              />
              <div className="logo-text margin-left-1 display-flex flex-column">
                <span className="prime-organization-name margin-left-0 font-body-md text-primary-darker text-bold">
                  {organization?.name}
                </span>
                <span className="prime-organization-name margin-left-0 margin-top-05 text-primary-darker">
                  {t("header")}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};

export default PatientHeader;
