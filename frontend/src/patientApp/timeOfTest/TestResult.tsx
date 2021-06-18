import { formatFullName } from "../../app/utils/user";
import { TestResult as TestResultType } from "../../app/testQueue/QueueItem";
import { COVID_RESULTS, TEST_RESULT_DESCRIPTIONS } from "../../app/constants";
import { usePatient } from "../../hooks/usePatient";

const TestResult = () => {
  const { patient } = usePatient();
  const fullName = formatFullName(patient as any);

  const dateTested = patient?.lastTest?.dateTested
    ? new Date(patient?.lastTest?.dateTested).toLocaleDateString()
    : new Date().toLocaleDateString();

  const deviceType = patient?.lastTest?.deviceTypeModel;
  if (!patient) {
    return <>Patient is not selected</>;
  }
  return (
    <main className="patient-app padding-top-105 padding-bottom-4 bg-base-lightest">
      <div className="grid-container maxw-tablet">
        <div className="card usa-prose">
          <h1 className="font-heading-lg">SARS-CoV-2 result</h1>
          <h2 className="font-heading-sm">Patient</h2>
          <p className="margin-top-05">{fullName}</p>
          <div className="grid-row">
            <div className="grid-col usa-prose">
              <h2 className="font-heading-sm">Test result</h2>
              <p className="margin-top-05">
                {
                  TEST_RESULT_DESCRIPTIONS[
                    patient.lastTest?.result as TestResultType
                  ]
                }
              </p>
            </div>
            <div className="grid-col usa-prose">
              <h2 className="font-heading-sm">Test date</h2>
              <p className="margin-top-05">{dateTested}</p>
            </div>
          </div>
          <h2 className="font-heading-sm">Test device</h2>
          <p className="margin-top-05">{deviceType}</p>
          <h2 className="font-heading-sm">What does my result mean?</h2>
          <TestResultNotes result={patient.lastTest?.result} />
          <p>
            For more information, please visit the{" "}
            <a href="https://www.cdc.gov/coronavirus/2019-ncov/if-you-are-sick/end-home-isolation.html">
              Centers for Disease Control and Prevention (CDC) website
            </a>{" "}
            or contact your local health department.
          </p>
        </div>
      </div>
    </main>
  );
};

interface TestResultNotesProps {
  result: TestResultType;
}

const TestResultNotes: React.FC<TestResultNotesProps> = (props) => {
  switch (props.result) {
    case COVID_RESULTS.POSITIVE:
      return (
        <>
          <p>
            Getting a positive COVID-19 test result can be difficult news, so
            it’s important to{" "}
            <a href="https://www.cdc.gov/coronavirus/2019-ncov/daily-life-coping/managing-stress-anxiety.html">
              take steps to cope with stress
            </a>{" "}
            during this time. Reach out to your support system and make a phone
            or video appointment with a mental health professional if needed.
          </p>
          <p>
            Most people who get COVID-19 will be able to recover at home. Make
            sure to follow CDC guidelines for people who are recovering at home
            and their caregivers, including:
          </p>
          <ul>
            <li>Stay home when you are sick, except to get medical care.</li>
            <li>
              Self isolate for 10 full days after symptoms first appeared (or
              starting the day after you had your test, if you have no
              symptoms).
            </li>
            <li>
              If you are self isolating at home where others live, use a
              separate room and bathroom for sick household members (if
              possible). Clean any shared rooms as needed, to avoid transmitting
              the virus.
            </li>
            <li>
              Wash your hands often with soap and water for at least 20 seconds,
              especially after blowing your nose, coughing, or sneezing; going
              to the bathroom; and before eating or preparing food.
            </li>
            <li>
              If soap and water are not available, use an alcohol-based hand
              sanitizer with at least 60% alcohol.
            </li>
            <li>
              Have a supply of clean, disposable face masks. Everyone, no matter
              their COVID-19 diagnosis, should wear face masks while in the
              home.
            </li>
          </ul>
          <p>
            <a href="https://www.cdc.gov/coronavirus/2019-ncov/symptoms-testing/symptoms.html">
              Watch for symptoms and learn when to seek emergency medical
              attention
            </a>
            .
          </p>
          <p>
            If someone is showing any of these signs, seek emergency medical
            care immediately:
          </p>
          <ul>
            <li>Trouble breathing</li>
            <li>Persistent chest pain/pressure</li>
            <li>Confusion</li>
            <li>Inability to wake or stay awake</li>
            <li>Bluish lips or face</li>
          </ul>
          <p>
            Call 911 or call ahead to your local emergency room: Notify the
            operator that you are seeking care for someone who has or may have
            COVID-19.
          </p>
        </>
      );
    case COVID_RESULTS.NEGATIVE:
      return (
        <>
          <p>
            COVID-19 antigen tests can sometimes provide inaccurate or false
            results and follow up testing may be needed. Continue social
            distancing and wearing a mask. Contact your health care provider to
            decide if additional testing is needed, especially if you experience
            any of these symptoms:
          </p>
          <ul>
            <li>Fever or chills</li>
            <li>Cough</li>
            <li>Shortness of breath or difficulty breathing</li>
            <li>Fatigue</li>
            <li>Muscle or body aches</li>
            <li>Headache</li>
            <li>Loss of taste or smell</li>
            <li>Sore throat</li>
            <li>Congestion or runny nose</li>
            <li>Nausea or vomiting</li>
            <li>Diarrhea</li>
          </ul>
        </>
      );
    default:
      return (
        <>
          <p>
            An inconclusive result is neither positive nor negative. This can
            happen because of problems with the sample collection, a very
            early-stage COVID-19 infection, or for patients with COVID-19 that
            are close to recovery. With an inconclusive result, collecting and
            testing another sample is recommended.
          </p>
          <p>
            Please make an appointment for another test as soon as possible. If
            you’ve gotten tested due to COVID-19 symptoms, it is recommended
            that you self-isolate until you get your new test results.
          </p>
        </>
      );
  }
};

export default TestResult;
