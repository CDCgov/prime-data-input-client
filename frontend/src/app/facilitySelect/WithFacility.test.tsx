import React from "react";
import renderer from "react-test-renderer";

import { facilitySample } from "../../config/constants";
import { appConfig, facilities } from "../../storage/store";

import WithFacility from "./WithFacility";

const mockHistoryPush = jest.fn();

jest.mock("react-router-dom", () => ({
  useHistory: () => ({
    push: mockHistoryPush,
  }),
}));

describe("WithFacility", () => {
  let component: any;
  beforeEach(() => {
    mockHistoryPush.mockClear();
    renderer.act(() => {});
  });

  describe("With zero facilities", () => {
    beforeEach(() => {
      renderer.act(() => {
        appConfig({
          ...appConfig(),
          dataLoaded: true,
          organization: { name: "Organization Name" },
          user: {
            ...appConfig().user,
            firstName: "Kim",
            lastName: "Mendoza",
          },
        });
        component = renderer.create(<WithFacility>App</WithFacility>);
      });
    });

    it("should notify user to contact an admin", () => {
      expect(component.toJSON()).toMatchSnapshot();
    });
  });

  describe("With one facility", () => {
    beforeEach(() => {
      renderer.act(() => {
        appConfig({
          ...appConfig(),
          dataLoaded: false,
          organization: { name: "Organization Name" },
          user: {
            ...appConfig().user,
            firstName: "Kim",
            lastName: "Mendoza",
          },
        });
        facilities({
          ...facilities(),
          availableFacilities: [
            { ...facilitySample, id: "1", name: "Facility 1" },
          ],
        });
        component = renderer.create(<WithFacility>App</WithFacility>);
      });
    });

    it("should render with a value", () => {
      expect(component.toJSON()).toMatchSnapshot();
    });
    it("should select the first facility", () => {
      expect(facilities().selectedFacility?.id).toEqual("1");
    });
    it("should push history", () => {
      expect(mockHistoryPush).toHaveBeenCalled();
    });
    it("should set the facility id search param", () => {
      expect(mockHistoryPush).toHaveBeenCalledWith({ search: "?facility=1" });
    });
  });

  describe("With two facilities", () => {
    beforeEach(() => {
      renderer.act(() => {
        appConfig({
          ...appConfig(),
          dataLoaded: true,
          organization: { name: "Organization Name" },
          user: {
            ...appConfig().user,
            firstName: "Kim",
            lastName: "Mendoza",
          },
        });
        facilities({
          ...facilities(),
          selectedFacility: { id: "", name: "", ...facilitySample },
          availableFacilities: [
            { ...facilitySample, id: "1", name: "Facility 1" },
            { ...facilitySample, id: "2", name: "Facility 2" },
          ],
        });

        component = renderer.create(<WithFacility>App</WithFacility>);
      });
    });

    it("should render with a value", () => {
      expect(component.toJSON()).toMatchSnapshot();
    });

    describe("On facility select", () => {
      beforeEach(() => {
        renderer.act(() => {
          component.root.findAllByType("button")[0].props.onClick();
        });
      });

      it("should select the first facility", () => {
        expect(facilities().selectedFacility?.id).toEqual("1");
      });
      it("should push history once", () => {
        expect(mockHistoryPush).toHaveBeenCalledTimes(1);
      });
      it("should set the facility id search param", () => {
        expect(mockHistoryPush).toHaveBeenCalledWith({ search: "?facility=1" });
      });
    });
  });
});
