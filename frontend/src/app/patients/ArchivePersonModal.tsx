import React from "react";
import { gql, useMutation } from "@apollo/client";
import Modal from "react-modal";
import Button from "../commonComponents/Button";
import { displayFullName, showNotification } from "../utils";
import "./ArchivePersonModal.scss";

import Alert from "../commonComponents/Alert";
import { toast } from "react-toastify";

const ARCHIVE_PERSON_RECORD = gql`
  mutation ArchivePerson($id: String!, $deleted: Boolean!) {
    setPatientIsDeleted(id: $id, deleted: $deleted) {
      internalId
    }
  }
`;

interface Props {
  person: any;
  closeModal: () => void;
}

const ArchivePersonModal = ({ person, closeModal }: Props) => {
  const [archivePersonRecord] = useMutation(ARCHIVE_PERSON_RECORD);
  const yesArchivePerson = () => {
    archivePersonRecord({
      variables: {
        id: person.internalId,
        deleted: true,
      },
    })
      .then(() => {
        const alert = <Alert type="success" title="Record archived" body="" />;
        showNotification(toast, alert);
      })
      .finally(() => {
        closeModal();
      });
  };

  return (
    <Modal
      isOpen={true}
      className="sr-archive-person-modal-content"
      overlayClassName="sr-archive-person-modal-overlay"
      contentLabel="Archive record"
    >
      <p>
        Are you sure you want to archive the record for{" "}
        <b>
          {displayFullName(
            person.firstName,
            person.middleName,
            person.lastName
          )}
        </b>
        ?
      </p>
      <div className="sr-archive-person-buttons">
        <Button variant="unstyled" label="No, go back" onClick={closeModal} />
        <Button label="Yes, I'm sure" onClick={yesArchivePerson} />
      </div>
    </Modal>
  );
};

export default ArchivePersonModal;
