import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route } from "react-router";
import faker from "faker";

import { SelfRegistration } from "./SelfRegistration";

const VALID_LINK = "foo-facility";

jest.mock("../PxpApiService", () => ({
  PxpApi: {
    getEntityName: (link: string) => {
      return new Promise((res, rej) => {
        if (link === VALID_LINK) {
          res("Foo Facility");
        } else {
          rej();
        }
      });
    },
    selfRegister: () => {
      return Promise.resolve();
    },
  },
}));

const originalConsoleError = console.error;

describe("SelfRegistration", () => {
  beforeEach(() => {
    // For smartystreets failures
    console.error = jest.fn();
  });
  afterEach(() => {
    console.error = originalConsoleError;
  });

  it("Renders a 404 page for a bad link", async () => {
    await waitFor(() => {
      render(
        <MemoryRouter initialEntries={["/register/some-bad-link"]}>
          <Route
            exact
            path="/register/:registrationLink"
            component={SelfRegistration}
          />
        </MemoryRouter>
      );
    });

    expect(screen.queryByText("Page not found")).toBeInTheDocument();
  });

  it("Allows for user to register through link", async () => {
    await waitFor(() => {
      render(
        <MemoryRouter initialEntries={[`/register/${VALID_LINK}`]}>
          <Route
            exact
            path="/register/:registrationLink"
            component={SelfRegistration}
          />
        </MemoryRouter>
      );
    });
    expect(screen.queryByText("Foo Facility")).toBeInTheDocument();
    fireEvent.click(screen.getByText("I agree"));
    expect(screen.getByText("General information")).toBeInTheDocument();
    Object.entries(filledForm).forEach(([field, value]) => {
      fireEvent.change(screen.getByLabelText(field, { exact: false }), {
        target: { value },
      });
    });
    screen.getAllByLabelText("No").forEach(fireEvent.click);
    fireEvent.click(screen.getByLabelText("Mobile"));
    fireEvent.click(screen.getByText("Submit"));
    await screen.findByText("Address validation");
    fireEvent.click(screen.getByLabelText("Use address", { exact: false }));
    fireEvent.click(screen.getByText("Save changes"));
    expect(
      await screen.findByText("Registration complete")
    ).toBeInTheDocument();
  });
});

const filledForm = {
  "First name": faker.name.firstName(),
  "Middle name": faker.name.middleName(),
  "Last name": faker.name.lastName(),
  "Date of birth": "1970-09-22",
  "Primary phone number": "7038675309",
  "Street address 1": faker.address.streetAddress(),
  State: "MD",
  "Zip code": "12345",
};
