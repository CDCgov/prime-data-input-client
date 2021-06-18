import { useReactiveVar } from "@apollo/client";

import { appConfig } from "../storage/store";

export const useAppConfig = () => {
  const config = useReactiveVar(appConfig);
  const setUser = (user: User) => {
    appConfig({
      ...config,
      user,
    });
  };
  const updateOrganizationName = (organizationName: string) => {
    appConfig({
      ...config,
      organization: {
        ...config.organization,
        name: organizationName,
      },
    });
  };
  const setActivationToken = (activationToken: string | null) => {
    appConfig({ ...config, activationToken });
  };
  const setInitialData = (data: AppConfigState) => {
    appConfig(data);
  };
  const updateConfigField = (field: string, value: any) => {
    appConfig({ ...config, [field]: value });
  };
  return {
    config,
    setUser,
    setInitialData,
    updateConfigField,
    setActivationToken,
    updateOrganizationName,
  };
};
