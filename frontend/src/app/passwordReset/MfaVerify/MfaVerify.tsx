import { ReactElement, useState } from "react";

import { Card } from "../../commonComponents/Card/Card";
import { CardBackground } from "../../commonComponents/CardBackground/CardBackground";
import TextInput from "../../commonComponents/TextInput";
import Button from "../../commonComponents/Button/Button";

interface Props {
  hint: ReactElement;
  type: string;
}

export const MfaVerify = (props: Props) => {
  const [code, setCode] = useState("");
  const [codeError, setCodeError] = useState("");

  const validateCode = (): boolean => {
    let error = "";
    if (code === "") {
      error = "Enter your security code";
    }
    setCodeError(error);
    return error === "";
  };

  return (
    <CardBackground>
      <Card logo>
        <h1 className="font-ui-sm margin-top-3">Verify via {props.type}</h1>
        <p className="usa-hint margin-bottom-0 line-height-sans-3">
          {props.hint}
        </p>
        <div className="display-flex">
          <TextInput
            className="flex-fill"
            label={"One-time security code"}
            name={"security-code"}
            type={"tel"}
            value={code}
            errorMessage={codeError}
            validationStatus={codeError ? "error" : undefined}
            onBlur={validateCode}
            onChange={(evt) => setCode(evt.currentTarget.value)}
          />
          <Button
            className="margin-top-3 flex-align-self-end margin-left-1"
            label={"Submit"}
            type={"submit"}
            onClick={validateCode}
          />
        </div>
        <Button
          className="usa-button--outline display-block margin-top-3"
          label={"Send another code"}
          type={"submit"}
        />
      </Card>
      <p className="margin-top-4">
        <a href="#0">Return to sign in</a>
      </p>
    </CardBackground>
  );
};
