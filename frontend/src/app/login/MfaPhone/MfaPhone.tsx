import { useState } from "react";

import { Card } from "../../commonComponents/Card/Card";
import { CardBackground } from "../../commonComponents/CardBackground/CardBackground";
import TextInput from "../../commonComponents/TextInput";
import Button from "../../commonComponents/Button/Button";

interface Props {
  phoneNumber: string;
}

export const MfaPhone = (props: Props) => {
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
        <h1 className="font-ui-sm margin-top-3">Enter your security code</h1>
        <p className="usa-hint margin-bottom-0 line-height-sans-3">
          Enter the security code sent to <b>{props.phoneNumber}</b>. This code
          will expire in 10 minutes.
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
        <a href="#0">Choose another authentication method</a>
      </p>
      <p>
        <a href="#0">Return to sign in</a>
      </p>
    </CardBackground>
  );
};
