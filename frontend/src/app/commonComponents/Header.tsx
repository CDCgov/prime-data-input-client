import React, { useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { PATIENT_TERM_PLURAL_CAP } from "../../config/constants";
import classNames from "classnames";
import { v4 as uuidv4 } from "uuid";
import useComponentVisible from "./ComponentVisible";
import Dropdown from "./Dropdown";
import { useSelector } from "react-redux";
import { connect } from "react-redux";
import Button from "./Button";
import { formatFullName } from "../utils/user";
import siteLogo from "../../img/simplereport-logo-color.svg";
import { hasPermission, appPermissions } from "../permissions";
import { LinkWithQuery } from "./LinkWithQuery";

const Header: React.FC<{}> = () => {
  const organization = useSelector(
    (state) => (state as any).organization as Organization
  );
  const facilities = useSelector(
    (state) => ((state as any).facilities as Facility[]) || []
  );
  const facility = useSelector(
    (state) => ((state as any).facility as Facility) || { id: "", name: "" }
  );
  const user = useSelector((state) => (state as any).user as User);
  const [menuVisible, setMenuVisible] = useState(false);
  const {
    ref: staffDefailsRef,
    isComponentVisible: staffDetailsVisible,
    setIsComponentVisible: setStaffDetailsVisible,
  } = useComponentVisible(false);

  const onFacilitySelect = (e: React.FormEvent<HTMLSelectElement>) => {
    const id = (e.target as HTMLSelectElement).value;
    window.location.href = `${window.location.pathname}?facility=${encodeURIComponent(id)}`;
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
      "https://hhs-prime.okta.com/oauth2/default/v1/logout"
        + `?id_token_hint=${encodeURIComponent(id_token || "")}`
        + `&post_logout_redirect_uri=${encodeURIComponent(process.env.REACT_APP_OKTA_URL || "")}`
        + `&state=${state}`
    );
  };

  return (
    <header className="usa-header usa-header--basic">
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
                <li className="usa-sidenav__item">{facility.name}</li>
                <li className="usa-sidenav__item">
                  <Button variant="unstyled" label="Log out" onClick={logout} />
                </li>
              </ul>
            </li>

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
              selectedValue={facility.id}
              onChange={onFacilitySelect}
              options={facilities.map(({ name, id }) => ({
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
                  <li className="usa-sidenav__item">{facility.name}</li>
                  <li className="usa-sidenav__item">
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

export default connect()(Header);
