import { Router } from "react-router-dom";
import { createMemoryHistory } from "history";
import { render } from "@testing-library/react";

import { patient } from "../../storage/store";
import { patientSample } from "../../config/constants";

import TestResult from "./TestResult";

describe("TestResult", () => {
  it("should show a positive result", () => {
    patient({
      ...patientSample,
      firstName: "Bob",
      lastName: "Barker",
      middleName: "",
      lastTest: {
        deviceTypeModel: "Testing device",
        dateTested: "08/27/2020",
        result: "POSITIVE",
      },
    });

    const { container, getByText } = render(
      <Router history={createMemoryHistory()}>
        <TestResult />
      </Router>
    );

    expect(getByText("SARS-CoV-2 result")).toBeInTheDocument();
    expect(getByText("Bob Barker")).toBeInTheDocument();
    expect(getByText("Test result")).toBeInTheDocument();
    expect(getByText("Positive")).toBeInTheDocument();
    expect(container).toMatchSnapshot();
  });
  it("should show a negative result", () => {
    patient({
      ...patientSample,
      firstName: "Bob",
      lastName: "Barker",
      middleName: "",
      lastTest: {
        deviceTypeModel: "Testing device",
        dateTested: "08/27/2020",
        result: "NEGATIVE",
      },
    });

    const { container, getByText } = render(
      <Router history={createMemoryHistory()}>
        <TestResult />
      </Router>
    );

    expect(getByText("SARS-CoV-2 result")).toBeInTheDocument();
    expect(getByText("Bob Barker")).toBeInTheDocument();
    expect(getByText("Test result")).toBeInTheDocument();
    expect(getByText("Negative")).toBeInTheDocument();
    expect(container).toMatchSnapshot();
  });
  it("should show an inconclusive result", () => {
    patient({
      ...patientSample,

      firstName: "Bob",
      lastName: "Barker",
      middleName: "",
      lastTest: {
        deviceTypeModel: "Testing device",
        dateTested: "08/27/2020",
        result: "UNDETERMINED",
      },
    });
    const { container, getByText } = render(
      <Router history={createMemoryHistory()}>
        <TestResult />
      </Router>
    );

    expect(getByText("SARS-CoV-2 result")).toBeInTheDocument();
    expect(getByText("Bob Barker")).toBeInTheDocument();
    expect(getByText("Test result")).toBeInTheDocument();
    expect(getByText("Inconclusive")).toBeInTheDocument();
    expect(container).toMatchSnapshot();
  });
});
