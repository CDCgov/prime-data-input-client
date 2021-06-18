import { useReactiveVar } from "@apollo/client";

import { facilities as facilitiesVar } from "../storage/store";

export const useFacilities = () => {
  const facilities = useReactiveVar(facilitiesVar);

  const setCurrentFacility = (facility: Facility) => {
    facilitiesVar({ ...facilitiesVar(), selectedFacility: facility });
  };

  const setInitFacilities = (data: Facility[]) => {
    if (data.length) {
      if (data.length > 1) {
        facilitiesVar({ selectedFacility: null, availableFacilities: data });
        return;
      }
      facilitiesVar({ selectedFacility: data[0], availableFacilities: data });
    }
  };

  return {
    facilities,
    setCurrentFacility,
    setInitFacilities,
  };
};
