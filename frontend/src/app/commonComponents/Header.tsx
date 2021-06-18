import React, { useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import classNames from "classnames";
import { v4 as uuidv4 } from "uuid";
import {
  useAppInsightsContext,
  useTrackEvent,
} from "@microsoft/applicationinsights-react-js";

import { PATIENT_TERM_PLURAL_CAP } from "../../config/constants";
import { formatFullName } from "../utils/user";
import siteLogo from "../../img/simplereport-logo-color.svg";
import { hasPermission, appPermissions } from "../permissions";
import { useAppConfig } from "../../hooks/useAppConfig";
import { useFacilities } from "../../hooks/useFacilities";

import Button from "./Button/Button";
import Dropdown from "./Dropdown";
import useComponentVisible from "./ComponentVisible";
import { LinkWithQuery } from "./LinkWithQuery";
import { TrainingNotification } from "./TrainingNotification";

const Header: React.FC<{}> = () => {
  const appInsights = useAppInsightsContext();
  const trackSupport = useTrackEvent(appInsights, "Support", {});

  const handleSupportClick = (e: MouseEvent) => {
    if (appInsights) {
      trackSupport(e);
    }
  };

  const {
    facilities: { selectedFacility, availableFacilities },
  } = useFacilities();
  const {
    config: { organization, user },
  } = useAppConfig();

  const [menuVisible, setMenuVisible] = useState(false);
  const {
    ref: staffDefailsRef,
    isComponentVisible: staffDetailsVisible,
    setIsComponentVisible: setStaffDetailsVisible,
  } = useComponentVisible(false);

  const onFacilitySelect = (e: React.FormEvent<HTMLSelectElement>) => {
    const id = (e.target as HTMLSelectElement).value;
    window.location.href = `${
      window.location.pathname
    }?facility=${encodeURIComponent(id)}`;
  };

  const canViewSettings = hasPermission(
    user.permissions,
    appPermissions.settings.canView
  );

  const canViewPeople = hasPermission(
    user.permissions,
    appPermissions.people.canView
  );

  const canViewResults = hasPermission(
    user.permissions,
    appPermissions.results.canView
  );

  const canViewTestQueue = hasPermission(
    user.permissions,
    appPermissions.tests.canView
  );

  const logout = () => {
    // Fetch the id_token from local storage
    const id_token = localStorage.getItem("id_token");
    const state = uuidv4();
    // Remove auth data from local_storage
    localStorage.removeItem("access_token");
    localStorage.removeItem("id_token");
    window.location.replace(
      "https://hhs-prime.okta.com/oauth2/default/v1/logout" +
        `?id_token_hint=${encodeURIComponent(id_token || "")}` +
        `&post_logout_redirect_uri=${encodeURIComponent(
          process.env.REACT_APP_BASE_URL || ""
        )}` +
        `&state=${state}`
    );
  };

  return (
    <header className="usa-header usa-header--basic">
      {process.env.REACT_APP_IS_TRAINING_SITE === "true" && (
        <TrainingNotification />
      )}
      <div className="usa-nav-container">
        <div className="usa-navbar">
          <div className="usa-logo" id="basic-logo">
            <LinkWithQuery to={`/queue`} title="Home" aria-label="Home">
              <img
                className="width-card desktop:width-full"
                src={siteLogo}
                alt="{process.env.REACT_APP_TITLE}"
              />
            </LinkWithQuery>
            <div className="prime-organization-name">{organization.name}</div>
          </div>
          <button
            onClick={() => setMenuVisible(!menuVisible)}
            className="usa-menu-btn"
          >
            Menu
          </button>
        </div>

        <nav
          aria-label="Primary navigation"
          className={classNames(
            "usa-nav",
            "prime-nav",
            "desktop:display-none",
            {
              "is-visible": menuVisible,
            }
          )}
        >
          <button
            className="fa-layers fa-fw fa-2x usa-nav__close prime-nav-close-button"
            onClick={() => setMenuVisible(false)}
            title={"close menu"}
          >
            <FontAwesomeIcon icon={"window-close"} />
          </button>

          <ul className="usa-nav__primary usa-accordion">
            {canViewTestQueue ? (
              <li className="usa-nav__primary-item prime-staff-infobox-sidemenu prime-settings-hidden">
                <LinkWithQuery
                  to={`/queue`}
                  onClick={() => setMenuVisible(false)}
                  activeClassName="active-nav-item"
                  className="prime-nav-link"
                  activeStyle={{
                    color: "white",
                  }}
                >
                  Conduct Tests
                </LinkWithQuery>
              </li>
            ) : null}
            {canViewResults ? (
              <li className="usa-nav__primary-item prime-staff-infobox-sidemenu prime-settings-hidden">
                <LinkWithQuery
                  to={`/results`}
                  onClick={() => setMenuVisible(false)}
                  activeClassName="active-nav-item"
                  className="prime-nav-link"
                  activeStyle={{
                    color: "white",
                  }}
                >
                  Results
                </LinkWithQuery>
              </li>
            ) : null}
            {canViewPeople ? (
              <li className="usa-nav__primary-item prime-staff-infobox-sidemenu prime-settings-hidden">
                <LinkWithQuery
                  to={`/patients`}
                  onClick={() => setMenuVisible(false)}
                  activeClassName="active-nav-item"
                  className="prime-nav-link"
                  activeStyle={{
                    color: "white",
                  }}
                >
                  {PATIENT_TERM_PLURAL_CAP}
                </LinkWithQuery>
              </li>
            ) : null}
            <li className="usa-nav__primary-item prime-staff-infobox-sidemenu prime-settings-hidden">
              <FontAwesomeIcon
                icon={"user-circle"}
                style={{
                  fill: "white",
                }}
              />
            </li>

            <li className="usa-nav__primary-item usa-sidenav prime-staff-infobox-sidemenu prime-settings-hidden">
              <ul className="usa-sidenav__sublist prime-sidenav_inset">
                <li className="usa-sidenav__item span-full-name">
                  {formatFullName(user)}
                </li>
                <li className="usa-sidenav__item">
                  <span>
                    <strong>Role: </strong>
                    {user.roleDescription}
                  </span>
                </li>
                <li className="usa-sidenav__item">{selectedFacility?.name}</li>
              </ul>
            </li>
            <div>
              <div className="navlink__support">
                <a
                  href="https://simplereport.gov/support"
                  target="none"
                  onClick={() => handleSupportClick}
                >
                  Support
                </a>
              </div>
              <Button variant="unstyled" label="Log out" onClick={logout} />
            </div>
            {canViewSettings ? (
              <li className="usa-nav__primary-item prime-settings-hidden">
                <LinkWithQuery
                  to={`/settings`}
                  onClick={() => setMenuVisible(false)}
                  activeClassName="active-nav-item"
                  activeStyle={{
                    color: "white",
                  }}
                >
                  <FontAwesomeIcon icon={"cog"} /> Settings
                </LinkWithQuery>
              </li>
            ) : null}
          </ul>
        </nav>

        <nav aria-label="Primary navigation" className="usa-nav prime-nav">
          <ul className="usa-nav__primary usa-accordion">
            {canViewTestQueue ? (
              <li className="usa-nav__primary-item">
                <LinkWithQuery
                  to={`/queue`}
                  onClick={() => setMenuVisible(false)}
                  activeClassName="active-nav-item"
                  className="prime-nav-link"
                  id="conduct-test-nav-link"
                  activeStyle={{
                    color: "white",
                  }}
                >
                  Conduct Tests
                </LinkWithQuery>
              </li>
            ) : null}
            {canViewResults ? (
              <li className="usa-nav__primary-item">
                <LinkWithQuery
                  to={`/results`}
                  onClick={() => setMenuVisible(false)}
                  activeClassName="active-nav-item"
                  className="prime-nav-link"
                  id="results-nav-link"
                  activeStyle={{
                    color: "white",
                  }}
                >
                  Results
                </LinkWithQuery>
              </li>
            ) : null}
            {canViewPeople ? (
              <li className="usa-nav__primary-item">
                <LinkWithQuery
                  to={`/patients`}
                  onClick={() => setMenuVisible(false)}
                  activeClassName="active-nav-item"
                  className="prime-nav-link"
                  id="patient-nav-link"
                  activeStyle={{
                    color: "white",
                  }}
                >
                  {PATIENT_TERM_PLURAL_CAP}
                </LinkWithQuery>
              </li>
            ) : null}
          </ul>
          <div className="prime-facility-select">
            <Dropdown
              selectedValue={selectedFacility?.id || ""}
              onChange={onFacilitySelect}
              options={availableFacilities.map(({ name, id }) => ({
                label: name,
                value: id,
              }))}
            />
          </div>
          <ul className="usa-nav__primary usa-accordion">
            <li className="usa-nav__primary-item nav__primary-item-icon">
              <LinkWithQuery
                to={`#`}
                isActive={() => staffDetailsVisible}
                onClick={(e) => {
                  e.preventDefault();
                  setStaffDetailsVisible(!staffDetailsVisible);
                }}
                activeClassName="active-nav-item"
                data-testid="user-button"
              >
                <FontAwesomeIcon
                  icon={"user-circle"}
                  style={{
                    color: staffDetailsVisible ? "white" : "",
                  }}
                />
              </LinkWithQuery>
              <div
                ref={staffDefailsRef}
                aria-label="Primary navigation"
                className={classNames("shadow-3", "prime-staff-infobox", {
                  "is-prime-staff-infobox-visible": staffDetailsVisible,
                })}
              >
                <ul className="usa-sidenav__sublist">
                  <li className="usa-sidenav__item span-full-name">
                    {formatFullName(user)}
                  </li>
                  <li className="usa-sidenav__item">
                    <span>
                      <strong>Role: </strong>
                      {user.roleDescription}
                    </span>
                  </li>
                  <li className="usa-sidenav__item">
                    {selectedFacility?.name}
                  </li>
                  <li className="usa-sidenav__item navlink__support">
                    <a
                      href="https://simplereport.gov/support"
                      target="none"
                      onClick={() => handleSupportClick}
                      data-testid="support-link"
                    >
                      Support
                    </a>
                  </li>
                  <li className="usa-sidenav__item margin-top-2">
                    <Button
                      variant="unstyled"
                      label=" Log out"
                      onClick={logout}
                    />
                  </li>
                </ul>
              </div>
            </li>
            {canViewSettings ? (
              <li className="usa-nav__primary-item nav__primary-item-icon">
                <LinkWithQuery
                  to={`/settings`}
                  onClick={() => setMenuVisible(false)}
                  activeClassName="active-nav-item"
                  activeStyle={{
                    color: "white",
                  }}
                >
                  <FontAwesomeIcon icon={"cog"} />
                </LinkWithQuery>
              </li>
            ) : null}
          </ul>
        </nav>
      </div>
    </header>
  );
};

export default Header;
