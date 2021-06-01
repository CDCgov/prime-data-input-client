import React, { useState } from "react";
import moment from "moment";
import { DatePicker, Label } from "@trussworks/react-uswds";

import {
  COVID_RESULTS,
  TEST_RESULT_DESCRIPTIONS,
  YES_NO_VALUES,
} from "../../constants";
import RadioGroup from "../../commonComponents/RadioGroup";
import Dropdown, { Option } from "../../commonComponents/Dropdown";
import Optional from "../../commonComponents/Optional";
import Checkboxes from "../../commonComponents/Checkboxes";

interface Props {
  testTypeConfig: Option[];
  isFirstTest: boolean | undefined;
  setIsFirstTest: (isFirstTest: boolean) => void;
  priorTestDate: string | undefined | null;
  setPriorTestDate: (priorTestDate: string | undefined) => void;
  priorTestResult: string | undefined | null;
  setPriorTestResult: (priorTestResult: string | undefined | null) => void;
  priorTestType: string | undefined;
  setPriorTestType: (priorTestType: string | undefined) => void;
  lastTest:
    | {
        dateTested: string;
        result: string;
      }
    | undefined;
}

const PriorTestInputs: React.FC<Props> = ({
  testTypeConfig,
  isFirstTest,
  setIsFirstTest,
  priorTestDate,
  setPriorTestDate,
  priorTestResult,
  setPriorTestResult,
  priorTestType,
  setPriorTestType,
  lastTest,
}) => {
  const recentDate = (lastTest?.dateTested || "").split("T")[0];
  const filledPriorTest =
    priorTestDate &&
    recentDate === priorTestDate &&
    lastTest &&
    lastTest.result === priorTestResult;
  const [lastTestAnswer, setlastTestAnswer] = useState(
    isFirstTest === undefined ? undefined : filledPriorTest ? "yes" : "no"
  );
  const [lastTestDateKnown, setLastTestDateKnown] = useState(
    !!priorTestDate || priorTestDate === undefined
  );
  const previousTestEntry = (
    <>
      <div className="usa-form-group">
        <Label htmlFor="prior_test_date">Date of most recent test</Label>
        <span className="usa-hint">mm/dd/yyyy</span>
        <DatePicker
          className="maxw-mobile"
          id="prior_test_date"
          name="prior_test_date"
          defaultValue={priorTestDate || undefined}
          minDate="2020-02-01"
          maxDate={new Date().toISOString().split("T")[0]}
          disabled={!lastTestDateKnown}
          onChange={setPriorTestDate}
        />
      </div>
      <Checkboxes
        legend="Date of most recent test unknown?"
        name="mostRecentTestUnknown"
        legendSrOnly={true}
        boxes={[{ label: "Unknown", value: "UNKNOWN" }]}
        checkedValues={{ UNKNOWN: !lastTestDateKnown }}
        onChange={(e) => {
          setLastTestDateKnown(!e.target.checked);
          if (e.target.checked) {
            setPriorTestDate(undefined);
          }
        }}
      />
      <Dropdown
        options={testTypeConfig}
        label="Type of prior test"
        name="prior_test_type"
        selectedValue={priorTestType || ""}
        onChange={(e) => setPriorTestType(e.target.value)}
        defaultSelect
      />
      <Dropdown
        options={[
          {
            value: COVID_RESULTS.POSITIVE,
            label: TEST_RESULT_DESCRIPTIONS.POSITIVE,
          },
          {
            value: COVID_RESULTS.NEGATIVE,
            label: TEST_RESULT_DESCRIPTIONS.NEGATIVE,
          },
          {
            value: COVID_RESULTS.INCONCLUSIVE,
            label: TEST_RESULT_DESCRIPTIONS.UNDETERMINED,
          },
          {
            value: COVID_RESULTS.UNKNOWN,
            label: TEST_RESULT_DESCRIPTIONS.UNKNOWN,
          },
        ]}
        label="Result of prior test"
        name="prior_test_result"
        selectedValue={
          priorTestResult === null
            ? COVID_RESULTS.UNKNOWN
            : priorTestResult || ""
        }
        defaultSelect
        onChange={(e) => setPriorTestResult(e.target.value)}
      />
    </>
  );

  // If lastTest matches priorTest, offer to fill it
  if (lastTest) {
    // Suggest a prior test
    // TODO: ARIA/508 compliance
    const legendIsh = (
      <>
        <div className="margin-top-2">
          <b>Was this your most recent COVID-19 test?</b>
          <Optional />
        </div>
        <p className="prime-previous-test-display margin-top-2 margin-bottom-0 line-height-sans-5">
          <b>Date: </b>
          {moment(lastTest.dateTested).format("LLLL")}
          <br />
          <b>Type: </b>
          Antigen
          <br />
          <b>Result: </b>
          {lastTest.result}
        </p>
      </>
    );
    return (
      <>
        {legendIsh}
        <RadioGroup
          buttons={[
            { label: "Yes", value: "yes" },
            { label: "No", value: "no" },
          ]}
          selectedRadio={lastTestAnswer}
          onChange={(value) => {
            setIsFirstTest(false);
            setlastTestAnswer(value);
            if (value === "yes") {
              // Fill in last test info using this data
              // TODO: update when test history has test type
              setPriorTestType("2");
              setPriorTestDate((lastTest.dateTested || "").split("T")[0]);
              setPriorTestResult(lastTest?.result);
            } else {
              setPriorTestType(undefined);
              setPriorTestDate(undefined);
              setPriorTestResult(undefined);
            }
          }}
          legend="Was this your most recent COVID-19 test?"
          legendSrOnly
          name="most_recent_flag"
          variant="horizontal"
        />
        {lastTestAnswer === "no" && previousTestEntry}
      </>
    );
  }

  return (
    <>
      <div className="usa-legend">
        Is this your first COVID-19 test?
        <Optional />
      </div>
      <div className="prime-previous-test-display margin-top-2">
        No previous test data found
      </div>

      <RadioGroup
        buttons={YES_NO_VALUES}
        selectedRadio={
          isFirstTest === true ? "YES" : isFirstTest === false ? "NO" : ""
        }
        onChange={(value) => {
          setIsFirstTest(value === "YES");
        }}
        legend="Is this your first covid test?"
        legendSrOnly
        name="prior_test_flag"
        variant="horizontal"
      />
      {isFirstTest === false && previousTestEntry}
    </>
  );
};

export default PriorTestInputs;
