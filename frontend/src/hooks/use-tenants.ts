"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { tenantProfilesApi } from "@/lib/api/client";
import type { TenantProfileRequestDto } from "@/lib/api/generated";

export function useTenants(page = 0, size = 20, query?: string) {
  return useQuery({
    queryKey: ["tenants", page, size, query ?? ""],
    queryFn: async () => {
      const { data } = await tenantProfilesApi.getAll({
        page,
        size,
        query: query || undefined,
      });
      return data;
    },
  });
}

export function useTenant(id: string) {
  return useQuery({
    queryKey: ["tenants", id],
    queryFn: async () => {
      const { data } = await tenantProfilesApi.getById({ id });
      return data;
    },
    enabled: !!id,
  });
}

export function useCreateTenant() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: TenantProfileRequestDto) => {
      const { data } = await tenantProfilesApi.create({ tenantProfileRequestDto: payload });
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["tenants"] });
    },
  });
}

export function useUpdateTenant(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: TenantProfileRequestDto) => {
      // Backend endpoint is PUT (full replacement) — renamed from PATCH
      // during the pilot feedback patch (BUG-002). Generated method
      // name reflects this; confirm against generated/api.ts if it
      // differs after any future regeneration.
      const { data } = await tenantProfilesApi.update({ id, tenantProfileRequestDto: payload });
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["tenants"] });
      queryClient.invalidateQueries({ queryKey: ["tenants", id] });
    },
  });
}

export function useDeleteTenant() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: string) => {
      await tenantProfilesApi._delete({ id });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["tenants"] });
    },
  });
}