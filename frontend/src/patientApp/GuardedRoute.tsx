import { Route, Redirect } from "react-router-dom";

import { useAppConfig } from "../hooks/useAppConfig";

const GuardedRoute = ({ component: Component, auth, ...rest }: any) => {
  const {
    config: { plid },
  } = useAppConfig();
  return (
    <Route
      {...rest}
      render={(props) =>
        auth === true ? (
          <Component {...props} />
        ) : (
          <Redirect
            to={{
              pathname: "/",
              search: `?plid=${plid}`,
            }}
          />
        )
      }
    />
  );
};

export default GuardedRoute;
