import { toast } from "react-toastify";
import { ApolloClient, ApolloLink, concat } from "@apollo/client";
import { createUploadLink } from "apollo-upload-client";
import { ErrorResponse, onError } from "@apollo/client/link/error";
import { ApplicationInsights } from "@microsoft/applicationinsights-web";

import { showError } from "../app/utils";
import { getAppInsights } from "../app/TelemetryService";
import cache from "../storage/cache";

const appInsights: null | ApplicationInsights = getAppInsights();

const httpLink = createUploadLink({
  uri: `${process.env.REACT_APP_BACKEND_URL}/graphql`,
});

const authMiddleware = new ApolloLink((operation, forward) => {
  operation.setContext({
    headers: {
      "Access-Control-Request-Headers": "Authorization",
      Authorization: `Bearer ${localStorage.getItem("access_token")}`,
    },
  });
  return forward(operation);
});

const logoutLink = onError(({ networkError, graphQLErrors }: ErrorResponse) => {
  if (networkError && process.env.REACT_APP_BASE_URL) {
    if ("statusCode" in networkError && networkError.statusCode === 401) {
      console.warn("[UNATHORIZED_ACCESS] !!");
      console.warn("redirect-to:", process.env.REACT_APP_BASE_URL);
      window.location.replace(process.env.REACT_APP_BASE_URL);
    } else {
      if (appInsights instanceof ApplicationInsights) {
        appInsights.trackException({ error: networkError });
      }
      showError(
        toast,
        "Please check for errors and try again",
        networkError.message
      );
    }
  }
  if (graphQLErrors) {
    const messages = graphQLErrors.map(({ message, locations, path }) => {
      console.error(
        `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}`
      );
      return message;
    });
    showError(
      toast,
      "Please check for errors and try again",
      messages.join(" ")
    );
    console.error("graphql error", graphQLErrors);
  }
});

const client = new ApolloClient({
  cache,
  link: logoutLink.concat(concat(authMiddleware, httpLink)),
  connectToDevTools: true,
});

export default client;
