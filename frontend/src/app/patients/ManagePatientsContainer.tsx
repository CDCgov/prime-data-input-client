import React from "react";
import { useReactiveVar } from "@apollo/client";

import { hasPermission, appPermissions } from "../permissions";
import { useDocumentTitle } from "../utils/hooks";
import { appConfig, facilities } from "../../storage/store";

import ManagePatients from "./ManagePatients";

const ManagePatientsContainer = (props: { page?: number }) => {
  useDocumentTitle("People");
  const { selectedFacility } = useReactiveVar<FacilitiesState>(facilities);
  const { user } = useReactiveVar<AppConfigState>(appConfig);

  const activeFacilityId = selectedFacility?.id;
  const isAdmin = user.isAdmin;

  const canEditUser = hasPermission(
    user.permissions,
    appPermissions.people.canEdit
  );
  const canDeleteUser = hasPermission(
    user.permissions,
    appPermissions.people.canDelete
  );
  if (!activeFacilityId) {
    return <div>"No facility selected"</div>;
  }
  return (
    <ManagePatients
      activeFacilityId={activeFacilityId}
      canEditUser={canEditUser}
      canDeleteUser={canDeleteUser}
      currentPage={props.page}
      isAdmin={isAdmin}
    />
  );
};

export default ManagePatientsContainer;
