import React, { useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useSelector } from "react-redux";

import Button from "../../commonComponents/Button";
import TextInput from "../../commonComponents/TextInput";
import Dropdown from "../../commonComponents/Dropdown";
import { Role } from "../../permissions";
import { RootState } from "../../store";

import { SettingsUser, UserFacilitySetting } from "./ManageUsersContainer";
import "./ManageUsers.scss";
import UserFacilitiesSettingsForm from "./UserFacilitiesSettingsForm";
import { UpdateUser } from "./ManageUsers";

interface Props {
  onClose: () => void;
  onSubmit: (newUserInvite: Partial<SettingsUser>) => void;
  isUpdating: boolean;
}

const initialFormState: Partial<SettingsUser> = {};

// TODO: right now, all newly invited users are of role USER. This is a future feature
const ROLE_OPTIONS: { value: Role; label: string }[] = [
  {
    value: "ENTRY_ONLY",
    label: "Entry only (conduct tests)",
  },
  {
    value: "USER",
    label: "Standard user (manage results and profiles)",
  },
  {
    value: "ADMIN",
    label: "Admin (full permissions)",
  },
];

const CreateUserForm: React.FC<Props> = ({ onClose, onSubmit, isUpdating }) => {
  const facilities = useSelector<RootState, UserFacilitySetting[]>(
    (state) => state.facilities
  );
  const [newUser, updateNewUser] = useState(initialFormState);
  const onChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    updateNewUser({ ...newUser, [e.target.name]: e.target.value });
  };

  const updateUser: UpdateUser = (key, value) => {
    updateNewUser({
      ...newUser,
      [key]: value,
    });
  };

  const setUserRole =
    process.env.REACT_APP_ADD_NEW_USER_SET_CUSTOM_ROLE_ENABLED === "true" ? (
      <>
        <Dropdown
          options={ROLE_OPTIONS}
          label="Access Level"
          name="role"
          selectedValue={newUser.role as string}
          defaultSelect
          className="grid-col"
          onChange={onChange}
        />
        <p>
          If no role is selected, the user will default to a Standard user role,
          which allows them to manage results and profiles.
        </p>
      </>
    ) : (
      <p>
        New users will be assigned as Standard Users, which allows them to
        conduct tests and manage results and profiles. You will be able to edit
        their access levels after you send them an invite.
      </p>
    );

  const disableSubmit =
    isUpdating ||
    !newUser.firstName ||
    !newUser.lastName ||
    !newUser.email ||
    !newUser.organization?.testingFacility ||
    newUser.organization.testingFacility.length === 0;

  return (
    <div className="border-0 usa-card__container">
      <div className="display-flex flex-justify">
        <h1 className="font-heading-lg margin-top-05 margin-bottom-0">
          Invite new user
        </h1>
        <button onClick={onClose} className="close-button" aria-label="Close">
          <span className="fa-layers">
            <FontAwesomeIcon icon={"circle"} size="2x" inverse />
            <FontAwesomeIcon icon={"times-circle"} size="2x" />
          </span>
        </button>
      </div>
      <div className="border-top border-base-lighter margin-x-neg-205 margin-top-205"></div>
      <div className="grid-row grid-gap">
        <TextInput
          name="firstName"
          className="grid-col"
          label="First name"
          value={newUser.firstName}
          required
          onChange={onChange}
          disabled={isUpdating}
        />
        <TextInput
          name="lastName"
          label="Last name"
          className="grid-col"
          value={newUser.lastName}
          required
          onChange={onChange}
          disabled={isUpdating}
        />
      </div>
      <div className="grid-row">
        <TextInput
          type="email"
          label="Email address"
          name="email"
          className="grid-col"
          value={newUser.email}
          required
          onChange={onChange}
          disabled={isUpdating}
        />
      </div>
      <div className="grid-row">{setUserRole}</div>
      <UserFacilitiesSettingsForm
        activeUser={newUser}
        onUpdateUser={updateUser}
        allFacilities={facilities}
        showRequired
      />
      <div className="border-top border-base-lighter margin-x-neg-205 margin-top-5 padding-top-205 text-right">
        <div className="display-flex flex-justify-end">
          <Button
            className="margin-right-2"
            onClick={onClose}
            variant="unstyled"
            label="Go back"
          />
          <Button
            className="margin-right-205"
            onClick={() => onSubmit(newUser)}
            label={isUpdating ? "Sending" : "Send invite"}
            disabled={disableSubmit}
          />
        </div>
      </div>
    </div>
  );
};

export default CreateUserForm;
