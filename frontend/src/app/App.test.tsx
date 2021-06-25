import React from "react";
import TestRenderer, { act } from "react-test-renderer";
import { BrowserRouter as Router, Route } from "react-router-dom";
import { Provider } from "react-redux";
import createMockStore, { MockStoreEnhanced } from "redux-mock-store";
import { MockedProvider, MockedResponse } from "@apollo/client/testing";
import { ApolloError } from "@apollo/client";

import App, { WHOAMI_QUERY } from "./App";
import { queueQuery } from "./testQueue/TestQueue";

jest.mock("uuid");

const mockStore = createMockStore([]);
const mockDispatch = jest.fn();

const store = {
  dispatch: jest.fn(),
  organization: {
    name: "Organization Name",
  },
  user: {
    id: "05b2f71a-9392-442b-aab5-4eb550a864c0",
    firstName: "Bob",
    middleName: null,
    lastName: "Bobberoo",
    suffix: null,
    email: "bob@example.com",
    roleDescription: "Admin user",
    isAdmin: false,
    permissions: [
      "EDIT_PATIENT",
      "ARCHIVE_PATIENT",
      "READ_PATIENT_LIST",
      "EDIT_ORGANIZATION",
      "START_TEST",
      "EDIT_FACILITY",
      "ACCESS_ALL_FACILITIES",
      "READ_RESULT_LIST",
      "READ_ARCHIVED_PATIENT_LIST",
      "SUBMIT_TEST",
      "MANAGE_USERS",
      "SEARCH_PATIENTS",
      "UPDATE_TEST",
    ],
  },
  facilities: [
    {
      id: "fec4de56-f4cc-4c61-b3d5-76869ca71296",
      name: "Testing Site",
    },
  ],
  facility: {
    id: "fec4de56-f4cc-4c61-b3d5-76869ca71296",
    name: "Testing Site",
  },
};

store.dispatch = mockDispatch;

const WhoAmIQueryMock = {
  request: {
    query: WHOAMI_QUERY,
    fetchPolicy: "no-cache",
  },
  result: {
    data: {
      whoami: {
        id: "05b2f71a-9392-442b-aab5-4eb550a864c0",
        firstName: "Bob",
        middleName: null,
        lastName: "Bobberoo",
        suffix: null,
        email: "bob@example.com",
        isAdmin: false,
        permissions: [
          "EDIT_PATIENT",
          "ARCHIVE_PATIENT",
          "READ_PATIENT_LIST",
          "EDIT_ORGANIZATION",
          "START_TEST",
          "EDIT_FACILITY",
          "ACCESS_ALL_FACILITIES",
          "READ_RESULT_LIST",
          "READ_ARCHIVED_PATIENT_LIST",
          "SUBMIT_TEST",
          "MANAGE_USERS",
          "SEARCH_PATIENTS",
          "UPDATE_TEST",
        ],
        roleDescription: "Admin user",
        organization: {
          name: "Dis Organization",
          testingFacility: [
            {
              id: "fec4de56-f4cc-4c61-b3d5-76869ca71296",
              name: "Testing Site",
            },
          ],
        },
      },
    },
  },
};
const facilityQueryMock = {
  request: {
    query: queueQuery,
    fetchPolicy: "no-cache",
    variables: {
      facilityId: "fec4de56-f4cc-4c61-b3d5-76869ca71296",
    },
  },
  result: {
    data: {
      queue: [],
      organization: {
        testingFacility: [
          {
            id: "fec4de56-f4cc-4c61-b3d5-76869ca71296",
            deviceTypes: [
              {
                internalId: "d70bb3b8-96bd-40d9-a3ce-b266a7edb91d",
                name: "Quidel Sofia 2",
                model: "Sofia 2 SARS Antigen FIA",
                testLength: 15,
              },
              {
                internalId: "5e44dcef-8cc6-44f4-a200-a5b8169ab60a",
                name: "LumiraDX",
                model: "LumiraDx SARS-CoV-2 Ag Test*",
                testLength: 15,
              },
            ],
            defaultDeviceType: {
              internalId: "5e44dcef-8cc6-44f4-a200-a5b8169ab60a",
              name: "LumiraDX",
              model: "LumiraDx SARS-CoV-2 Ag Test*",
              testLength: 15,
            },
          },
        ],
      },
    },
  },
};
const WhoAmIErrorQueryMock = {
  request: {
    query: WHOAMI_QUERY,
    fetchPolicy: "no-cache",
  },
  error: new Error("Server connection error"),
};

const apolloError = new ApolloError({
  networkError: new Error("UNAUTHORIZED"),
  errorMessage: "UNAUTHORIZED",
});

const WhoAmIAuthErrorQueryMock = {
  request: {
    query: WHOAMI_QUERY,
    fetchPolicy: "no-cache",
  },
  error: apolloError,
};
const renderApp = (
  newStore: MockStoreEnhanced<unknown, {}>,
  queryMocks: MockedResponse[]
) => {
  return TestRenderer.create(
    <Provider store={newStore}>
      <MockedProvider mocks={queryMocks} addTypename={false}>
        <Router>
          <Route path="/" component={App} />
        </Router>
      </MockedProvider>
    </Provider>
  );
};

describe("App", () => {
  it("Render first loading screen", async () => {
    const mockedStore = mockStore({});
    const component = renderApp(mockedStore, [WhoAmIQueryMock]);
    const tree = component.toJSON();
    expect(tree.children).toContain("Loading account information...");

    expect(component).toMatchSnapshot();
  });

  it("Render facility loading", async () => {
    const mockedStore = mockStore({ ...store });

    const component = renderApp(mockedStore, [
      WhoAmIQueryMock,
      facilityQueryMock,
    ]);

    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 0));
    });

    const tree = component.toJSON();

    expect(tree.children).toContain("Loading facility information...");
    expect(component).toMatchSnapshot();
  });

  it("Render main screen", async () => {
    const mockedStore = mockStore({ ...store, dataLoaded: true });
    const component = renderApp(mockedStore, [
      WhoAmIQueryMock,
      facilityQueryMock,
    ]);
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 0));
    });
    expect(component).toMatchSnapshot();
  });
  it("should show error UI", async () => {
    const mockedStore = mockStore({ ...store, dataLoaded: true });
    const component = renderApp(mockedStore, [WhoAmIErrorQueryMock]);
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 0));
    });
    expect(component).toMatchSnapshot();
  });
  it("should show auth error UI", async () => {
    const mockedStore = mockStore({ ...store, dataLoaded: true });
    const component = renderApp(mockedStore, [WhoAmIAuthErrorQueryMock]);
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 0));
    });
    expect(component).toMatchSnapshot();
  });
});
