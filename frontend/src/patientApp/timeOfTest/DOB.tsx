import React, { FormEvent, useEffect, useState, useRef } from "react";
import { Redirect } from "react-router-dom";
import moment from "moment";

import Button from "../../app/commonComponents/Button/Button";
import TextInput from "../../app/commonComponents/TextInput";
import { PxpApi } from "../PxpApiService";
import Alert from "../../app/commonComponents/Alert";
import { usePatient } from "../../hooks/usePatient";
import { useAppConfig } from "../../hooks/useAppConfig";

const DOB = () => {
  const [birthDate, setBirthDate] = useState("");
  const [birthDateError, setBirthDateError] = useState("");
  const [linkExpiredError, setLinkExpiredError] = useState(false);
  const dobRef = useRef<HTMLInputElement>(null);

  const { patient, setCurrentPatient } = usePatient();
  const {
    config: { plid },
    updateOrganizationName,
  } = useAppConfig();

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    dobRef?.current?.focus();
  }, []);

  const confirmBirthDate = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const date = moment(birthDate.replace("/", ""), "MMDDYYYY");
    if (!date.isValid()) {
      setBirthDateError("Enter your date of birth");
      dobRef?.current?.focus();
      return;
    }

    setLoading(true);
    try {
      const response = await PxpApi.validateDateOfBirth(
        plid as string,
        date.format("YYYY-MM-DD")
      );
      updateOrganizationName(response.organizationName);
      setCurrentPatient(response);
    } catch (error) {
      if (error?.status === 410) {
        setLinkExpiredError(true);
      } else {
        setBirthDateError(
          "No patient link with the supplied ID was found, or the birth date provided was incorrect."
        );
      }
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <main>
        <div className="grid-container maxw-tablet">
          <p className="margin-top-3">Validating birth date...</p>
        </div>
      </main>
    );
  }
  if (patient?.orderStatus === "COMPLETED") {
    return (
      <Redirect
        to={{
          pathname: "/test-result",
          search: `?plid=${plid}`,
        }}
      />
    );
  } else if (patient?.firstName) {
    return (
      <Redirect
        to={{
          pathname: "/patient-info-confirm",
          search: `?plid=${plid}`,
        }}
      />
    );
  }

  return (
    <>
      <main>
        <div className="grid-container maxw-tablet">
          {!linkExpiredError ? (
            <>
              <p className="margin-top-3">
                Enter your date of birth to access your COVID-19 Testing Portal.
              </p>
              <form className="usa-form" onSubmit={confirmBirthDate}>
                <TextInput
                  label={"Date of birth"}
                  name={"birthDate"}
                  type={"password"}
                  autoComplete={"on"}
                  value={birthDate}
                  size={8}
                  pattern={"([0-9]{1,2}/[0-9]{1,2}/[0-9]{4})|([0-9]{8})"}
                  inputMode={"numeric"}
                  ariaDescribedBy={"bdayFormat"}
                  hintText={"MM/DD/YYYY or MMDDYYYY"}
                  errorMessage={birthDateError}
                  validationStatus={birthDateError ? "error" : undefined}
                  onChange={(evt) => setBirthDate(evt.currentTarget.value)}
                  inputRef={dobRef}
                />
                <Button
                  id="dob-submit-button"
                  label={"Continue"}
                  type={"submit"}
                />
              </form>
            </>
          ) : (
            <>
              <p></p>
              <Alert
                type="error"
                title="Link expired"
                body="This link has expired. Please contact your provider."
              />
            </>
          )}
        </div>
      </main>
    </>
  );
};

export default DOB;
