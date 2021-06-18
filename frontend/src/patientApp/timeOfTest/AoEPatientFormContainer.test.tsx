import renderer from "react-test-renderer";
import { MockedProvider } from "@apollo/client/testing";

import { appConfig, patient } from "../../storage/store";
import { patientSample } from "../../config/constants";

import AoEPatientFormContainer from "./AoEPatientFormContainer";

jest.mock("react-router-dom", () => ({
  useHistory: () => ({
    listen: jest.fn(),
    push: jest.fn(),
  }),
}));

describe("AoEPatientFormContainer", () => {
  beforeAll(() => {
    appConfig({ ...appConfig(), plid: "123" });
    patient({ ...patientSample });
    jest
      .useFakeTimers("modern")
      .setSystemTime(new Date("2399-01-01").getTime());
  });
  it("snapshot", () => {
    const component = renderer.create(
      <MockedProvider mocks={[]} addTypename={false}>
        <AoEPatientFormContainer page={""} />
      </MockedProvider>
    );

    expect(component.toJSON()).toMatchSnapshot();
  });
});
