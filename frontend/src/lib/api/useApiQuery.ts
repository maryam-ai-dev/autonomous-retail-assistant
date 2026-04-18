"use client";

import axios from "axios";
import { useEffect, useRef, useState } from "react";
import apiClient from "@/core/api/client";
import type { QueryError, QueryState } from "@/types/app";

const USE_MOCKS = process.env.NEXT_PUBLIC_USE_MOCKS === "true";
const DEFAULT_TIMEOUT_MS = 10_000;

function normalizeError(err: unknown): QueryError {
  if (axios.isAxiosError(err)) {
    const status = err.response?.status;
    if (err.code === "ECONNABORTED" || err.code === "ERR_CANCELED") {
      return {
        message: "Request timed out",
        status,
        isTimeout: true,
        notFound: false,
      };
    }
    return {
      message: err.message,
      status,
      isTimeout: false,
      notFound: status === 404,
    };
  }
  if (err instanceof Error) {
    return { message: err.message, notFound: false, isTimeout: false };
  }
  return { message: "Unknown error", notFound: false, isTimeout: false };
}

type UseApiQueryOptions<T> = {
  key: string;
  fetcher: () => Promise<T>;
  mockData: T | null;
  enabled?: boolean;
};

export function useApiQuery<T>({
  key,
  fetcher,
  mockData,
  enabled = true,
}: UseApiQueryOptions<T>): QueryState<T> {
  const fetcherRef = useRef(fetcher);
  // React 19 flags ref mutation during render; we only use current inside effects
  // and it must track the latest closure to avoid stale fetch args.
  // eslint-disable-next-line react-hooks/refs
  fetcherRef.current = fetcher;

  const [state, setState] = useState<QueryState<T>>(() => {
    if (!enabled) return { data: null, isLoading: false, error: null };
    if (USE_MOCKS) return { data: mockData, isLoading: false, error: null };
    return { data: null, isLoading: true, error: null };
  });

  useEffect(() => {
    if (!enabled) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setState({ data: null, isLoading: false, error: null });
      return;
    }
    if (USE_MOCKS) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setState({ data: mockData, isLoading: false, error: null });
      return;
    }
    let cancelled = false;
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setState({ data: null, isLoading: true, error: null });
    fetcherRef.current()
      .then((data) => {
        if (cancelled) return;
        setState({ data, isLoading: false, error: null });
      })
      .catch((err: unknown) => {
        if (cancelled) return;
        setState({
          data: null,
          isLoading: false,
          error: normalizeError(err),
        });
      });
    return () => {
      cancelled = true;
    };
  }, [key, enabled, mockData]);

  return state;
}

export function apiGet<T>(
  url: string,
  params?: Record<string, unknown>,
): Promise<T> {
  return apiClient
    .get<T>(url, { params, timeout: DEFAULT_TIMEOUT_MS })
    .then((response) => response.data);
}
