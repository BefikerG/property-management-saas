"use client";

import { useQueries } from "@tanstack/react-query";
import { propertiesApi } from "@/lib/api/client";
import { useAllProperties } from "./use-dashboard-data";
import { useMemo } from "react";

/**
 * Fetches units for every property in the portfolio and flattens
 * them into a single array, each unit tagged with its parent
 * property's name for display purposes in the unit matrix.
 */
export function usePortfolioUnits() {
  const { data: properties = [], isLoading: propertiesLoading } = useAllProperties();

  const unitQueries = useQueries({
    queries: properties.map((property) => ({
      queryKey: ["units", property.id],
      queryFn: async () => {
        const { data } = await propertiesApi.getAllUnits({
          propertyId: property.id!,
          page: 0,
          size: 200,
        });
        return (data.content ?? []).map((unit) => ({
          ...unit,
          propertyName: property.name,
        }));
      },
      enabled: !!property.id,
    })),
  });

  const isLoading = propertiesLoading || unitQueries.some((q) => q.isLoading);
  const units = useMemo(
    () => unitQueries.flatMap((q) => q.data ?? []),
    [unitQueries]
  );

  return { units, properties, isLoading };
}