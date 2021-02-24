import React, { useEffect } from "react";
import { gql, useQuery } from "@apollo/client";
import { ToastContainer } from "react-toastify";
import { useDispatch, connect } from "react-redux";
import "react-toastify/dist/ReactToastify.css";
import { Redirect, Route, Switch } from "react-router-dom";
import { AppInsightsContext } from "@microsoft/applicationinsights-react-js";
import { reactPlugin } from "./AppInsights";
import ProtectedRoute from "./commonComponents/ProtectedRoute";

import PrimeErrorBoundary from "./PrimeErrorBoundary";
import Header from "./commonComponents/Header";

import USAGovBanner from "./commonComponents/USAGovBanner";
import LoginView from "./LoginView";
import { setInitialState } from "./store";
import TestResultsList from "./testResults/TestResultsList";
import TestQueueContainer from "./testQueue/TestQueueContainer";
import ManagePatientsContainer from "./patients/ManagePatientsContainer";
import EditPatientContainer from "./patients/EditPatientContainer";
import AddPatient from "./patients/AddPatient";
import AdminRoutes from "./admin/AdminRoutes";
import WithFacility from "./facilitySelect/WithFacility";
import { appPermissions } from "./permissions";
import Settings from "./Settings/Settings";

const WHOAMI_QUERY = gql`
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
  const dispatch = useDispatch();
  const { data, loading, error } = useQuery(WHOAMI_QUERY, {
    fetchPolicy: "no-cache",
  });

  useEffect(() => {
    if (!data) return;

    dispatch(
      setInitialState({
        organization: {
          name: data.whoami.organization?.name,
        },
        facilities: data.whoami.organization?.testingFacility,
        facility: null,
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
      })
    );
    // eslint-disable-next-line
  }, [data]);

  if (loading) {
    return <p>Loading account information...</p>;
  }

  if (error) {
    throw error;
  }

  return (
    <AppInsightsContext.Provider value={reactPlugin}>
      <PrimeErrorBoundary
        onError={(error: any) => (
          <div>
            <h1> There was an error. Please try refreshing</h1>
            <pre> {JSON.stringify(error, null, 2)} </pre>
          </div>
        )}
      >
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
                  path="/results"
                  render={() => {
                    return <TestResultsList />;
                  }}
                  requiredPermissions={appPermissions.results.canView}
                  userPermissions={data.whoami.permissions}
                />
                <ProtectedRoute
                  path={`/patients`}
                  render={() => {
                    return <ManagePatientsContainer />;
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
    </AppInsightsContext.Provider>
  );
};

export default connect()(App);
