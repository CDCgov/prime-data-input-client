import { useReactiveVar } from "@apollo/client";

import { useDocumentTitle } from "../utils/hooks";
import { facilities } from "../../storage/store";

import TestQueue from "./TestQueue";

const TestQueueContainer = () => {
  useDocumentTitle("Conduct test");
  const { selectedFacility } = useReactiveVar<FacilitiesState>(facilities);
  const activeFacilityId = selectedFacility?.id;

  return !activeFacilityId ? (
    <div>No facility selected</div>
  ) : (
    <TestQueue activeFacilityId={activeFacilityId} />
  );
};

export default TestQueueContainer;
