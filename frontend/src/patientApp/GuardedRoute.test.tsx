import { render } from "@testing-library/react";
import { MemoryRouter, Route } from "react-router";

import { appConfig } from "../storage/store";

import GuardedRoute from "./GuardedRoute";
import TermsOfService from "./timeOfTest/TermsOfService";

const mockContainer = (auth: boolean) => (
  <MemoryRouter initialEntries={["/terms-of-service"]}>
    <GuardedRoute
      auth={auth}
      component={TermsOfService}
      path="/terms-of-service"
    />
    <Route exact path="/">
      <p>This is some very specific text</p>
    </Route>
  </MemoryRouter>
);

describe("GuardedRoute", () => {
  beforeEach(() => {
    appConfig({ ...appConfig(), plid: "foo" });
  });
  it("should not redirect to '/' if auth is true", () => {
    const { getByText } = render(mockContainer(true));
    expect(getByText("Terms of service")).toBeInTheDocument();
  });
  it("should redirect to '/' if auth is false", () => {
    const { getByText } = render(mockContainer(false));
    expect(getByText("This is some very specific text")).toBeInTheDocument();
  });
});
