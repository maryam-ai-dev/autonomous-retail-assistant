"use client";

import { useEffect, useRef, useState } from "react";
import { RobotState } from "../types";
import { pollState } from "../services/simulation-service";

export function useRobotState(intervalMs: number = 2000) {
  const [state, setState] = useState<RobotState | null>(null);
  const [error, setError] = useState<string | null>(null);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    async function fetchState() {
      try {
        const data = await pollState();
        setState(data);
        setError(null);
      } catch {
        setError("Unable to connect to simulation bridge");
      }
    }

    fetchState();
    intervalRef.current = setInterval(fetchState, intervalMs);

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [intervalMs]);

  return { state, error };
}
