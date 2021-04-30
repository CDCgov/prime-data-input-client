import { render, screen, fireEvent } from "@testing-library/react";

import { MfaSmsVerify } from "./MfaSmsVerify";

describe("Verify SMS MFA", () => {
  beforeEach(() => {
    render(<MfaSmsVerify />);
  });

  it("can enter a security code", () => {
    fireEvent.change(
      screen.getByLabelText("One-time security code", { exact: false }),
      {
        target: { value: "123" },
      }
    );
    fireEvent.click(screen.getByText("Verify"));
    expect(
      screen.queryByText("Security code is required")
    ).not.toBeInTheDocument();
  });

  it("requires a security code", () => {
    fireEvent.click(screen.getByText("Verify"));
    expect(screen.getByText("Security code is required")).toBeInTheDocument();
  });
});
