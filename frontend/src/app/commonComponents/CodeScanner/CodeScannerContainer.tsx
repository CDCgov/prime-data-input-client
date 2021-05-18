import React, { useState } from "react";
import cls from "classnames";

import Button from "../Button/Button";
import TextInput from "../TextInput";

import CodeScanner from "./CodeScanner";
import styles from "./CodeScanner.module.scss";

interface Props {
  onChange: (testId: string) => void;
}

export const CodeScannerContainer: React.FC<Props> = ({ onChange }) => {
  const [testId, setTestId] = useState<string>("");
  const [manually, setManually] = useState<boolean>(false);

  const handleAddTestId = (id?: string) => {
    if (id) {
      onChange(id);
    } else {
      onChange(testId);
    }
  };
  return (
    <div>
      {!manually && (
        <React.Fragment>
          <CodeScanner onScan={(id: string) => handleAddTestId(id)} />
          <Button
            variant={"unstyled"}
            label={"Enter ID manually"}
            onClick={() => setManually(true)}
          />
        </React.Fragment>
      )}
      {manually && (
        <React.Fragment>
          <TextInput
            name="testId"
            label="Test ID"
            type="text"
            value={testId}
            onChange={({ target }) => setTestId(target.value)}
          />

          <ul className={cls("usa-button-group", styles.buttonGroup)}>
            <li className="usa-button-group__item">
              <Button
                label="Back"
                variant={"unstyled"}
                onClick={() => setManually(false)}
              />
            </li>
            <li className="usa-button-group__item">
              <Button label="Save" onClick={() => handleAddTestId()} />
            </li>
          </ul>
        </React.Fragment>
      )}
    </div>
  );
};
