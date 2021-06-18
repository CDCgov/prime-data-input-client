import { render, screen, fireEvent, within } from "@testing-library/react";
import { MockedProvider } from "@apollo/client/testing";
import { act } from "react-dom/test-utils";
import { MemoryRouter, Route } from "react-router";

import { facilities } from "../../storage/store";
import { facilitySample } from "../../config/constants";

import AddPatient, { ADD_PATIENT } from "./AddPatient";

jest.mock("../utils/smartyStreets", () => ({
  getBestSuggestion: jest.fn(),
  suggestionIsCloseEnough: () => false,
}));

const fillOutForm = (
  inputs: { [label: string]: string },
  inputGroups: {
    [legend: string]: { label: string; value: string; exact?: boolean };
  }
) => {
  Object.entries(inputs).forEach(([label, value]) => {
    fireEvent.change(
      screen.getByLabelText(label, {
        exact: false,
      }),
      {
        target: { value },
      }
    );
  });
  Object.entries(inputGroups).forEach(([legend, { label, value, exact }]) => {
    const fieldset = screen
      .getByText(legend, {
        exact: false,
      })
      .closest("fieldset");
    if (fieldset === null) {
      throw Error(`Unable to corresponding fieldset for ${legend}`);
    }
    fireEvent.click(
      within(fieldset).getByLabelText(label, {
        exact: exact || false,
      }),
      {
        target: { value },
      }
    );
  });
};

