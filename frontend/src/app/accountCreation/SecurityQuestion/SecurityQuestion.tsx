import { useState } from "react";
import { Redirect } from "react-router";

import { Card } from "../../commonComponents/Card/Card";
import { CardBackground } from "../../commonComponents/CardBackground/CardBackground";
import Dropdown from "../../commonComponents/Dropdown";
import TextInput from "../../commonComponents/TextInput";
import Button from "../../commonComponents/Button/Button";
import StepIndicator from "../../commonComponents/StepIndicator";
import {
  accountCreationSteps,
  securityQuestions,
} from "../../../config/constants";
import { AccountCreationApi } from "../AccountCreationApiService";

export const SecurityQuestion = () => {
  // State setup
  const [securityQuestion, setSecurityQuestion] = useState("");
  const [securityQuestionError, setSecurityQuestionError] = useState("");
  const [securityAnswer, setSecurityAnswer] = useState("");
  const [securityAnswerError, setSecurityAnswerError] = useState("");
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const validateSecurityQuestion = (): boolean => {
    let error = "";
    if (securityQuestion === "") {
      error = "Enter a security question";
    }
    setSecurityQuestionError(error);
    return error === "";
  };

  const validateSecurityAnswer = (): boolean => {
    let error = "";
    if (securityAnswer === "") {
      error = "Enter your answer";
    }
    setSecurityAnswerError(error);
    return error === "";
  };

  const handleSubmit = async () => {
    if (validateSecurityQuestion() && validateSecurityAnswer()) {
      setLoading(true);
      try {
        await AccountCreationApi.setRecoveryQuestion(
          securityQuestion,
          securityAnswer
        );
        setSubmitted(true);
      } catch (error) {
        setSecurityQuestionError(`API Error: ${error}`);
      } finally {
        setLoading(false);
      }
    }
  };

  if (loading) {
    return (
      <main>
        <div className="grid-container maxw-tablet">
          <p className="margin-top-3">Validating security question...</p>
        </div>
      </main>
    );
  }

  if (submitted) {
    return (
      <Redirect
        to={{
          pathname: "/mfa-select",
        }}
      />
    );
  }

  return (
    <CardBackground>
      <Card logo bodyKicker="Set up your account">
        <StepIndicator
          steps={accountCreationSteps}
          currentStepValue={"1"}
          noLabels={true}
        />
        <Dropdown
          label="Security question"
          name="security-question"
          hintText="If you forget your password, we’ll ask you this question to verify your identity."
          required
          selectedValue={securityQuestion}
          options={securityQuestions.map((c) => ({ label: c, value: c }))}
          defaultSelect
          className="usa-input--medium"
          errorMessage={securityQuestionError}
          validationStatus={securityQuestionError ? "error" : undefined}
          onBlur={validateSecurityQuestion}
          onChange={(evt) => setSecurityQuestion(evt.target.value)}
        />
        <TextInput
          label={"Answer"}
          name={"answer"}
          value={securityAnswer}
          required
          errorMessage={securityAnswerError}
          validationStatus={securityAnswerError ? "error" : undefined}
          onBlur={validateSecurityAnswer}
          onChange={(evt) => setSecurityAnswer(evt.target.value)}
        />
        <Button
          className="margin-top-3"
          label={"Continue"}
          type={"submit"}
          onClick={handleSubmit}
        />
      </Card>
      <p className="margin-top-5">
        <a href="#0">Return to previous step</a>
      </p>
    </CardBackground>
  );
};
