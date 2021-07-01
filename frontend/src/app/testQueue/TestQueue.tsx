import React, { useEffect } from "react";
import { gql, useQuery } from "@apollo/client";
import { toast } from "react-toastify";
import { TransitionGroup, CSSTransition } from "react-transition-group";

import { showError } from "../utils";
import Loading from "../commonComponents/Loading";

import AddToQueueSearch from "./addToQueue/AddToQueueSearch";
import QueueItem from "./QueueItem";
import { AoEAnswers } from "./AoEForm/AoEForm";

const pollInterval = 10_000;

const transitionDuration = 1000;
const onEmptyQueueEntering = (node: HTMLElement) => {
  node.style.opacity = "1";
};
const onEmptyQueueExiting = (node: HTMLElement) => {
  node.style.display = "none";
};

const onExiting = (node: HTMLElement) => {
  node.style.marginBottom = `-${node.offsetHeight}px`;
  node.style.opacity = "0";
  node.style.transition = `opacity ${transitionDuration}ms ease-out, margin ${transitionDuration}ms ease-out`;
  node.style.pointerEvents = "none";
};

const emptyQueueMessage = (
  <div className="grid-container prime-center card-container">
    <div className="grid-row">
      <div className="usa-card__body">
        <p>
          There are no tests running. Search for a person to start their test{" "}
        </p>
      </div>
    </div>
  </div>
);

export const queueQuery = gql`
  query GetFacilityQueue($facilityId: ID!) {
    queue(facilityId: $facilityId) {
      internalId
      pregnancy
      dateAdded
      symptoms
      symptomOnset
      noSymptoms
      firstTest
      priorTestDate
      priorTestType
      priorTestResult
      deviceType {
        internalId
        name
        model
        testLength
      }
      patient {
        internalId
        telephone
        birthDate
        firstName
        middleName
        lastName
        gender
        testResultDelivery
        preferredLanguage
      }
      result
      dateTested
      patientLink {
        internalId
      }
    }
    organization {
      testingFacility {
        id
        deviceTypes {
          internalId
          name
          model
          testLength
        }
        defaultDeviceType {
          internalId
          name
          model
          testLength
        }
      }
    }
  }
`;

interface Props {
  activeFacilityId: string;
}

interface QueueItemData extends AoEAnswers {
  internalId: string;
  dateAdded: string;
  deviceType: {
    internalId: string;
    testLength: number;
  };
  patient: {
    internalId: string;
  };
  result: string;
  dateTested: string;
  patientLink: {
    internalId: string;
  };
}

const TestQueue: React.FC<Props> = ({ activeFacilityId }) => {
  const { data, loading, error, refetch, startPolling, stopPolling } = useQuery(
    queueQuery,
    {
      fetchPolicy: "no-cache",
      variables: {
        facilityId: activeFacilityId,
      },
    }
  );

  useEffect(() => {
    // Start polling on creation, stop on componenent teardown
    startPolling(pollInterval);
    return stopPolling;
  }, [startPolling, stopPolling]);

  if (error) {
    throw error;
  }
  if (loading) {
    return <Loading message="Loading patients..." />;
  }

  const facility = data.organization.testingFacility.find(
    (f: { id: string }) => f.id === activeFacilityId
  );
  if (!facility) {
    return <p>Facility not found</p>;
  }
  if (facility.deviceTypes.length === 0) {
    showError(
      toast,
      "This facility does not have any testing devices. Go into Settings -> Manage facilities and add a device."
    );
  }
  let shouldRenderQueue =
    data.queue.length > 0 && facility.deviceTypes.length > 0;

  const createQueueItems = (patientQueue: QueueItemData[]) => {
    const queue =
      shouldRenderQueue &&
      patientQueue.map(
        ({
          internalId,
          deviceType,
          patient,
          result,
          dateTested,
          patientLink,
          ...questions
        }) => {
          return (
            <CSSTransition
              key={internalId}
              onExiting={onExiting}
              timeout={transitionDuration}
            >
              <QueueItem
                internalId={internalId}
                patient={patient}
                askOnEntry={questions}
                selectedDeviceId={deviceType?.internalId || null}
                selectedDeviceTestLength={deviceType?.testLength || null}
                selectedTestResult={result}
                devices={facility.deviceTypes}
                defaultDevice={facility.defaultDeviceType}
                refetchQueue={refetch}
                facilityId={activeFacilityId}
                dateTestedProp={dateTested}
                patientLinkId={patientLink?.internalId || null}
              />
            </CSSTransition>
          );
        }
      );

    return (
      <TransitionGroup
        timeout={transitionDuration}
        component={null}
        unmountOnExit
      >
        {queue}
        {!shouldRenderQueue && (
          <CSSTransition
            key="empty-queue"
            onEntering={onEmptyQueueEntering}
            onExiting={onEmptyQueueExiting}
            timeout={transitionDuration}
          >
            {emptyQueueMessage}
          </CSSTransition>
        )}
      </TransitionGroup>
    );
  };

  const patientsInQueue = data.queue.map(
    (q: QueueItemData) => q.patient.internalId
  );

  return (
    <main className="prime-home">
      <div className="grid-container">
        <div className="position-relative">
          <AddToQueueSearch
            refetchQueue={refetch}
            facilityId={activeFacilityId}
            patientsInQueue={patientsInQueue}
          />
        </div>
        {createQueueItems(data.queue)}
      </div>
    </main>
  );
};

export default TestQueue;
