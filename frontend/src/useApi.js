import { useCallback, useEffect, useState } from "react";

// fetcher must be a stable function (defined outside components)
export function useApi(fetcher, refreshMs = 0) {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    try {
      setData(await fetcher());
      setError("");
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [fetcher]);

  useEffect(() => {
    load();
    if (!refreshMs) return;
    const timer = setInterval(load, refreshMs);
    return () => clearInterval(timer);
  }, [load, refreshMs]);

  return { data, error, loading, reload: load };
}