import siteLogo from "../img/simplereport-logomark-color.svg";
import { useAppConfig } from "../hooks/useAppConfig";

const PatientHeader = () => {
  const {
    config: { organization },
  } = useAppConfig();

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
                  COVID-19 Testing Portal
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
