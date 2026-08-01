"use client";

import { useParams } from "next/navigation";
import { ModuleTabs } from "@/components/shared/module-tabs";
import { useTenant } from "@/hooks/use-tenants";
import { Skeleton } from "@/components/ui/skeleton";

export default function TenantDetailLayout({ children }: { children: React.ReactNode }) {
  const params = useParams<{ id: string }>();
  const { data: tenant, isLoading } = useTenant(params.id);

  const tabs = [
    { label: "Overview", href: `/tenants/${params.id}` },
    { label: "Lease History", href: `/tenants/${params.id}/leases` },
  ];

  return (
    <div className="space-y-4">
      <div>
        {isLoading ? (
          <Skeleton className="h-6 w-48" />
        ) : (
          <h1 className="text-xl font-semibold text-text-primary">{tenant?.fullName}</h1>
        )}
        <p className="text-sm text-text-secondary mt-1">{tenant?.email}</p>
      </div>
      <ModuleTabs tabs={tabs} />
      <div className="pt-2">{children}</div>
    </div>
  );
}