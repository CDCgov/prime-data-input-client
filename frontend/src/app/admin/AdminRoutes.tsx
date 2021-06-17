import React from "react";
import { Redirect, Route } from "react-router-dom";

import DeviceTypeFormContainer from "./DeviceType/DeviceTypeFormContainer";
import Admin from "./Admin";
import PendingOrganizationsList from "./Organization/PendingOrganizationsList";

interface Props {
  match: { url: string };
  isAdmin: boolean;
}

const AdminRoutes: React.FC<Props> = ({ match, isAdmin }) => {
  if (!isAdmin) {
    return (
      <Route
        path={match.url}
        render={({ location }) => (
          <Redirect to={{ ...location, pathname: "/queue" }} />
        )}
      />
    );
  }
  return (
    <>
      <Route
        path={`${match.url}/pending-organizations`}
        render={() => <PendingOrganizationsList />}
      />
      <Route
        path={`${match.url}/create-device-type`}
        render={() => <DeviceTypeFormContainer />}
      />
      <Route path={"/admin"} exact={true} render={() => <Admin />} />
    </>
  );
};

export default AdminRoutes;
