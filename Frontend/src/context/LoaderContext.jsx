import React, { createContext, useContext, useState } from "react";
import GlobalLoader from "../components/GlobalLoader";

const LoaderContext = createContext();

export function LoaderProvider({ children }) {
  const [loading, setLoading] = useState(false);

  const showLoader = () => setLoading(true);
  const hideLoader = () => setLoading(false);

  return (
    <LoaderContext.Provider value={{ showLoader, hideLoader }}>
      {children}
      <GlobalLoader show={loading} />
    </LoaderContext.Provider>
  );
}

export const useLoader = () => useContext(LoaderContext);
