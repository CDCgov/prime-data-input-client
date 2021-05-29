import { graphql, GraphQLHandler, GraphQLRequest } from "msw";
import {
  ApolloClient,
  HttpLink,
  InMemoryCache,
  ApolloProvider,
} from "@apollo/client";

const mocks = {
  EditQueueItem: graphql.mutation("EditQueueItem", (req, res, ctx) => {
    return res(
      ctx.data({
        editQueueItem: {
          result: req.body?.variables.result || null,
          dateTested: req.body?.variables.dateTested || null,
          deviceType: {
            internalId: null,
            testLength: "0.1",
            ...req.body?.variables.deviceType,
          },
        },
      })
    );
  }),
  SubmitTestResults: graphql.mutation("SubmitTestResults", (req, res, ctx) => {
    return res(
      ctx.data({
        submitTestResults: {
          internalId: req.body?.variables.patientId,
        },
      })
    );
  }),
  GetPatientsLastResult: graphql.query(
    "GetPatientsLastResult",
    (req, res, ctx) => {
      return res(ctx.data({ patient: { lastTest: {} } }));
    }
  ),
  SendPatientLinkSms: graphql.mutation("sendPatientLinkSms", (req, res, ctx) =>
    res(ctx.data({}))
  ),
  UpdateAOE: graphql.mutation("UpdateAOE", (req, res, ctx) => {
    return res(ctx.data({}));
  }),
  RemovePatientFromQueue: graphql.mutation(
    "RemovePatientFromQueue",
    (req, res, ctx) => res(ctx.data({}))
  ),
};

export const getMocks = (
  ...names: (keyof typeof mocks)[]
): GraphQLHandler<GraphQLRequest<any>>[] => names.map((name) => mocks[name]);

const cache = new InMemoryCache();
const link = new HttpLink({
  uri: "http://localhost:3000/graphql",
  fetch: (...args) => fetch(...args),
});
const client = new ApolloClient({
  cache,
  link,
});
export const StoryGraphQLProvider: React.FC = ({ children }) => (
  <ApolloProvider client={client}>{children}</ApolloProvider>
);
