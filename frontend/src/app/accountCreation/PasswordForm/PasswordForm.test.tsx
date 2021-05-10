import { render, screen, fireEvent } from "@testing-library/react";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router";
import createMockStore from "redux-mock-store";

import { PasswordForm } from "./PasswordForm";

const mockStore = createMockStore([]);

const store = mockStore({
  activationToken: "foo",
});

describe("PasswordForm", () => {
  beforeEach(() => {
    render(
      <MemoryRouter>
        <Provider store={store}>
          <PasswordForm />
        </Provider>
      </MemoryRouter>
    );
  });

  const strengthLabel = (label: string) => (content: string, element: any) => {
    return (
      element.tagName.toLowerCase() === "span" && content.startsWith(label)
    );
  };

  it("requires a password", () => {
    fireEvent.click(screen.getByText("Continue"));
    expect(
      screen.getByText(
        "Your password must have at least 8 characters, a lowercase letter, an uppercase letter, and a number"
      )
    ).toBeInTheDocument();
  });

  it("thinks 'foo' is a weak password", () => {
    fireEvent.change(screen.getByLabelText("Password *"), {
      target: { value: "foo" },
    });
    expect(screen.getByText(strengthLabel("Weak"))).toBeInTheDocument();
  });

  it("thinks 'fooBAR' is a weak password", () => {
    fireEvent.change(screen.getByLabelText("Password *"), {
      target: { value: "fooBAR" },
    });
    expect(screen.getByText(strengthLabel("Weak"))).toBeInTheDocument();
  });

  it("thinks 'fooB1' is an okay password", () => {
    fireEvent.change(screen.getByLabelText("Password *"), {
      target: { value: "fooB1" },
    });
    expect(screen.getByText(strengthLabel("Okay"))).toBeInTheDocument();
  });

  it("thinks 'fooBAR123!' is a good password", () => {
    fireEvent.change(screen.getByLabelText("Password *"), {
      target: { value: "fooBAR123!" },
    });
    expect(screen.getByText(strengthLabel("Good"))).toBeInTheDocument();
  });

  it("can type in the password confirmation", () => {
    fireEvent.change(screen.getByLabelText("Password *"), {
      target: { value: "fooBAR123!" },
    });
    fireEvent.change(
      screen.getByLabelText("Confirm password", { exact: false }),
      {
        target: { value: "fooBAR123!" },
      }
    );
    expect(screen.getByText(strengthLabel("Good"))).toBeInTheDocument();
  });

  it("requires passwords to match", () => {
    fireEvent.change(screen.getByLabelText("Password *"), {
      target: { value: "fooBAR123!" },
    });
    fireEvent.change(
      screen.getByLabelText("Confirm password", { exact: false }),
      {
        target: { value: "fooBAR123" },
      }
    );
    expect(screen.getByText(strengthLabel("Good"))).toBeInTheDocument();
    fireEvent.click(screen.getByText("Continue"));
    expect(screen.getByText("Passwords must match")).toBeInTheDocument();
  });
});
