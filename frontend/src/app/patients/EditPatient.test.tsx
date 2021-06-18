import {
  render,
  screen,
  fireEvent,
  cleanup,
  within,
} from "@testing-library/react";
import { MockedProvider } from "@apollo/client/testing";
import { act } from "react-dom/test-utils";
import { MemoryRouter } from "react-router";

import { facilities } from "../../storage/store";
import { facilitySample } from "../../config/constants";

import EditPatient, { GET_PATIENT } from "./EditPatient";

jest.mock("../commonComponents/ComboBox", () => () => <></>);

describe("EditPatient", () => {
  afterEach(cleanup);

  const mockFacilityID = "b0d2041f-93c9-4192-b19a-dd99c0044a7e";
  const mockPatientID = "555e8a40-0f95-458e-a038-6b500a0fc2ad";
  facilities({
    ...facilities(),
    availableFacilities: [
      { ...facilitySample, id: mockFacilityID, name: "123" },
    ],
  });
  describe("Waiting for network response", () => {
    beforeEach(() => {
      render(
        <MemoryRouter>
          <MockedProvider mocks={[]} addTypename={false}>
            <EditPatient
              facilityId={mockFacilityID}
              patientId={mockPatientID}
            />
          </MockedProvider>
        </MemoryRouter>
      );
    });
    it("shows loading text", async () => {
      expect(
        await screen.queryAllByText("loading...", { exact: false })[0]
      ).toBeInTheDocument();
    });
  });

  describe("phone number input", () => {
    beforeEach(async () => {
      const mocks = [
        {
          request: {
            query: GET_PATIENT,
            variables: {
              id: mockPatientID,
            },
          },
          result: {
            data: {
              patient: {
                firstName: "Eugenia",
                middleName: null,
                lastName: "Franecki",
                birthDate: "1939-10-11",
                street: "736 Jackson PI NW",
                streetTwo: "DC",
                city: null,
                state: "DC",
                zipCode: null,
                telephone: "(634) 397-4114",
                phoneNumbers: [
                  {
                    type: "LANDLINE",
                    number: "(631) 867-5309",
                  },
                  {
                    type: "MOBILE",
                    number: "(634) 397-4114",
                  },
                ],
                role: "UNKNOWN",
                email: "foo@bar.com",
                county: null,
                race: null,
                ethnicity: null,
                gender: null,
                residentCongregateSetting: true,
                employedInHealthcare: true,
                facility: null,
              },
            },
          },
        },
      ];

      render(
        <MemoryRouter>
          <MockedProvider mocks={mocks} addTypename={false}>
            <EditPatient
              facilityId={mockFacilityID}
              patientId={mockPatientID}
            />
          </MockedProvider>
        </MemoryRouter>
      );

      await act(async () => {
        await screen.findAllByText("Franecki, Eugenia", { exact: false });
      });
    });

    it("populates primary phone number field with patient `telephone`", () => {
      const legend = "Primary phone number";
      const input = screen.getByLabelText(legend, { exact: false });

      if (input === null) {
        throw Error(`Unable to corresponding input for ${legend}`);
      }

      // In the GraphQL response, this was the second entry in the
      // phone numbers array, but the array should be sorted to place the
      // value matching `patient.telephone` first
      expect(input.value).toBe("(634) 397-4114");
    });
  });

  describe("facility select input", () => {
    let component: any;
    beforeEach(async () => {
      const mocks = [
        {
          request: {
            query: GET_PATIENT,
            variables: {
              id: mockPatientID,
            },
          },
          result: {
            data: {
              patient: {
                firstName: "Eugenia",
                middleName: null,
                lastName: "Franecki",
                birthDate: "1939-10-11",
                street: "736 Jackson PI NW",
                streetTwo: "DC",
                city: null,
                state: "DC",
                zipCode: null,
                telephone: "(634) 397-4114",
                phoneNumbers: [
                  {
                    type: "MOBILE",
                    number: "(634) 397-4114",
                  },
                ],
                role: "UNKNOWN",
                email: "foo@bar.com",
                county: null,
                race: null,
                ethnicity: null,
                gender: null,
                residentCongregateSetting: true,
                employedInHealthcare: true,
                facility: null,
              },
            },
          },
        },
      ];

      component = render(
        <MemoryRouter>
          <MockedProvider mocks={mocks} addTypename={false}>
            <EditPatient
              facilityId={mockFacilityID}
              patientId={mockPatientID}
            />
          </MockedProvider>
        </MemoryRouter>
      );
      await act(async () => {
        await screen.findAllByText("Franecki, Eugenia", { exact: false });
      });
    });

    it("shows the form title", () => {
      expect(
        screen.queryAllByText("Franecki, Eugenia", { exact: false })[0]
      ).toBeInTheDocument();
    });

    it("matches screenshot", () => {
      expect(component).toMatchSnapshot();
    });

    describe("facility select input", () => {
      let facilityInput: HTMLSelectElement;
      beforeEach(() => {
        facilityInput = screen.getByLabelText("Facility", {
          exact: false,
        }) as HTMLSelectElement;
      });
      it("is present in the form", () => {
        expect(facilityInput).toBeInTheDocument();
      });
      it("patient with null facility prop maps to all facilities", () => {
        expect(facilityInput.value).toBe("~~ALL-FACILITIES~~");
      });
      it("updates its selection on change", async () => {
        fireEvent.change(facilityInput, {
          target: { value: mockFacilityID },
        });
        expect(facilityInput.value).toBe(mockFacilityID);
      });
    });
  });
  describe("non-answer and unknown options", () => {
    beforeEach(async () => {
      const mocks = [
        {
          request: {
            query: GET_PATIENT,
            variables: {
              id: mockPatientID,
            },
          },
          result: {
            data: {
              patient: {
                firstName: "Eugenia",
                middleName: null,
                lastName: "Franecki",
                birthDate: "1939-10-11",
                street: "736 Jackson PI NW",
                streetTwo: "DC",
                city: null,
                state: "DC",
                zipCode: null,
                telephone: "(634) 397-4114",
                phoneNumbers: [
                  {
                    type: "MOBILE",
                    number: "(634) 397-4114",
                  },
                ],
                role: "UNKNOWN",
                email: "foo@bar.com",
                county: null,
                race: "refused",
                ethnicity: "refused",
                gender: "refused",
                residentCongregateSetting: null,
                employedInHealthcare: null,
                facility: null,
              },
            },
          },
        },
      ];

      render(
        <MemoryRouter>
          <MockedProvider mocks={mocks} addTypename={false}>
            <EditPatient
              facilityId={mockFacilityID}
              patientId={mockPatientID}
            />
          </MockedProvider>
        </MemoryRouter>
      );
      await act(async () => {
        await screen.findAllByText("Franecki, Eugenia", { exact: false });
      });
    });

    it("shows prefer not to answer options", () => {
      ["Race", "Are you Hispanic or Latino?", "Biological sex"].forEach(
        (legend) => {
          const fieldset = screen.getByText(legend).closest("fieldset");
          if (fieldset === null) {
            throw Error(`Unable to corresponding fieldset for ${legend}`);
          }
          const option = within(fieldset).getByLabelText(
            "Prefer not to answer"
          );
          expect(option).toBeChecked();
        }
      );
    });
    it("shows unknown answers", () => {
      ["congregate", "health care"].forEach((legend) => {
        const fieldset = screen
          .getByText(legend, { exact: false })
          .closest("fieldset");
        if (fieldset === null) {
          throw Error(`Unable to corresponding fieldset for ${legend}`);
        }
        const option = within(fieldset).getByLabelText("Unknown");
        expect(option).toBeChecked();
      });
    });
  });
});
