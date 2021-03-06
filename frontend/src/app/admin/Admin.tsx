import React from "react";
import { Link } from "react-router-dom";

import { LinkWithQuery } from "../commonComponents/LinkWithQuery";

const Admin = () => {
  return (
    <main className="prime-home">
      <div className="grid-container">
        <div className="grid-row">
          <div className="prime-container card-container">
            <div className="usa-card__header">
              <div>
                <h2>Admin</h2>
              </div>
            </div>
            <div className="usa-card__body">
              <div>
                <LinkWithQuery to={`/admin/pending-organizations`}>
                  Organizations pending identify verification
                </LinkWithQuery>
              </div>
              <div>
                <Link to="/admin/add-organization-admin">
                  Add organization admin
                </Link>
              </div>
              <div>
                <Link to="/admin/create-device-type">
                  Create new device type
                </Link>
              </div>
              <div>
                <Link to="/admin/tenant-data-access">Organization data</Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
};

export default Admin;
