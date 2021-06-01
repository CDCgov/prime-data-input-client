import { Card } from "../../commonComponents/Card/Card";
import { CardBackground } from "../../commonComponents/CardBackground/CardBackground";
import StepIndicator from "../../commonComponents/StepIndicator";
import { accountCreationSteps } from "../../../config/constants";
import iconLoader from "../../../../node_modules/uswds/dist/img/loader.svg";

export const MfaSecurityKey = () => {
  return (
    <CardBackground>
      <Card logo bodyKicker="Set up your account">
        <StepIndicator
          steps={accountCreationSteps}
          currentStepValue={"2"}
          noLabels={true}
        />
        <p className="margin-bottom-0">Register your security key.</p>
        <ol className="usa-list usa-hint font-ui-2xs">
          <li>
            Insert your Security Key in your computer’s USB port or connect it
            with a USB cable.
          </li>
          <li>
            Once connected, tap the button or gold disk if your key has one.
          </li>
        </ol>
        <div className="display-flex flex-column flex-align-center">
          <img className="square-5 chromatic-ignore" src={iconLoader} alt="" />
        </div>
        {/* <Button className="margin-top-3" label={"Continue"} type={"submit"} /> */}
      </Card>
      <p className="margin-top-5">
        <a href="#0">Return to previous step</a>
      </p>
    </CardBackground>
  );
};
