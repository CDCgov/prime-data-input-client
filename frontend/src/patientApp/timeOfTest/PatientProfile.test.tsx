import renderer from "react-test-renderer";
import { MemoryRouter } from "react-router";

import PatientProfile from "./PatientProfile";

const mockContainer = (patient: any) => (
  <MemoryRouter>
    <PatientProfile patient={patient} />
  </MemoryRouter>
);

describe("PatientProfile", () => {
  it("snapshot", () => {
    const patient = {
      firstName: "Jane",
      lastName: "Doe",
      street: "24 Dreary Ln",
    };

    const component = renderer.create(mockContainer(patient));
    expect(component.toJSON()).toMatchSnapshot();
  });
  it("should redirect to '/' if no patient", () => {
    renderer.create(mockContainer(null));
    // eslint-disable-next-line no-restricted-globals
    expect(location.pathname).toEqual("/");
  });
});
