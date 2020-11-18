import {
  ApolloClient,
  ApolloProvider,
  InMemoryCache
} from "@apollo/client"
import React from "react";
import ReactDOM from "react-dom";
import { Provider } from "react-redux";

import App from "./app";
import * as serviceWorker from "./serviceWorker";
import "./styles/App.css";
import { store, persistor } from "./app/store";
import { PersistGate } from "redux-persist/integration/react";

const cache = new InMemoryCache({});
const client = new ApolloClient({cache, uri: process.env.REACT_APP_BACKEND_URL})

ReactDOM.render(
  <ApolloProvider client={client}>
    <React.StrictMode>
      <Provider store={store}>
        <PersistGate loading={null} persistor={persistor}>
          <App />
        </PersistGate>
      </Provider>
    </React.StrictMode>
  </ApolloProvider>,
  document.getElementById("root")
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
