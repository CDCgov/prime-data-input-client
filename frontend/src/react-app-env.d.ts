/// <reference types="react-scripts" />

interface User {
  id: string;
  firstName: string;
  middleName: string;
  lastName: string;
  suffix: string;
  email: string;
  type?: UserType; // TODO: remove optional prop
  permissions?: UserPermission[]; // TODO: remove optional prop
  roleDescription: RoleDescription;
  isAdmin: boolean;
}
