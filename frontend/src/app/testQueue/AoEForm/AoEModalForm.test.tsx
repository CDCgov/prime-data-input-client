import renderer, { act } from "react-test-renderer";
import { MockedProvider } from "@apollo/client/testing";
import MockDate from "mockdate";
import ReactDOM from "react-dom";

import AoEModalForm, { LAST_TEST_QUERY } from "./AoEModalForm";

jest.mock("./AoEForm", () => () => <></>);
jest.mock("react-modal", () => (props: any) => <>{props.children}</>);

const mocks = [
  {
    request: {
      query: LAST_TEST_QUERY,
      variables: {
        patientId: "123",
      },
    },
    result: {
      data: {
        patient: {
          lastTest: {
            dateTested: "2021-02-05T22:01:55.386Z",
            result: "NEGATIVE",
          },
        },
      },
    },
  },
];

describe("AoEModalForm", () => {
  let component: renderer.ReactTestRenderer;

  beforeAll(() => {
    ReactDOM.createPortal = jest.fn((element, node) => {
      return element;
    }) as any;
  });

  beforeEach(() => {
    MockDate.set("2021-02-06");
    component = renderer.create(
      <MockedProvider mocks={mocks} addTypename={false}>
        <AoEModalForm
          saveButtonText="save"
          onClose={jest.fn()}
          patient={{
            internalId: "123",
            gender: "male",
            firstName: "Steve",
            lastName: "Jobs",
          }}
          loadState={{
            noSymptoms: false,
            symptoms: '{"426000000":"true","49727002":false}',
            symptomOnset: "",
            priorTestDate: "",
            priorTestResult: "",
            priorTestType: "",
            firstTest: false,
            pregnancy: "",
          }}
          saveCallback={jest.fn()}
        />
      </MockedProvider>
    );
  });

  describe("on data loaded", () => {
    beforeEach(async () => {
      // load data
      await act(async () => {
        await new Promise((resolve) => setTimeout(resolve, 0));
      });
    });

    it("renders", async () => {
      expect(component.toJSON()).toMatchSnapshot();
    });

    it("has a distinct view for 'text'", async () => {
      act(() => {
        component.root.findByProps({ name: "qr-code" }).props.onChange("text");
      });
      expect(component.toJSON()).toMatchSnapshot();
    });

    it("has a distinct view for 'verbal'", async () => {
      act(() => {
        component.root
          .findByProps({ name: "qr-code" })
          .props.onChange("verbal");
      });
      expect(component.toJSON()).toMatchSnapshot();
    });
  });
});
