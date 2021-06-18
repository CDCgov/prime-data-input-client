import React, { useState } from "react";
import { Redirect, RouteComponentProps, withRouter } from "react-router";
import classnames from "classnames";

import Button from "../../app/commonComponents/Button/Button";
import { useAppConfig } from "../../hooks/useAppConfig";

import ToS from "./ToS";

interface Props extends RouteComponentProps {
  className?: string;
  onAgree?: () => void;
}

const TermsOfService: React.FunctionComponent<Props> = ({
  className,
  onAgree,
}) => {
  const [nextPage, setNextPage] = useState(false);
  const {
    config: { plid },
  } = useAppConfig();

  if (nextPage) {
    return (
      <Redirect
        to={{
          pathname: "/birth-date-confirmation",
          search: `?plid=${plid}`,
        }}
      />
    );
  }

  return (
    <main
      className={classnames(
        "patient-app padding-bottom-4 bg-base-lightest",
        className
      )}
    >
      <form className="grid-container maxw-tablet usa-prose">
        <h1 className="font-heading-lg margin-top-3">Terms of service</h1>
        <p className="margin-top-105">
          This testing site uses{" "}
          <a href="https://simplereport.gov/">SimpleReport</a> to manage
          COVID-19 testing and reporting. The terms below explain SimpleReport’s
          policies and terms of service.
        </p>
        <div className="tos-content prime-formgroup usa-prose height-card-lg overflow-x-hidden font-body-3xs">
          <ToS />
        </div>
        <p>By agreeing, you consent to our terms of service.</p>
        <Button
          id="tos-consent-button"
          label="I agree"
          onClick={() => {
            if (onAgree) {
              onAgree();
            } else {
              setNextPage(true);
            }
          }}
        />
      </form>
    </main>
  );
};

export default withRouter(TermsOfService);
