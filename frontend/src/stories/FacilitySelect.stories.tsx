import { Story, Meta } from "@storybook/react";

import FacilitySelect, {
  FacilitySelectProps,
} from "../app/facilitySelect/FacilitySelect";

export default {
  title: "App/Facility select",
  component: FacilitySelect,
  argTypes: { setActiveFacility: { action: "clicked" } },
} as Meta;

const Template = (): Story<FacilitySelectProps> => (args) => (
  <FacilitySelect {...args} />
);

const createTestFacility = (name: string, id: string): Facility => ({
  id,
  cliaNumber: "",
  city: "",
  defaultDevice: "",
  deviceTypes: [],
  email: "",
  name,
  zipCode: "",
  streetTwo: "",
  phone: "",
  orderingProvider: {
    NPI: "",
    city: "",
    firstName: "",
    lastName: "",
    middleName: "",
    phone: "",
    state: "",
    street: "",
    streetTwo: "",
    suffix: "",
    zipCode: "",
  },
  street: "",
  state: "",
});

export const NoFacilities = Template();
NoFacilities.args = {
  facilities: [],
};

export const MultipleFacilities = Template();
MultipleFacilities.args = {
  facilities: [
    createTestFacility("MSU Lab", "abc"),
    createTestFacility("West Lake", "def"),
  ],
};
