import { ChangeEvent, useState } from "react";
import { Redirect } from "react-router";
import { useReactiveVar } from "@apollo/client";

import TextInput from "../../commonComponents/TextInput";
import Button from "../../commonComponents/Button/Button";
import {
  hasLowerCase,
  hasNumber,
  hasUpperCase,
  isAtLeast8Chars,
} from "../../utils/text";
import { AccountCreationApi } from "../AccountCreationApiService";
import { appConfig } from "../../../storage/store";
import { LoadingCard } from "../LoadingCard/LoadingCard";
import { Card } from "../../commonComponents/Card/Card";
import { CardBackground } from "../../commonComponents/CardBackground/CardBackground";
import StepIndicator from "../../commonComponents/StepIndicator";
import { accountCreationSteps } from "../../../config/constants";

export const PasswordForm = () => {
  // State setup
  const [password, setPassword] = useState("");
  const [passwordConfirmation, setPasswordConfirmation] = useState("");
  const [strength, setStrength] = useState(0);
  const [strengthHint, setStrengthHint] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [passwordConfirmationError, setPasswordConfirmationError] = useState(
    ""
  );
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  // Get activation token from store
  const { activationToken } = useReactiveVar(appConfig);

  // An array of functions that test for all of the password requirements
  const requirements = [hasLowerCase, hasUpperCase, hasNumber, isAtLeast8Chars];

  // Returns an array only containing the requirement functions that fail
  const missingRequirements = (value: string): Function[] =>
    requirements.filter((f) => !f(value));

  // Returns an array only containing the requirement functions that pass
  const matchedRequirements = (value: string): Function[] =>
    requirements.filter((f) => f(value));

  // Builds a hint or error string for password describing missing requirements
  const buildHint = (value: string): string => {
    const needs = missingRequirements(value);
    if (needs.length) {
      const hints = [];
      if (needs.includes(isAtLeast8Chars)) {
        hints.push("at least 8 characters");
      }
      if (needs.includes(hasLowerCase)) {
        hints.push("a lowercase letter");
      }
      if (needs.includes(hasUpperCase)) {
        hints.push("an uppercase letter");
      }
      if (needs.includes(hasNumber)) {
        hints.push("a number");
      }
      if (hints.length === 1) {
        return "Your password must have " + hints[0];
      } else if (hints.length >= 3) {
        return `Your password must have ${hints.slice(0, -1).join(", ")}, and ${
          hints[hints.length - 1]
        }`;
      } else {
        return `Your password must have ${hints.slice(0, -1).join(", ")} and ${
          hints[hints.length - 1]
        }`;
      }
    }
    return "";
  };

  // This function set the hint on the page, then returns the number of passing requirements
  const calculateStrength = (value: string): number => {
    setStrengthHint(buildHint(value));
    return matchedRequirements(value).length;
  };

  // onChange handler for password field
  const handlePasswordChange = ({
    target: { value },
  }: ChangeEvent<HTMLInputElement>) => {
    setPassword(value);
    setStrength(calculateStrength(value));
  };

  // Clears the hint and sets the error since this is validation on submit
  const validatePassword = (): boolean => {
    setStrengthHint("");
    const hint = buildHint(password);
    setPasswordError(hint);
    return hint === "";
  };

  const validatePasswordConfirmation = (): boolean => {
    let error = "";
    if (password !== passwordConfirmation) {
      error = "Passwords must match";
    }
    setPasswordConfirmationError(error);
    return error === "";
  };

  // Form submit handler
  const handleSubmit = async () => {
    if (
      validatePassword() &&
      validatePasswordConfirmation() &&
      activationToken
    ) {
      setLoading(true);
      try {
        await AccountCreationApi.setPassword(activationToken, password);
        setSubmitted(true);
      } catch (error) {
        setPasswordError(`API Error: ${error?.message}`);
      } finally {
        setLoading(false);
      }
    }
  };

  // This switch sets both the label of the password strength
  // and the color class to use for the strength bars
  let strengthLabel, strengthColor: string;
  switch (strength) {
    case 1:
      strengthLabel = "Weak";
      strengthColor = "bg-error";
      break;
    case 2:
      strengthLabel = "Okay";
      strengthColor = "bg-orange";
      break;
    case 3:
      strengthLabel = "Medium";
      strengthColor = "bg-gold";
      break;
    case 4:
      strengthLabel = "Strong";
      strengthColor = "bg-success";
      break;
    default:
      strengthLabel = "...";
      strengthColor = "bg-base-lighter";
  }

  // This builds the divs for the strength bars
  const strengthBars = [1, 2, 3, 4].map((score) => {
    const margin = score === 1 ? "" : "margin-left-1";
    const color = strength >= score ? strengthColor : "bg-base-lighter";
    return (
      <div
        key={score}
        className={`height-1 width-full ${margin} ${color}`}
      ></div>
    );
  });

  if (loading) {
    return <LoadingCard message="Validating password" />;
  }

  if (submitted) {
    return (
      <Redirect
        to={{
          pathname: "/set-recovery-question",
          search: `?activationToken=${activationToken}`,
        }}
      />
    );
  }

  return (
    <CardBackground>
      <Card logo bodyKicker="Set up your account">
        <StepIndicator
          steps={accountCreationSteps}
          currentStepValue={"0"}
          noLabels={true}
        />
        <TextInput
          label={"Password"}
          name={"password"}
          type={"password"}
          value={password}
          hintText="Your password must be at least 8 characters, include an uppercase and lowercase letter, and a number."
          errorMessage={passwordError}
          validationStatus={passwordError ? "error" : undefined}
          onBlur={validatePassword}
          onChange={handlePasswordChange}
        />
        <div className="display-flex grid-gap margin-top-105">
          {strengthBars}
        </div>
        <p className="font-ui-3xs margin-bottom-0 text-base">
          Password strength: <span className="text-bold">{strengthLabel}</span>
        </p>
        <p className="font-ui-3xs margin-top-05 line-height-sans-3 text-base">
          {strengthHint}
        </p>
        <TextInput
          label={"Confirm password"}
          name={"confirm-password"}
          type={"password"}
          value={passwordConfirmation}
          errorMessage={passwordConfirmationError}
          validationStatus={passwordConfirmationError ? "error" : undefined}
          onBlur={validatePasswordConfirmation}
          onChange={(evt) => setPasswordConfirmation(evt.currentTarget.value)}
        />
        <Button
          className="margin-top-3"
          label={"Continue"}
          type={"submit"}
          onClick={handleSubmit}
        />
      </Card>
    </CardBackground>
  );
};
