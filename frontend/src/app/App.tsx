import React, { useEffect } from "react";
import { gql, useQuery } from "@apollo/client";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { Redirect, Route, Switch } from "react-router-dom";
import { ApplicationInsights } from "@microsoft/applicationinsights-web";

import { useFacilities } from "../hooks/useFacilities";
import { useAppConfig } from "../hooks/useAppConfig";

import ProtectedRoute from "./commonComponents/ProtectedRoute";
import PrimeErrorBoundary from "./PrimeErrorBoundary";
import Header from "./commonComponents/Header";
import USAGovBanner from "./commonComponents/USAGovBanner";
import LoginView from "./LoginView";
import TestResultsList from "./testResults/TestResultsList";
import TestQueueContainer from "./testQueue/TestQueueContainer";
import ManagePatientsContainer from "./patients/ManagePatientsContainer";
import EditPatientContainer from "./patients/EditPatientContainer";
import AddPatient from "./patients/AddPatient";
import AdminRoutes from "./admin/AdminRoutes";
import WithFacility from "./facilitySelect/WithFacility";
import { appPermissions } from "./permissions";
import Settings from "./Settings/Settings";
import { getAppInsights } from "./TelemetryService";

export const WHOAMI_QUERY = gql`
  query WhoAmI {
    whoami {
      id
      firstName
      middleName
      lastName
      suffix
      email
      isAdmin
      permissions
      roleDescription
      organization {
        name
        testingFacility {
          id
          name
        }
      }
    }
  }
`;

const App = () => {
  const appInsights = getAppInsights();
  const { setInitFacilities } = useFacilities();
  const { setInitialData } = useAppConfig();

  const { data, loading, error } = useQuery(WHOAMI_QUERY, {
    fetchPolicy: "no-cache",
  });

  useEffect(() => {
    if (!data) return;
    setInitFacilities(data.whoami.organization.testingFacility);
    setInitialData({
      user: {
        id: data.whoami.id,
        firstName: data.whoami.firstName,
        middleName: data.whoami.middleName,
        lastName: data.whoami.lastName,
        suffix: data.whoami.suffix,
        email: data.whoami.email,
        roleDescription: data.whoami.roleDescription,
        isAdmin: data.whoami.isAdmin,
        permissions: data.whoami.permissions,
      },
      organization: {
        name: data.whoami.organization?.name,
      },
      dataLoaded: true,
      activationToken: null,
    });
    // eslint-disable-next-line
  }, [data]);

  if (loading) {
    return <p>Loading account information...</p>;
  }

  if (error) {
    if (appInsights instanceof ApplicationInsights) {
      appInsights.trackException({ error });
    }
    return <p>Server connection error...</p>;
  }
  return (
    <PrimeErrorBoundary>
      <WithFacility>
        <div className="App">
          <div id="main-wrapper">
            <USAGovBanner />
            <Header />
            <Switch>
              <Route path="/login" component={LoginView} />
              <Route
                path="/queue"
                render={() => {
                  return <TestQueueContainer />;
                }}
              />
              <Route
                path="/"
                exact
                render={({ location }) => (
                  <Redirect
                    to={{
                      ...location,
                      pathname: data.whoami.isAdmin ? "/admin" : "/queue",
                    }}
                  />
                )}
              />
              <ProtectedRoute
                path="/results/:page?"
                render={({ match }: any) => {
                  return <TestResultsList page={match.params.page} />;
                }}
                requiredPermissions={appPermissions.results.canView}
                userPermissions={data.whoami.permissions}
              />
              <ProtectedRoute
                path={`/patients/:page?`}
                render={({ match }: any) => {
                  return <ManagePatientsContainer page={match.params.page} />;
                }}
                requiredPermissions={appPermissions.people.canView}
                userPermissions={data.whoami.permissions}
              />
              <ProtectedRoute
                path={`/patient/:patientId`}
                render={({ match }: any) => (
                  <EditPatientContainer patientId={match.params.patientId} />
                )}
                requiredPermissions={appPermissions.people.canEdit}
                userPermissions={data.whoami.permissions}
              />
              <ProtectedRoute
                path={`/add-patient/`}
                render={() => <AddPatient />}
                requiredPermissions={appPermissions.people.canEdit}
                userPermissions={data.whoami.permissions}
              />
              <ProtectedRoute
                path="/settings"
                component={Settings}
                requiredPermissions={appPermissions.settings.canView}
                userPermissions={data.whoami.permissions}
              />
              <Route
                path={"/admin"}
                render={({ match }) => (
                  <AdminRoutes match={match} isAdmin={data.whoami.isAdmin} />
                )}
              />
            </Switch>
            <ToastContainer
              autoClose={5000}
              closeButton={false}
              limit={2}
              position="bottom-center"
              hideProgressBar={true}
            />
          </div>
        </div>
      </WithFacility>
    </PrimeErrorBoundary>
  );
};

export default App;
