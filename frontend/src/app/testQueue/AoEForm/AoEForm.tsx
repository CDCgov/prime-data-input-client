import React, { useCallback, useState } from "react";
import classnames from "classnames";
import moment from "moment";

import {
  globalSymptomDefinitions,
  getTestTypes,
  getPregnancyResponses,
  getTestResultDeliveryPreferences,
} from "../../../patientApp/timeOfTest/constants";
import RadioGroup from "../../commonComponents/RadioGroup";
import Button from "../../commonComponents/Button/Button";
import FormGroup from "../../commonComponents/FormGroup";
import RequiredMessage from "../../commonComponents/RequiredMessage";
import { COVID_RESULTS } from "../../constants";
import { CodeScannerContainer } from "../../commonComponents/CodeScanner/CodeScannerContainer";

import "./AoEForm.scss";
import SymptomInputs from "./SymptomInputs";
import PriorTestInputs from "./PriorTestInputs";

// Get the value associate with a button label
// TODO: move to utility?
const findValueForLabel = (
  label: string,
  list: { label: string; value: string }[]
) => (list.filter((item) => item.label === label)[0] || {}).value;

interface Props {
  saveButtonText: string;
  onClose?: () => void;
  patient: {
    internalId: string;
    gender: string;
    testResultDelivery: string;
    birthDate: string;
  };
  lastTest:
    | {
        dateTested: string;
        result: string;
      }
    | undefined;
  loadState?: {
    noSymptoms: boolean;
    symptoms: string;
    symptomOnset: string;
    priorTestDate: string;
    priorTestResult: string;
    priorTestType: string;
    firstTest: boolean;
    pregnancy: string;
  };
  saveCallback: (response: {
    noSymptoms: boolean;
    symptoms: string;
    symptomOnset: string | undefined;
    priorTestDate: string | undefined | null;
    priorTestResult: string | undefined | null;
    priorTestType: string | undefined | null;
    firstTest: boolean;
    pregnancy: string | undefined;
    testResultDelivery: string;
  }) => void;
  isModal: boolean;
  noValidation: boolean;
  formRef?: React.Ref<HTMLFormElement>;
}

