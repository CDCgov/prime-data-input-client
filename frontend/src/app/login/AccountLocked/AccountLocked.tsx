import { Card } from "../../commonComponents/Card/Card";
import { CardBackground } from "../../commonComponents/CardBackground/CardBackground";

export const AccountLocked = () => {
  return (
    <CardBackground>
      <Card logo>
        <h1 className="font-ui-sm margin-top-3">
          Your account has been locked for 15 minutes
        </h1>
        <p className="line-height-sans-3">
          We’ve sent you an email with instructions to reset your password.
        </p>
      </Card>
      <p className="margin-top-4">
        <a href="#0">Return to sign in</a>
      </p>
    </CardBackground>
  );
};
