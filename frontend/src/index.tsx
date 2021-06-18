import React from "react";
import ReactDOM from "react-dom";
import { ApolloProvider } from "@apollo/client";
import { Switch, Route, BrowserRouter as Router } from "react-router-dom";
import Modal from "react-modal";

import App from "./app/App";
import AccountCreationApp from "./app/accountCreation/AccountCreationApp";
import TelemetryProvider from "./app/telemetry-provider";
import HealthChecks from "./app/HealthChecks";
import { SelfRegistration } from "./patientApp/selfRegistration/SelfRegistration";
import PatientApp from "./patientApp/PatientApp";
import client from "./config/client";
import * as serviceWorker from "./serviceWorker";

import "./styles/App.css";

// Define the root element for modals
if (process.env.NODE_ENV !== "test") {
  Modal.setAppElement("#root");
}

if (window.location.hash) {
  const params = new URLSearchParams(window.location.hash.slice(1));
  const bearerToken = params.get("access_token");
  if (bearerToken) {
    localStorage.setItem("access_token", bearerToken);
  }
  // We need to store the ID token in order for logout to work correctly.
  const idToken = params.get("id_token");
  if (idToken) {
    localStorage.setItem("id_token", idToken);
  }
}

export const ReactApp = (
  <ApolloProvider client={client}>
    <React.StrictMode>
      <Router basename={process.env.PUBLIC_URL}>
        <TelemetryProvider
          instrumentationKey={process.env.REACT_APP_APPINSIGHTS_KEY}
        >
          <Switch>
            <Route path="/health" component={HealthChecks} />
            <Route path="/pxp" component={PatientApp} />
            <Route path="/uac" component={AccountCreationApp} />
            <Route
              path="/register/:registrationLink"
              component={SelfRegistration}
            />
            <Route path="/" component={App} />
            <Route component={() => <>Page not found</>} />
          </Switch>
        </TelemetryProvider>
      </Router>
    </React.StrictMode>
  </ApolloProvider>
);
export const rootElement = document.getElementById("root");

ReactDOM.render(ReactApp, rootElement);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
