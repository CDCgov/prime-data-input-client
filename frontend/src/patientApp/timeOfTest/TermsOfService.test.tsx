import renderer from "react-test-renderer";
import { MockedProvider } from "@apollo/client/testing";
import { Router } from "react-router-dom";
import { createMemoryHistory } from "history";

import TermsOfService from "./TermsOfService";

describe("TermsOfService", () => {
  it("snapshot", () => {
    const component = renderer.create(
      <Router history={createMemoryHistory()}>
        <MockedProvider mocks={[]} addTypename={false}>
          <TermsOfService />
        </MockedProvider>
      </Router>
    );

    expect(component.toJSON()).toMatchSnapshot();
  });
});
