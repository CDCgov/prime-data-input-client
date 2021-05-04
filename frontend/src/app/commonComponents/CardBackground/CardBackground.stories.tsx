import { Story, Meta } from "@storybook/react";

import { CardBackground } from "./CardBackground";

export default {
  title: "Components/Card background",
  component: CardBackground,
  argTypes: {
    logo: { control: "boolean" },
  },
} as Meta;

const Template: Story = (args) => <CardBackground {...args} />;

export const WithChildren = Template.bind({});
WithChildren.args = {
  children: (
    <>
      <p>This is some test content</p>
    </>
  ),
};
