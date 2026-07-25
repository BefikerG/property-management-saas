"use client";

import { useQuery } from "@tanstack/react-query";
import { propertiesApi, leasesApi, invoicesApi, auditLogApi } from "@/lib/api/client";

/**
 * All dashboard data fetching lives here as individual TanStack Query
 * hooks. Each is independently cached and independently loading —
 * the dashboard page composes them, so one slow widget never blocks
 * the others from rendering.
 */

export function useAllProperties() {
  return useQuery({
    queryKey: ["properties", "all"],
    queryFn: async () => {
      const { data } = await propertiesApi.getAllProperties({ page: 0, size: 100 });
      return data.content ?? [];
    },
  });
}

export function useAllUnitsForProperty(propertyId: string) {
  return useQuery({
    queryKey: ["units", propertyId],
    queryFn: async () => {
      const { data } = await propertiesApi.getAllUnits({ propertyId, page: 0, size: 200 });
      return data.content ?? [];
    },
    enabled: !!propertyId,
  });
}

export function useAllActiveLeases() {
  return useQuery({
    queryKey: ["leases", "active"],
    queryFn: async () => {
      // NOTE: getAll3() maps to LeasesApi's GET /api/v1/leases?status=... endpoint.
      // Numbered suffix is an OpenAPI Generator artifact — the backend's
      // @Operation annotations lack explicit operationId values, causing
      // name collisions with LeasesApi's other GET endpoints. See backlog
      // item: add explicit operationId to LeaseController for stable names.
      const { data } = await leasesApi.getAll3({ status: "ACTIVE", page: 0, size: 200 });
      return data.content ?? [];
    },
  });
}

export function useAllInvoices() {
  return useQuery({
    queryKey: ["invoices", "all"],
    queryFn: async () => {
      // NOTE: getAll4() maps to InvoicesPaymentsApi's GET /api/v1/invoices endpoint.
      // Same numbered-suffix generator artifact as leasesApi.getAll3() above.
      const { data } = await invoicesApi.getAll4({ page: 0, size: 300 });
      return data.content ?? [];
    },
  });
}

export function useRecentAuditLog() {
  return useQuery({
    queryKey: ["audit-log", "recent"],
    queryFn: async () => {
      const { data } = await auditLogApi.getAuditLog({ page: 0, size: 5 });
      return data.content ?? [];
    },
  });
}