const AoEForm: React.FC<Props> = ({
  saveButtonText,
  onClose,
  patient,
  loadState = {},
  saveCallback,
  isModal,
  noValidation,
  lastTest,
  formRef,
}) => {
  // this seems like it will do a bunch of wasted work on re-renders and non-renders,
  // but it's all small-ball stuff for now
  const testConfig = getTestTypes();
  const symptomConfig = globalSymptomDefinitions;
  const initialSymptoms: { [key: string]: boolean } = {};
  if (loadState.symptoms) {
    const loadedSymptoms: { [key: string]: string | boolean } = JSON.parse(
      loadState.symptoms
    );

    symptomConfig.forEach((opt) => {
      const val = opt.value;
      if (typeof loadedSymptoms[val] === "string") {
        initialSymptoms[val] = loadedSymptoms[val] === "true";
      } else {
        initialSymptoms[val] = loadedSymptoms[val] as boolean;
      }
    });
  } else {
    symptomConfig.forEach((opt) => {
      initialSymptoms[opt.value] = false;
    });
  }

  const [noSymptoms, setNoSymptoms] = useState(loadState.noSymptoms || false);
  const [currentSymptoms, setSymptoms] = useState(initialSymptoms);
  const [onsetDate, setOnsetDate] = useState(loadState.symptomOnset);
  const [isFirstTest, setIsFirstTest] = useState(loadState.firstTest);
  const [priorTestDate, setPriorTestDate] = useState(loadState.priorTestDate);
  const [priorTestType, setPriorTestType] = useState(loadState.priorTestType);
  const [priorTestResult, setPriorTestResult] = useState<
    string | null | undefined
  >(
    loadState.priorTestResult === undefined
      ? undefined
      : loadState.priorTestResult || null
  );
  const [pregnancyResponse, setPregnancyResponse] = useState(
    loadState.pregnancy
  );
  const [testResultDelivery, setTestResultDelivery] = useState(
    patient.testResultDelivery
  );

  const [linkedTestId, setLinkedTestId] = useState<string | null>(null);

  const patientIsOver18 = moment().diff(patient.birthDate, "years") >= 18;

  // form validation
  const [symptomError, setSymptomError] = useState<string | undefined>();
  const [symptomOnsetError, setSymptomOnsetError] = useState<
    string | undefined
  >();
  const symptomRef = React.createRef<HTMLInputElement>();
  const symptomOnsetRef = React.createRef<HTMLInputElement>();

  const isValidForm = () => {
    if (noValidation) return true;
    const hasSymptoms = Object.keys(currentSymptoms).some(
      (key) => currentSymptoms[key]
    );
    if (!noSymptoms && !hasSymptoms) {
      setSymptomError("Select your symptoms");
      setSymptomOnsetError(undefined);
      symptomRef?.current?.focus();
      return false;
    }

    if (noSymptoms) {
      setSymptomError(undefined);
      setSymptomOnsetError(undefined);
      return true;
    }

    if (hasSymptoms && !onsetDate) {
      setSymptomError(undefined);
      setSymptomOnsetError("Enter the date of symptom onset");
      symptomOnsetRef?.current?.focus();
      return false;
    }

    if (hasSymptoms && onsetDate) {
      setSymptomError(undefined);
      setSymptomOnsetError(undefined);
      return true;
    }
  };

  // Auto-answer pregnancy question for males
  const pregnancyResponses = getPregnancyResponses();
  if (patient.gender === "male" && !pregnancyResponse) {
    setPregnancyResponse(findValueForLabel("No", pregnancyResponses));
  }

  const saveAnswers = (e: React.FormEvent) => {
    if (isValidForm()) {
      const saveSymptoms = { ...currentSymptoms };
      if (noSymptoms) {
        // set all symptoms explicitly to false
        symptomConfig.forEach(({ value }) => {
          saveSymptoms[value] = false;
        });
      }
      const priorTest = isFirstTest
        ? {
            firstTest: true,
            priorTestDate: null,
            priorTestType: null,
            priorTestResult: null,
          }
        : {
            firstTest: false,
            priorTestDate: priorTestDate,
            priorTestType: priorTestType,
            priorTestResult:
              !priorTestResult || priorTestResult === COVID_RESULTS.UNKNOWN
                ? null
                : priorTestResult,
          };

      saveCallback({
        noSymptoms,
        symptoms: JSON.stringify(saveSymptoms),
        symptomOnset: onsetDate,
        ...priorTest,
        pregnancy: pregnancyResponse,
        testResultDelivery,
      });
      if (isModal && onClose) {
        onClose();
      } else {
        // pxp must not use dom form's action->post
        e.preventDefault();
      }
    } else {
      e.preventDefault();
    }
  };
  const handleTestLinking = useCallback((testId: string) => {
    console.log("[TEST ID LINKED]", testId);
    setLinkedTestId(testId);
  }, []);

  const buttonGroup = (
    <div
      className={classnames(
        "sr-time-of-test-buttons",
        "sr-time-of-test-buttons-footer",
        { "aoe-modal__footer": isModal }
      )}
    >
      <Button
        id="aoe-form-save-button"
        className="margin-right-0"
        label={saveButtonText}
        type={"submit"}
      />
    </div>
  );

  return (
    <>
      <form
        className="display-flex flex-column padding-bottom-10"
        onSubmit={saveAnswers}
        ref={formRef}
      >
        {isModal && (
          <div className="margin-top-4 border-top border-base-lighter" />
        )}
        <RequiredMessage />
        {patientIsOver18 && (
          <FormGroup title="Results">
            <RadioGroup
              legend="How would you like to receive a copy of your results?"
              name="testResultDelivery"
              onChange={setTestResultDelivery}
              buttons={getTestResultDeliveryPreferences()}
              selectedRadio={testResultDelivery}
            />
          </FormGroup>
        )}
        <FormGroup title="Symptoms">
          <SymptomInputs
            noSymptoms={noSymptoms}
            setNoSymptoms={setNoSymptoms}
            currentSymptoms={currentSymptoms}
            setSymptoms={setSymptoms}
            setOnsetDate={setOnsetDate}
            onsetDate={onsetDate}
            symptomError={symptomError}
            symptomOnsetError={symptomOnsetError}
            symptomRef={symptomRef}
            symptomOnsetRef={symptomOnsetRef}
          />
        </FormGroup>

        <FormGroup title="Test history">
          <div className="prime-formgroup__wrapper">
            <PriorTestInputs
              testTypeConfig={testConfig}
              priorTestDate={priorTestDate}
              setPriorTestDate={setPriorTestDate}
              isFirstTest={isFirstTest}
              setIsFirstTest={setIsFirstTest}
              priorTestType={priorTestType}
              setPriorTestType={setPriorTestType}
              priorTestResult={priorTestResult}
              setPriorTestResult={setPriorTestResult}
              lastTest={lastTest}
            />
          </div>
        </FormGroup>
        <FormGroup title="Add test">
          {!linkedTestId && (
            <CodeScannerContainer
              onChange={(testId: string) => {
                handleTestLinking(testId);
              }}
            />
          )}

          {linkedTestId && (
            <React.Fragment>
              <div className="usa-alert usa-alert--success usa-alert--slim">
                <div className="usa-alert__body">
                  <p className="usa-alert__text">
                    Test kit linked successfully
                  </p>
                </div>
              </div>
            </React.Fragment>
          )}
        </FormGroup>

        {patient.gender?.toLowerCase() !== "male" && (
          <FormGroup title="Pregnancy">
            <RadioGroup
              legend="Currently pregnant?"
              name="pregnancy"
              onChange={setPregnancyResponse}
              buttons={pregnancyResponses}
              selectedRadio={pregnancyResponse}
            />
          </FormGroup>
        )}
        <div
          className={classnames(
            isModal
              ? "modal__footer--sticky position-fixed flex-align-self-end"
              : "margin-top-3"
          )}
        >
          {buttonGroup}
        </div>
      </form>
    </>
  );
};

export default AoEForm;
