import React, { useState } from "react";
import { gql, useMutation, useQuery } from "@apollo/client";
import { toast } from "react-toastify";
import {
  useAppInsightsContext,
  useTrackEvent,
} from "@microsoft/applicationinsights-react-js";

import Alert from "../../commonComponents/Alert";
import { showNotification } from "../../utils";
import OrganizationForm from "./OrganizationForm";
import { Redirect } from "react-router-dom";

const GET_DEVICES_QUERY = gql`
  query GetDevices {
    deviceType {
      internalId
      name
    }
  }
`;

const CREATE_ORGANIZATION_MUTATION = gql`
  mutation CreateOrganization(
    $name: String!
    $externalId: String!
    $testingFacilityName: String!
    $cliaNumber: String
    $street: String
    $streetTwo: String
    $city: String
    $county: String
    $state: String
    $zipCode: String!
    $phone: String
    $email: String
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
    $deviceTypes: [String]!
    $defaultDevice: String!
  ) {
    createOrganization(
      name: $name
      externalId: $externalId
      testingFacilityName: $testingFacilityName
      cliaNumber: $cliaNumber
      street: $street
      streetTwo: $streetTwo
      city: $city
      county: $county
      state: $state
      zipCode: $zipCode
      phone: $phone
      email: $email
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
      deviceTypes: $deviceTypes
      defaultDevice: $defaultDevice
    ) {
      internalId
    }
  }
`;

interface Props {
  facilityId: string;
}

const OrganizationFormContainer: any = (props: Props) => {
  const [submitted, setSubmitted] = useState(false);
  const { data, loading, error } = useQuery<DeviceTypes, {}>(
    GET_DEVICES_QUERY,
    {
      fetchPolicy: "no-cache",
    }
  );
  const appInsights = useAppInsightsContext();
  const [createOrganization] = useMutation(CREATE_ORGANIZATION_MUTATION);
  const trackSaveSettings = useTrackEvent(
    appInsights,
    "Save Settings",
    null,
    false
  );

  if (loading) {
    return <p> Loading... </p>;
  }
  if (error) {
    return error;
  }

  if (data === undefined) {
    return <p>Error: device types not found</p>;
  }

  const saveOrganization = (organization: Organization, facility: Facility) => {
    trackSaveSettings(null);
    const provider = facility.orderingProvider;
    createOrganization({
      variables: {
        name: organization.name,
        externalId: organization.externalId,
        testingFacilityName: facility.name,
        cliaNumber: facility.cliaNumber,
        street: facility.street,
        streetTwo: facility.streetTwo,
        city: facility.city,
        county: facility.county,
        state: facility.state,
        zipCode: facility.zipCode,
        phone: facility.phone,
        email: facility.email,
        orderingProviderFirstName: provider.firstName,
        orderingProviderMiddleName: provider.middleName,
        orderingProviderLastName: provider.lastName,
        orderingProviderSuffix: provider.suffix,
        orderingProviderNPI: provider.NPI,
        orderingProviderStreet: provider.street,
        orderingProviderStreetTwo: provider.streetTwo,
        orderingProviderCity: provider.city,
        orderingProviderCounty: provider.county,
        orderingProviderState: provider.state,
        orderingProviderZipCode: provider.zipCode,
        orderingProviderPhone: provider.phone,
        deviceTypes: facility.deviceTypes,
        defaultDevice: facility.defaultDevice,
      },
    }).then(() => {
      let alert = (
        <Alert
          type="success"
          title="Created Organization"
          body="The organization has been created"
        />
      );
      showNotification(toast, alert);
      setSubmitted(true);
    });
  };

  const getFacilityData = (): Facility => {
    const defaultDevice = data.deviceType[0].internalId;
    return {
      id: "",
      name: "",
      cliaNumber: "",
      street: "",
      streetTwo: "",
      city: "",
      county: "",
      state: "",
      zipCode: "",
      phone: "",
      email: "",
      orderingProvider: {
        firstName: "",
        middleName: "",
        lastName: "",
        suffix: "",
        NPI: "",
        street: "",
        streetTwo: "",
        city: "",
        county: "",
        state: "",
        zipCode: "",
        phone: "",
      },
      deviceTypes: [defaultDevice],
      defaultDevice,
    };
  };

  if (submitted) {
    return <Redirect to="/admin" />;
  }

  return (
    <OrganizationForm
      organization={{
        name: "",
        internalId: "",
        externalId: "",
        testingFacility: [getFacilityData()],
      }}
      facility={getFacilityData()}
      deviceOptions={data.deviceType}
      saveOrganization={saveOrganization}
    />
  );
};

export default OrganizationFormContainer;
