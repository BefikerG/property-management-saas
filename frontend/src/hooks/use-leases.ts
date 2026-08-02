"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { leasesApi } from "@/lib/api/client";
import type { LeaseRequestDto, LeaseResponseDto } from "@/lib/api/generated";

export type LeaseStatusFilter = "DRAFT" | "PENDING" | "ACTIVE" | "TERMINATED" | "EXPIRED";

export function useLeases(page = 0, size = 20, status?: LeaseStatusFilter) {
  return useQuery({
    queryKey: ["leases", page, size, status ?? "ALL"],
    queryFn: async () => {
      // NOTE: getAll3() is an OpenAPI Generator numeric-suffix artifact —
      // see Step 6 commit for full explanation. Confirmed mapping:
      // LeasesApi.getAll3() -> GET /api/v1/leases
      const { data } = await leasesApi.getAll3({
        page,
        size,
        status: status ?? undefined,
      });
      return data;
    },
  });
}

export function useLease(id: string) {
  return useQuery({
    queryKey: ["leases", id],
    queryFn: async () => {
      const { data } = await leasesApi.getById3({ id });
      return data;
    },
    enabled: !!id,
  });
}

/**
 * Invalidates both leases and units queries — lease lifecycle actions
 * (activate/terminate/expire) atomically change unit status on the
 * backend, so any cached unit list anywhere in the app is now stale.
 * The ["units"] prefix match invalidates every per-property units
 * query registered via usePortfolioUnits (Step 6) and usePropertyUnits
 * (Step 7) simultaneously.
 */
function invalidateLeaseAndUnitCaches(queryClient: ReturnType<typeof useQueryClient>, leaseId?: string) {
  queryClient.invalidateQueries({ queryKey: ["leases"] });
  queryClient.invalidateQueries({ queryKey: ["units"] });
  if (leaseId) {
    queryClient.invalidateQueries({ queryKey: ["leases", leaseId] });
  }
}

export function useCreateLease() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: LeaseRequestDto) => {
      const { data } = await leasesApi.create2({ leaseRequestDto: payload });
      return data;
    },
    onSuccess: () => invalidateLeaseAndUnitCaches(queryClient),
  });
}

export function useActivateLease() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: string) => {
      const { data } = await leasesApi.activate({ id });
      return data;
    },
    onSuccess: (_data, id) => invalidateLeaseAndUnitCaches(queryClient, id),
  });
}

export function useTerminateLease() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: string) => {
      const { data } = await leasesApi.terminate({ id });
      return data;
    },
    onSuccess: (_data, id) => invalidateLeaseAndUnitCaches(queryClient, id),
  });
}

export function useExpireLease() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: string) => {
      const { data } = await leasesApi.expire({ id });
      return data;
    },
    onSuccess: (_data, id) => invalidateLeaseAndUnitCaches(queryClient, id),
  });
}