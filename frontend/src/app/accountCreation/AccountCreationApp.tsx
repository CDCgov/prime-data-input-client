import { FunctionComponent, useEffect } from "react";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { Route, Switch, BrowserRouter as Router } from "react-router-dom";

import PrimeErrorBoundary from "../PrimeErrorBoundary";
import USAGovBanner from "../commonComponents/USAGovBanner";
import { getActivationTokenFromUrl } from "../utils/url";
import PageNotFound from "../commonComponents/PageNotFound";
import { useAppConfig } from "../../hooks/useAppConfig";

import { SecurityQuestion } from "./SecurityQuestion/SecurityQuestion";
import { MfaSelect } from "./MfaSelect/MfaSelect";
import { MfaSms } from "./MfaSms/MfaSms";
import { MfaComplete } from "./MfaComplete/MfaComplete";
import { MfaOkta } from "./MfaOkta/MfaOkta";
import { MfaGoogleAuth } from "./MfaGoogleAuth/MfaGoogleAuth";
import { MfaSecurityKey } from "./MfaSecurityKey/MfaSecurityKey";
import { MfaSmsVerify } from "./MfaSmsVerify/MfaSmsVerify";
import { MfaEmailVerify } from "./MfaEmailVerify/MfaEmailVerify";
import { MfaPhone } from "./MfaPhone/MfaPhone";
import { MfaPhoneVerify } from "./MfaPhoneVerify/MfaPhoneVerify";
import { MfaOktaVerify } from "./MfaOktaVerify/MfaOktaVerify";
import { MfaGoogleAuthVerify } from "./MfaGoogleAuthVerify/MfaGoogleAuthVerify";
import { PasswordForm } from "./PasswordForm/PasswordForm";

interface WrapperProps {
  activationToken: string | null | undefined;
}
const AccountCreation404Wrapper: FunctionComponent<WrapperProps> = ({
  activationToken,
  children,
}) => {
  if (activationToken === undefined) {
    return <>Loading...</>;
  }
  if (activationToken === null) {
    return <PageNotFound />;
  }
  return <>{children}</>;
};

const AccountCreationApp = () => {
  const {
    setActivationToken,
    config: { activationToken },
  } = useAppConfig();

  useEffect(() => {
    setActivationToken(getActivationTokenFromUrl());
  }, [setActivationToken]);

  return (
    <PrimeErrorBoundary>
      <div className="App">
        <div id="main-wrapper">
          <USAGovBanner />
          <AccountCreation404Wrapper activationToken={activationToken}>
            <Router basename={`${process.env.PUBLIC_URL}/uac`}>
              <Switch>
                <Route path="/" exact component={PasswordForm} />
                <Route path="/set-password" component={PasswordForm} />
                <Route
                  path="/set-recovery-question"
                  component={SecurityQuestion}
                />
                <Route path="/mfa-select" component={MfaSelect} />
                <Route path="/mfa-sms/verify" component={MfaSmsVerify} />
                <Route path="/mfa-sms" component={MfaSms} />
                <Route path="/mfa-okta/verify" component={MfaOktaVerify} />
                <Route path="/mfa-okta" component={MfaOkta} />
                <Route
                  path="/mfa-google-auth/verify"
                  component={MfaGoogleAuthVerify}
                />
                <Route path="/mfa-google-auth" component={MfaGoogleAuth} />
                <Route path="/mfa-security-key" component={MfaSecurityKey} />
                <Route path="/mfa-phone/verify" component={MfaPhoneVerify} />
                <Route path="/mfa-phone" component={MfaPhone} />
                <Route path="/mfa-email/verify" component={MfaEmailVerify} />
                <Route path="/success" component={MfaComplete} />
              </Switch>
            </Router>
            <ToastContainer
              autoClose={5000}
              closeButton={false}
              limit={2}
              position="bottom-center"
              hideProgressBar={true}
            />
          </AccountCreation404Wrapper>
        </div>
      </div>
    </PrimeErrorBoundary>
  );
};

export default AccountCreationApp;
