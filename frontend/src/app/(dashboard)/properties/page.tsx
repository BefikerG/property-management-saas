"use client";

import { useProperties } from "@/hooks/use-properties";
import { Skeleton } from "@/components/ui/skeleton";
import { CreatePropertySheet } from "@/components/properties/create-property-sheet";
import Link from "next/link";
import { Building2 } from "lucide-react";

export default function PropertiesListPage() {
  const { data: propertiesPage, isLoading } = useProperties(0, 100);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-text-primary">Properties</h1>
          <p className="text-sm text-text-secondary mt-1">
            Manage your properties and their structures.
          </p>
        </div>
        <CreatePropertySheet />
      </div>

      <div className="bg-surface border border-border rounded-md p-4 shadow-xs">
        {isLoading ? (
          <div className="space-y-4">
            <Skeleton className="h-12 w-full" />
            <Skeleton className="h-12 w-full" />
            <Skeleton className="h-12 w-full" />
          </div>
        ) : propertiesPage?.content?.length ? (
          <ul className="divide-y divide-border">
            {propertiesPage.content.map((property) => (
              <li key={property.id} className="py-3">
                <Link
                  href={`/properties/${property.id}`}
                  className="flex items-start gap-3 hover:bg-surface-sunken p-2 -mx-2 rounded-md transition-colors"
                >
                  <div className="bg-surface-sunken p-2 rounded-md">
                    <Building2 className="h-icon-sm w-icon-sm text-text-secondary" />
                  </div>
                  <div>
                    <h3 className="text-sm font-medium text-text-primary">{property.name}</h3>
                    <p className="text-xs text-text-secondary mt-0.5">
                      {property.address} · {property.locationCity}
                    </p>
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-sm text-text-muted text-center py-8">
            No properties found. Create your first property to get started.
          </p>
        )}
      </div>
    </div>
  );
}