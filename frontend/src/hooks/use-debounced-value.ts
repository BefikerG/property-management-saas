"use client";

import { useEffect, useState } from "react";

/**
 * Returns a debounced copy of the given value, updating only after
 * the value has stopped changing for `delayMs`. Used to avoid firing
 * a backend search query on every keystroke.
 */
export function useDebouncedValue<T>(value: T, delayMs = 300): T {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timeout = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(timeout);
  }, [value, delayMs]);

  return debounced;
}