import { Story, Meta } from "@storybook/react";

import { appConfig } from "../../../storage/store";

import { PasswordReset } from "./PasswordReset";

export default {
  title: "App/Password reset/Reset password",
  component: PasswordReset,
  argTypes: {},
} as Meta;

appConfig({ ...appConfig(), activationToken: "foo" });

const Template: Story = (args) => <PasswordReset {...args} />;

export const Default = Template.bind({});
Default.args = {};
