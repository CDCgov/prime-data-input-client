import QRCode from "react-qr-code";

import { Card } from "../../commonComponents/Card/Card";
import { CardBackground } from "../../commonComponents/CardBackground/CardBackground";
import Button from "../../commonComponents/Button/Button";
import StepIndicator from "../../commonComponents/StepIndicator";
import { accountCreationSteps } from "../../../config/constants";

interface Props {
  qrCode: string;
}

export const MfaGoogleAuth = (props: Props) => {
  return (
    <CardBackground>
      <Card logo bodyKicker="Set up your account">
        <StepIndicator
          steps={accountCreationSteps}
          currentStepValue={"2"}
          noLabels={true}
        />
        <p className="margin-bottom-0">
          Get your security code via the Google Authenticator application.
        </p>
        <p className="usa-hint font-ui-2xs">
          To add an authentication application, scan this QR code in the app.
        </p>
        <div className="display-flex flex-column flex-align-center">
          {props.qrCode ? (
            <img src={props.qrCode} alt="TOTP QR Code" />
          ) : (
            <QRCode value={"https://bit.ly/3cRcweQ"} size={190} />
          )}
        </div>
        <Button className="margin-top-3" label={"Continue"} type={"submit"} />
      </Card>
      <p className="margin-top-5">
        <a href="#0">Return to previous step</a>
      </p>
    </CardBackground>
  );
};