describe("AddPatient", () => {
  describe("No facility selected", () => {
    beforeEach(() => {
      render(
        <MemoryRouter>
          <MockedProvider mocks={[]} addTypename={false}>
            <AddPatient />
          </MockedProvider>
        </MemoryRouter>
      );
    });
    it("does not show the form title", () => {
      expect(
        screen.queryByText("Add New Person", {
          exact: false,
        })
      ).toBeNull();
    });
    it("shows a 'No facility selected' message", async () => {
      expect(
        await screen.getByText("No facility selected", {
          exact: false,
        })
      ).toBeInTheDocument();
    });
  });

  describe("Facility selected", () => {
    const mockFacilityID = "b0d2041f-93c9-4192-b19a-dd99c0044a7e";
    beforeAll(() => {
      facilities({
        selectedFacility: {
          ...facilitySample,
          id: mockFacilityID,
          name: "123",
        },
        availableFacilities: [
          { ...facilitySample, id: mockFacilityID, name: "123" },
        ],
      });
    });
    beforeEach(() => {
      const mocks = [
        {
          request: {
            query: ADD_PATIENT,
            variables: {
              firstName: "Alice",
              middleName: null,
              lastName: "Hamilton",
              lookupId: null,
              birthDate: "1970-09-22",
              street: "25 Shattuck St",
              streetTwo: null,
              city: "Boston",
              state: "MA",
              zipCode: "02115",
              telephone: null,
              phoneNumbers: [
                {
                  type: "MOBILE",
                  number: "617-432-1000",
                },
              ],
              role: null,
              email: null,
              county: "",
              race: null,
              ethnicity: null,
              gender: null,
              residentCongregateSetting: false,
              employedInHealthcare: true,
              facilityId: mockFacilityID,
              preferredLanguage: null,
            },
          },
          result: {
            data: {
              internalId: "153f661f-b6ea-4711-b9ab-487b95198cce",
            },
          },
        },
        {
          request: {
            query: ADD_PATIENT,
            variables: {
              firstName: "Alice",
              middleName: null,
              lastName: "Hamilton",
              lookupId: "student-123",
              birthDate: "1970-09-22",
              street: "25 Shattuck St",
              streetTwo: null,
              city: "Boston",
              state: "MA",
              zipCode: "02115",
              telephone: null,
              phoneNumbers: [
                {
                  type: "MOBILE",
                  number: "617-432-1000",
                },
              ],
              role: "STUDENT",
              email: null,
              county: "",
              race: null,
              ethnicity: null,
              gender: null,
              residentCongregateSetting: false,
              employedInHealthcare: true,
              facilityId: mockFacilityID,
              preferredLanguage: null,
            },
          },
          result: {
            data: {
              internalId: "153f661f-b6ea-4711-b9ab-487b95198cce",
            },
          },
        },
      ];
      render(
        <MockedProvider mocks={mocks} addTypename={false}>
          <MemoryRouter initialEntries={["/add-patient/"]}>
            <Route component={AddPatient} path={"/add-patient/"} />
            <Route path={"/patients"} render={() => <p>Patients!</p>} />
          </MemoryRouter>
        </MockedProvider>
      );
    });
    it("shows the form title", async () => {
      await act(async () => {
        await new Promise((resolve) => setTimeout(resolve, 500));
      });
      expect(
        await screen.queryAllByText("Add New Person", { exact: false })[0]
      ).toBeInTheDocument();
    });

    describe("All required fields entered", () => {
      beforeEach(async () => {
        fillOutForm(
          {
            "First Name": "Alice",
            "Last Name": "Hamilton",
            Facility: mockFacilityID,
            "Date of birth": "1970-09-22",
            "Primary phone number": "617-432-1000",
            "Street address 1": "25 Shattuck St",
            City: "Boston",
            State: "MA",
            "Zip code": "02115",
          },
          {
            "Phone type": {
              label: "Mobile",
              value: "MOBILE",
              exact: true,
            },
            "Are you a resident in a congregate living setting": {
              label: "No",
              value: "No",
              exact: true,
            },
            "Are you a health care worker": {
              label: "Yes",
              value: "Yes",
              exact: true,
            },
          }
        );
        await act(async () => {
          fireEvent.click(
            screen.queryAllByText("Save", {
              exact: false,
            })[0]
          );
        });
      });
      it("show the address validation modal", async () => {
        await screen.findByText(`Address Validation`, {
          exact: false,
        });
      });
      describe("Submitting Address Verification", () => {
        beforeEach(async () => {
          await act(async () => {
            await new Promise((resolve) => setTimeout(resolve, 500));
          });
          const modal = screen.getByRole("dialog", {
            exact: false,
          });

          fireEvent.click(
            within(modal).getByLabelText("Use address as entered", {
              exact: false,
            }),
            {
              target: { value: "userAddress" },
            }
          );
          await act(async () => {
            fireEvent.click(
              within(modal).getByText("Save changes", {
                exact: false,
              })
            );
          });
        });
        it("redirects to the person tab", () => {
          expect(screen.getByText("Patients!")).toBeInTheDocument();
        });
      });
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
      it("defaults to no selection", () => {
        expect(facilityInput.value).toBe("");
      });
      it("updates its selection on change", async () => {
        fireEvent.change(facilityInput, {
          target: { value: mockFacilityID },
        });
        expect(facilityInput.value).toBe(mockFacilityID);
      });
    });

    describe("With student ID", () => {
      it("allows student ID to be entered", async () => {
        fillOutForm(
          {
            "First Name": "Alice",
            "Last Name": "Hamilton",
            Facility: mockFacilityID,
            "Date of birth": "1970-09-22",
            "Primary phone number": "617-432-1000",
            "Street address 1": "25 Shattuck St",
            City: "Boston",
            State: "MA",
            "Zip code": "02115",
          },
          {
            "Phone type": {
              label: "Mobile",
              value: "MOBILE",
              exact: true,
            },
            "Are you a resident in a congregate living setting": {
              label: "No",
              value: "No",
              exact: true,
            },
            "Are you a health care worker": {
              label: "Yes",
              value: "Yes",
              exact: true,
            },
          }
        );

        fireEvent.change(screen.getByLabelText("Role"), {
          target: { value: "STUDENT" },
        });
        expect(await screen.findByText("Student ID")).toBeInTheDocument();
      });
    });
  });
});
