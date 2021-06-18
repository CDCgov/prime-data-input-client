import React from "react";
import renderer from "react-test-renderer";

import { appConfig } from "../../storage/store";

import FacilitySelect from "./FacilitySelect";

describe("FacilitySelect", () => {
  let component: any;
  const mockSetActiveFacility = jest.fn();

  beforeEach(() => {
    appConfig({
      ...appConfig(),
      organization: { name: "Organization Name" },
      user: {
        ...appConfig().user,
        firstName: "Kim",
        lastName: "Mendoza",
      },
    });

    component = renderer.create(
      <FacilitySelect
        facilities={
          [
            { id: "1", name: "Facility 1" },
            { id: "2", name: "Facility 2" },
          ] as any
        }
        setActiveFacility={mockSetActiveFacility}
      />
    );
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
    it("should call setActiveFacility once", () => {
      expect(mockSetActiveFacility).toHaveBeenCalledTimes(1);
    });
    it("should select the first facility", () => {
      expect(mockSetActiveFacility).toHaveBeenCalledWith({
        id: "1",
        name: "Facility 1",
      });
    });
  });
});
