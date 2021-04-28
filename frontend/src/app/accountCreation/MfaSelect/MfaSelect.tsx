import { useState } from "react";

import { Card } from "../../commonComponents/Card/Card";
import { CardBackground } from "../../commonComponents/CardBackground/CardBackground";
import Button from "../../commonComponents/Button";
import RadioGroup from "../../commonComponents/RadioGroup";
import StepIndicator from "../../commonComponents/StepIndicator";
import { accountCreationSteps } from "../../../config/constants";

export const MfaSelect = () => {
  const [mfaOption, setMfaOption] = useState("");

  return (
    <CardBackground>
      <Card logo bodyKicker="Set up your account">
        <StepIndicator
          steps={accountCreationSteps}
          currentStepValue={"2"}
          noLabels={true}
        />
        <RadioGroup
          name="addressSelect"
          hintText="Add a second layer of security to protect your account."
          buttons={[
            {
              value: "SMS",
              label: "Text message (SMS)",
              labelDescription:
                "Get a single-use code sent via text message (SMS).",
              labelTag: "Less secure",
            },
            {
              value: "Okta",
              label: "Okta Verify",
              labelDescription:
                "Get a push notification sent through the Okta mobile app.",
              labelTag: "Secure",
            },
          ]}
          selectedRadio={mfaOption}
          onChange={setMfaOption}
          // onBlur={validate}
          // validationStatus={error ? "error" : undefined}
          variant="tile"
          // errorMessage={error ? ERROR_MESSAGE : undefined}
        />
        <Button className="margin-top-3" label={"Continue"} type={"submit"} />
      </Card>
      <p className="margin-top-5">
        <a href="#0">Return to previous step</a>
      </p>
    </CardBackground>
  );
};
