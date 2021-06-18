import React, { useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

import Button from "../../commonComponents/Button/Button";
import TextInput from "../../commonComponents/TextInput";
import Dropdown from "../../commonComponents/Dropdown";
import { Role } from "../../permissions";
import { useFacilities } from "../../../hooks/useFacilities";

import { SettingsUser } from "./ManageUsersContainer";
import "./ManageUsers.scss";
import UserFacilitiesSettingsForm from "./UserFacilitiesSettingsForm";
import { UpdateUser } from "./ManageUsers";

interface Props {
  onClose: () => void;
  onSubmit: (newUserInvite: Partial<SettingsUser>) => void;
  isUpdating: boolean;
}

const initialFormState: Partial<SettingsUser> = {
  role: "USER",
};

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
  const {
    facilities: { availableFacilities },
  } = useFacilities();

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

  const disableSubmit =
    isUpdating ||
    !newUser.firstName ||
    !newUser.lastName ||
    !newUser.email ||
    !newUser.organization?.testingFacility ||
    newUser.organization.testingFacility.length === 0;

  return (
    <div className="border-0 card-container">
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
      <div className="grid-row">
        <Dropdown
          options={ROLE_OPTIONS}
          label="Access Level"
          name="role"
          selectedValue={newUser.role as string}
          defaultSelect
          className="grid-col"
          onChange={onChange}
          required
        />
      </div>
      <UserFacilitiesSettingsForm
        activeUser={newUser}
        onUpdateUser={updateUser}
        allFacilities={availableFacilities}
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
