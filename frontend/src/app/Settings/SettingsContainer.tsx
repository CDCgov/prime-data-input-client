import React, { useState } from "react";
import { gql, useQuery, useMutation } from "@apollo/client";
import { toast } from "react-toastify";
import {
  useAppInsightsContext,
  useTrackEvent,
} from "@microsoft/applicationinsights-react-js";

import Alert from "../commonComponents/Alert";
import { showNotification } from "../utils";
import Settings from "./Settings";

const GET_SETTINGS_QUERY = gql`
  {
    organization {
      internalId
      testingFacility {
        cliaNumber
        name
        street
        streetTwo
        city
        county
        state
        zipCode
        phone
      }
      orderingProvider {
        firstName
        middleName
        lastName
        suffix
        NPI
        street
        streetTwo
        city
        county
        state
        zipCode
        phone
      }
      defaultDeviceType {
        internalId
      }
      deviceTypes {
        internalId
      }
    }
    deviceType {
      internalId
      name
    }
  }
`;

const SET_SETTINGS_MUTATION = gql`
  mutation(
    $testingFacilityName: String!
    $cliaNumber: String
    $orderingProviderFirstName: String!
    $orderingProviderMiddleName: String
    $orderingProviderLastName: String!
    $orderingProviderSuffix: String
    $orderingProviderNPI: String!
    $orderingProviderStreet: String
    $orderingProviderStreetTwo: String
    $orderingProviderCity: String
    $orderingProviderCounty: String
    $orderingProviderState: String
    $orderingProviderZipCode: String!
    $orderingProviderPhone: String
    $devices: [String]!
    $defaultDevice: String!
  ) {
    updateOrganization(
      testingFacilityName: $testingFacilityName
      cliaNumber: $cliaNumber
      orderingProviderFirstName: $orderingProviderFirstName
      orderingProviderMiddleName: $orderingProviderMiddleName
      orderingProviderLastName: $orderingProviderLastName
      orderingProviderSuffix: $orderingProviderSuffix
      orderingProviderNPI: $orderingProviderNPI
      orderingProviderStreet: $orderingProviderStreet
      orderingProviderStreetTwo: $orderingProviderStreetTwo
      orderingProviderCity: $orderingProviderCity
      orderingProviderCounty: $orderingProviderCounty
      orderingProviderState: $orderingProviderState
      orderingProviderZipCode: $orderingProviderZipCode
      orderingProviderPhone: $orderingProviderPhone
      deviceTypes: $devices
      defaultDevice: $defaultDevice
    )
  }
`;

const SettingsContainer = () => {
  const {
    data: settings,
    loading: isLoadingSettings,
    error: errorFetchingSettings,
  } = useQuery<SettingsData, {}>(GET_SETTINGS_QUERY, {
    fetchPolicy: "no-cache",
  });
  const appInsights = useAppInsightsContext();
  const [setSettings] = useMutation(SET_SETTINGS_MUTATION);
  const trackSaveSettings = useTrackEvent(
    appInsights,
    "Save Settings",
    null,
    false
  );
  const [mutationError, updateMutationError] = useState(null);

  if (isLoadingSettings) {
    return <p> Loading... </p>;
  }
  if (errorFetchingSettings) {
    return errorFetchingSettings;
  }
  if (mutationError) {
    throw mutationError;
  }

  if (settings === undefined) {
    return <p>Error: setting not found</p>;
  }

  const onSaveSettings = (org: Organization) => {
    trackSaveSettings(null);
    setSettings({
      variables: {
        testingFacilityName: org.testingFacility.name,
        cliaNumber: org.testingFacility.cliaNumber,
        orderingProviderFirstName: org.orderingProvider.firstName,
        orderingProviderMiddleName: org.orderingProvider.middleName,
        orderingProviderLastName: org.orderingProvider.lastName,
        orderingProviderSuffix: org.orderingProvider.suffix,
        orderingProviderNPI: org.orderingProvider.NPI,
        orderingProviderStreet: org.orderingProvider.street,
        orderingProviderStreetTwo: org.orderingProvider.streetTwo,
        orderingProviderCity: org.orderingProvider.city,
        orderingProviderCounty: org.orderingProvider.county,
        orderingProviderState: org.orderingProvider.state,
        orderingProviderZipCode: org.orderingProvider.zipCode,
        orderingProviderPhone: org.orderingProvider.phone,
        devices: org.deviceTypes,
        defaultDevice: org.defaultDevice,
      },
    })
      .then((d) => {
        console.log("success!", d); // TODO: should return an id
        let alert = (
          <Alert
            type={"success"}
            title={"Updated Organization"}
            body={"The settings for the organization have been updated"}
            role={"success"}
          />
        );
        showNotification(toast, alert);
      })
      .catch((error) => updateMutationError(error));
  };

  let deviceTypes = Object.values(settings.organization.deviceTypes).map(
    (d) => d.internalId
  );
  return (
    <Settings
      organization={{
        internalId: settings.organization.internalId,
        testingFacility: settings.organization.testingFacility,
        orderingProvider: settings.organization.orderingProvider,
        deviceTypes: deviceTypes,
        defaultDevice: settings.organization.defaultDeviceType
          ? settings.organization.defaultDeviceType.internalId
          : "",
      }}
      deviceOptions={settings.deviceType}
      saveSettings={onSaveSettings}
    />
  );
};

export default SettingsContainer;
