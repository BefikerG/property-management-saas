"use client";

import { useParams } from "next/navigation";
import { ModuleTabs } from "@/components/shared/module-tabs";
import { useProperty } from "@/hooks/use-properties";
import { Skeleton } from "@/components/ui/skeleton";

export default function PropertyDetailLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const params = useParams<{ id: string }>();
  const { data: property, isLoading } = useProperty(params.id);

  const tabs = [
    { label: "Overview", href: `/properties/${params.id}` },
    { label: "Units", href: `/properties/${params.id}/units` },
    { label: "Leases", href: `/properties/${params.id}/leases` },
  ];

  return (
    <div className="space-y-4">
      <div>
        {isLoading ? (
          <Skeleton className="h-6 w-48" />
        ) : (
          <h1 className="text-xl font-semibold text-text-primary">{property?.name}</h1>
        )}
        <p className="text-sm text-text-secondary mt-1">
          {property?.address} · {property?.locationCity}
        </p>
      </div>
      <ModuleTabs tabs={tabs} />
      <div className="pt-2">{children}</div>
    </div>
  );
}