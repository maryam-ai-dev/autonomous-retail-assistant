/**
 * Hand-written additions that do not come from the backend OpenAPI spec.
 * Frontend-only state shapes and UI helpers live here.
 */

export type QueryState<T> = {
  data: T | null;
  isLoading: boolean;
  error: QueryError | null;
};

export type QueryError = {
  message: string;
  status?: number;
  notFound: boolean;
  isTimeout: boolean;
};
