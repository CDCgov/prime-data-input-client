import { Card } from "../../commonComponents/Card/Card";
import { CardBackground } from "../../commonComponents/CardBackground/CardBackground";
import Button from "../../commonComponents/Button/Button";
import StepIndicator from "../../commonComponents/StepIndicator";
import { accountCreationSteps } from "../../../config/constants";

interface Props {
  qrCode: string;
  totpType: string;
}

export const MfaTotp = (props: Props) => {
  return (
    <CardBackground>
      <Card logo bodyKicker="Set up your account">
        <StepIndicator
          steps={accountCreationSteps}
          currentStepValue={"2"}
          noLabels={true}
        />
        <p className="margin-bottom-0">
          Get your security code via the {props.totpType} application.
        </p>
        <p className="usa-hint font-ui-2xs">
          To connect SimpleReport to {props.totpType}, scan this QR code in the
          app.
        </p>
        <div className="display-flex flex-column flex-align-center">
          {props.qrCode ? (
            <img
              src={props.qrCode}
              alt="TOTP QR Code"
              className="height-card"
            />
          ) : null}
        </div>
        <Button className="margin-top-3" label={"Continue"} type={"submit"} />
      </Card>
      <p className="margin-top-5">
        <a href="#0">Return to previous step</a>
      </p>
    </CardBackground>
  );
};
