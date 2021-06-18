import { makeVar } from "@apollo/client";

export const facilities = makeVar<FacilitiesState>({
  selectedFacility: null,
  availableFacilities: [],
});

export const appConfig = makeVar<AppConfigState>({
  dataLoaded: false,
  user: {
    id: "",
    firstName: "",
    middleName: "",
    lastName: "",
    suffix: "",
    email: "",
    permissions: [],
    roleDescription: "",
    isAdmin: false,
  },
  organization: {
    name: "",
  },
  activationToken: null,
});

export const patient = makeVar<Patient | null>(null);

const store = {
  facilities,
  appConfig,
  patient,
};

export default store;
