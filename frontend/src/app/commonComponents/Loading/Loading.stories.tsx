import { Story, Meta } from "@storybook/react";

import Loading from "./index";

export default {
  title: "Components/Loading",
  component: Loading,
  argTypes: {},
} as Meta;

const Template: Story = (args) => <Loading {...args} />;

export const Default = Template.bind({});
Default.args = { message: "Loading" };
