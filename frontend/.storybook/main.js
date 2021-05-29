module.exports = {
  stories: ["../src/**/*.stories.mdx", "../src/**/*.stories.@(js|jsx|ts|tsx)"],
  addons: [
    "@storybook/addon-essentials",
    "@storybook/addon-links",
    "@storybook/addon-a11y",
    "@storybook/preset-create-react-app",
  ],
  webpackFinal: (config) => {
    config.resolve.alias[
      "@microsoft/applicationinsights-react-js"
    ] = require.resolve("../src/stories/__mocks__/appInsights.js");
    config.resolve.alias["./TestTimer"] = require.resolve(
      "../src/stories/__mocks__/TestTimer.ts"
    );
    return config;
  },
};
