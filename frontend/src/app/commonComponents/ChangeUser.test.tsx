import { render, screen } from "@testing-library/react";

import ChangeUser from "./ChangeUser";

describe("ChangeUser", () => {
  beforeEach(() => {
    render(<ChangeUser />);
  });
  it("renders nothing", () => {
    expect(
      screen.queryByText("Login as", { exact: false })
    ).not.toBeInTheDocument();
  });

  describe("when in development", () => {
    const OLD_ENV = process.env;
    beforeAll(() => {
      jest.resetModules(); // Most important - it clears the cache
      process.env = { ...OLD_ENV }; // Make a copy
      process.env.NODE_ENV = "development";
    });
    afterAll(() => {
      process.env = OLD_ENV; // Restore old environment
    });
    it("renders 4 login links", () => {
      expect(screen.getAllByText("Login as", { exact: false }).length).toBe(4);
    });
  });
});
