import React, { useState } from "react";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import {
  BrowserRouter as Router,
  Redirect,
  Route,
  Switch,
} from "react-router-dom";

import Header from "./commonComponents/Header";
import USAGovBanner from "./commonComponents/USAGovBanner";
import OrganizationHomeContainer from "./OrganizationView/OrganizationHomeContainer";
import LoginView from "./LoginView";
//import Footer from "./commonComponents/Footer";
import ProtectedRoute from "./commonComponents/ProtectedRoute";
const isAuthenticated = true;

const App = () => {
  const [organization] = useState({ id: "123" });

  return (
    <div className="App">
      <div id="main-wrapper">
        <USAGovBanner />
        <Router basename={process.env.PUBLIC_URL}>
          <Header organizationId={organization.id} />
          <Switch>
            <Route path="/login" component={LoginView} />
            <ProtectedRoute
              path="/organization/:organizationId"
              component={OrganizationHomeContainer}
              isAuthenticated={isAuthenticated}
            />
            <Route path="/">
              <Redirect to={`/organization/${organization.id}`} />
            </Route>
            {/* <Route component={NotFoundComponent} /> */}
          </Switch>
        </Router>
        <ToastContainer
          autoClose={5000}
          closeButton={false}
          limit={1}
          position="bottom-center"
          hideProgressBar={true}
        />
        {/* <Footer /> */}
      </div>
    </div>
  );
};

export default App;
