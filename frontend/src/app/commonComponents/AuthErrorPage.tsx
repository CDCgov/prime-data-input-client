import USAGovBanner from "../commonComponents/USAGovBanner";
import siteLogo from "../../img/simplereport-logo-color.svg";

const AuthErrorPage = () => {
  return (
    <>
      <USAGovBanner />
      <header className="border-bottom border-base-lighter padding-y-1">
        <div className="grid-container">
          <img
            className="maxh-4"
            src={siteLogo}
            alt="{process.env.REACT_APP_TITLE}"
          />
        </div>
      </header>
      <main className="usa-section">
        <div className="grid-container usa-prose">
          <h1 className="">
            You don't have the appropriate permissions to view this page
          </h1>
          <p>
            If the problem continues, contact{" "}
            <a href="mailto:support@simplereport.gov">
              support@simplereport.gov
            </a>{" "}
            for support.
          </p>
        </div>
      </main>
    </>
  );
};

export default AuthErrorPage;
