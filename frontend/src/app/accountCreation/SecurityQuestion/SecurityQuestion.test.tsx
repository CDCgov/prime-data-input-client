import { render, screen, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { act } from "react-dom/test-utils";
import { MemoryRouter, Route } from "react-router";

import { SecurityQuestion } from "./SecurityQuestion";

jest.mock("../AccountCreationApiService", () => ({
  AccountCreationApi: {
    setRecoveryQuestion: (recoveryQuestion: string, recoveryAnswer: string) => {
      return new Promise((res, rej) => {
        if (recoveryAnswer === "Valid answer") {
          res("success");
        } else {
          rej("catastrophic failure");
        }
      });
    },
  },
}));

describe("SecurityQuestion", () => {
  beforeEach(() => {
    render(
      <MemoryRouter initialEntries={["/set-recovery-question"]}>
        <Route path="/set-recovery-question" component={SecurityQuestion} />
        <Route path="/mfa-select">
          <p>Recovery question set successfully.</p>
        </Route>
      </MemoryRouter>
    );
  });

  it("can choose a security question and type an answer", () => {
    userEvent.selectOptions(
      screen.getByLabelText("Security question", { exact: false }),
      ["In what city or town was your first job?"]
    );
    fireEvent.change(screen.getByLabelText("Answer", { exact: false }), {
      target: { value: "New York" },
    });
    expect(
      screen.getByText("In what city or town was your first job?")
    ).toBeInTheDocument();
  });

  it("requires a security question", () => {
    fireEvent.change(screen.getByLabelText("Answer", { exact: false }), {
      target: { value: "New York" },
    });
    fireEvent.click(screen.getByText("Continue"));
    expect(screen.getByText("Enter a security question")).toBeInTheDocument();
  });

  it("requires a security answer", () => {
    userEvent.selectOptions(
      screen.getByLabelText("Security question", { exact: false }),
      ["In what city or town was your first job?"]
    );
    fireEvent.click(screen.getByText("Continue"));
    expect(screen.getByText("Enter your answer")).toBeInTheDocument();
  });

  it("succeeds on submit w/ valid responses", async () => {
    userEvent.selectOptions(
      screen.getByLabelText("Security question", { exact: false }),
      ["In what city or town was your first job?"]
    );
    fireEvent.change(screen.getByLabelText("Answer", { exact: false }), {
      target: { value: "Valid answer" },
    });
    await act(async () => {
      await fireEvent.click(screen.getByText("Continue"));
    });
    expect(
      screen.getByText("Recovery question set successfully.")
    ).toBeInTheDocument();
  });

  it("fails on submit with invalid response and displays API error", async () => {
    userEvent.selectOptions(
      screen.getByLabelText("Security question", { exact: false }),
      ["In what city or town was your first job?"]
    );
    fireEvent.change(screen.getByLabelText("Answer", { exact: false }), {
      target: { value: "Invalid answer" },
    });
    await act(async () => {
      await fireEvent.click(screen.getByText("Continue"));
    });
    expect(
      screen.getByText("API Error: catastrophic failure")
    ).toBeInTheDocument();
  });
});
