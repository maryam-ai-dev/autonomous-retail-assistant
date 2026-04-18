"use client";

import { MOCK_CONNECTOR_STATUS } from "@/lib/mock/connector-status";
import type { components } from "@/types/api.generated";
import type { QueryError } from "@/types/app";
import { apiGet, useApiQuery } from "./useApiQuery";

type ConnectorStatus = components["schemas"]["ConnectorStatusDto"];

const DEV_TOOLS_ENABLED = process.env.NEXT_PUBLIC_DEV_TOOLS === "true";

type UseConnectorStatusResult = {
  retailers: ConnectorStatus[];
  isLoading: boolean;
  error: QueryError | null;
};

export function useConnectorStatus(): UseConnectorStatusResult {
  const state = useApiQuery<ConnectorStatus[]>({
    key: "connector-status",
    fetcher: () => apiGet<ConnectorStatus[]>("/api/admin/connectors/status"),
    mockData: MOCK_CONNECTOR_STATUS,
    enabled: DEV_TOOLS_ENABLED,
  });
  return {
    retailers: state.data ?? [],
    isLoading: state.isLoading,
    error: state.error,
  };
}
