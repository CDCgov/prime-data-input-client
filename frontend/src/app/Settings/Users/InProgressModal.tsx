import React from "react";
import Modal from "react-modal";
import Button from "../../commonComponents/Button";

interface Props {
  onClose: () => void;
  onContinue: () => void;
}

const InProgressModal: React.FC<Props> = ({ onClose, onContinue }) => {
  return (
    <Modal
      isOpen={true}
      style={{
        content: {
          inset: "3em auto auto auto",
          margin: "auto auto",
          display: "block",
          width: "30%",
          height: "25%",
          minHeight: "10em",
          minWidth: "10em",
        },
      }}
      overlayClassName="prime-modal-overlay"
      contentLabel="Unsaved changes to current user"
    >
      <div className="display-flex flex-column flex-align-center text-center">
        <h3> You have unsaved changes, are you sure you want to proceed? </h3>

        <div className="margin-top-3">
          <Button onClick={onClose} label="Go back" />
          <Button onClick={onContinue} label="Continue anyway" />
        </div>
      </div>
    </Modal>
  );
};

export default InProgressModal;
