import renderer from "react-test-renderer";
import { render, screen, cleanup } from "@testing-library/react";
import { MockedProvider } from "@apollo/client/testing";

import { appConfig, facilities, patient } from "../../storage/store";
import { facilitySample, patientSample } from "../../config/constants";

import PatientFormContainer from "./PatientFormContainer";

jest.mock("../..//app/commonComponents/ComboBox", () => () => <></>);

jest.mock("react-router-dom", () => ({
  Prompt: (props: any) => <></>,
  useHistory: () => ({
    listen: jest.fn(),
    push: jest.fn(),
  }),
}));

describe("PatientFormContainer", () => {
  beforeAll(() => {
    appConfig({ ...appConfig(), plid: "foo" });
    facilities({
      selectedFacility: { ...facilitySample, id: "123", name: "Sample" },
      availableFacilities: [{ ...facilitySample, id: "123", name: "Sample" }],
    });
    patient(patientSample);
  });
  afterEach(cleanup);

  it("snapshot", () => {
    const component = renderer.create(
      <MockedProvider mocks={[]} addTypename={false}>
        <PatientFormContainer />
      </MockedProvider>
    );

    expect(component.toJSON()).toMatchSnapshot();
  });
  describe("testing-library/react renderer", () => {
    window.scrollTo = jest.fn();

    beforeEach(() => {
      render(
        <MockedProvider mocks={[]} addTypename={false}>
          <PatientFormContainer />
        </MockedProvider>
      );
    });
    it("shows required field instruction once", () => {
      expect(
        screen.getByText("Required fields are marked with an asterisk", {
          exact: false,
        })
      ).toBeInTheDocument();
    });
    it("doesn't show a facility input", () => {
      expect(
        screen.queryByLabelText("Facility", {
          exact: false,
        }) as HTMLSelectElement
      ).toBeNull();
    });
  });
});
