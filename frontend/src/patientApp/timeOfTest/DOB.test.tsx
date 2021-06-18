import renderer from "react-test-renderer";
import { render, fireEvent, RenderResult } from "@testing-library/react";

import DOB from "./DOB";

const mockContainer = () => <DOB />;

jest.mock("../PxpApiService", () => {
  jest.fn();
});

describe("DOB", () => {
  describe("snapshot", () => {
    let component: renderer.ReactTestRenderer;
    beforeEach(() => {
      component = renderer.create(mockContainer());
    });
    it("matches", () => {
      expect(component.toJSON()).toMatchSnapshot();
    });
  });

  describe("behavior", () => {
    let utils: RenderResult;

    async function setup(utils: RenderResult) {
      const input = await utils.findByLabelText("Date of birth");
      const button = await utils.findByRole("button");
      return { input, button };
    }

    beforeEach(() => {
      utils = render(mockContainer());
    });

    it("validates birthdays", async () => {
      // GIVEN
      const { input, button } = await setup(utils);

      // WHEN
      fireEvent.change(input, { target: { value: "99-99-9999" } });
      fireEvent.submit(button);
      const error = await utils.findByRole("alert");

      // THEN
      expect(error.textContent).toEqual("Error: Enter your date of birth");
    });
  });
});
