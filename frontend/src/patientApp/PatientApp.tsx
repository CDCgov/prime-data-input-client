import { FunctionComponent, useEffect } from "react";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { Route, Switch, BrowserRouter as Router } from "react-router-dom";

import PrimeErrorBoundary from "../app/PrimeErrorBoundary";
import USAGovBanner from "../app/commonComponents/USAGovBanner";
import { getPatientLinkIdFromUrl } from "../app/utils/url";
import PageNotFound from "../app/commonComponents/PageNotFound";
import { useAppConfig } from "../hooks/useAppConfig";
import { usePatient } from "../hooks/usePatient";

import PatientHeader from "./PatientHeader";
import TermsOfService from "./timeOfTest/TermsOfService";
import DOB from "./timeOfTest/DOB";
import AoEPatientFormContainer from "./timeOfTest/AoEPatientFormContainer";
import PatientLanding from "./timeOfTest/PatientLanding";
import PatientProfileContainer from "./timeOfTest/PatientProfileContainer";
import PatientFormContainer from "./timeOfTest/PatientFormContainer";
import TestResult from "./timeOfTest/TestResult";
import GuardedRoute from "./GuardedRoute";

interface WrapperProps {
  plid: string | undefined;
}
const PatientLinkURL404Wrapper: FunctionComponent<WrapperProps> = ({
  plid,
  children,
}) => {
  if (plid === undefined) {
    return <>Loading...</>;
  }
  if (plid === null) {
    return <PageNotFound />;
  }
  return <>{children}</>;
};

const PatientApp = () => {
  const {
    updateConfigField,
    config: { plid },
  } = useAppConfig();
  const { patient } = usePatient();

  const auth = patient?.internalId !== "";

  useEffect(() => {
    updateConfigField("plid", getPatientLinkIdFromUrl());
    // eslint-disable-next-line
  }, []);

  return (
    <PrimeErrorBoundary>
      <div className="App">
        <div id="main-wrapper">
          <USAGovBanner />
          <PatientHeader />
          <PatientLinkURL404Wrapper plid={plid}>
            <Router basename={`${process.env.PUBLIC_URL}/pxp`}>
              <Switch>
                <Route path="/" exact component={TermsOfService} />
                <Route path="/terms-of-service" component={TermsOfService} />
                <Route path="/birth-date-confirmation" component={DOB} />
                <GuardedRoute
                  auth={auth}
                  path="/patient-info-confirm"
                  component={PatientProfileContainer}
                />
                <GuardedRoute
                  auth={auth}
                  path="/patient-info-edit"
                  component={PatientFormContainer}
                />
                <GuardedRoute
                  auth={auth}
                  path="/patient-info-symptoms"
                  component={AoEPatientFormContainer}
                />
                <GuardedRoute
                  auth={auth}
                  path="/success"
                  component={PatientLanding}
                />
                <GuardedRoute
                  auth={auth}
                  path="/test-result"
                  component={TestResult}
                />
              </Switch>
            </Router>
            <ToastContainer
              autoClose={5000}
              closeButton={false}
              limit={2}
              position="bottom-center"
              hideProgressBar={true}
            />
          </PatientLinkURL404Wrapper>
        </div>
      </div>
    </PrimeErrorBoundary>
  );
};

export default PatientApp;
