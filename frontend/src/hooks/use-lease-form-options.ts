"use client";

import { useMemo } from "react";
import { usePortfolioUnits } from "./use-portfolio-units";
import { useTenants } from "./use-tenants";

/**
 * Supplies the two dropdown option sets needed by the lease creation
 * form: VACANT units across the entire portfolio, and the full tenant
 * directory. Both reuse existing hooks — no new backend queries.
 */
export function useLeaseFormOptions() {
  const { units, isLoading: unitsLoading } = usePortfolioUnits();
  const { data: tenantsPage, isLoading: tenantsLoading } = useTenants(0, 200);

  const vacantUnits = useMemo(
    () => units.filter((u) => u.status === "VACANT"),
    [units]
  );

  return {
    vacantUnits,
    tenants: tenantsPage?.content ?? [],
    isLoading: unitsLoading || tenantsLoading,
  };
}