import renderer from "react-test-renderer";
import { MockedProvider } from "@apollo/client/testing";

import { patient } from "../../storage/store";
import { patientSample } from "../../config/constants";

import PatientProfileContainer from "./PatientProfileContainer";

jest.mock("react-router-dom", () => ({
  useHistory: () => ({
    listen: jest.fn(),
    push: jest.fn(),
  }),
}));

describe("PatientProfileContainer", () => {
  beforeAll(() => {
    patient(patientSample);
  });
  it("snapshot", () => {
    const component = renderer.create(
      <MockedProvider mocks={[]} addTypename={false}>
        <PatientProfileContainer />
      </MockedProvider>
    );

    expect(component.toJSON()).toMatchSnapshot();
  });
});
