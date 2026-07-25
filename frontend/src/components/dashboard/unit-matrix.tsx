"use client";

import { cn } from "@/lib/utils";
import { Skeleton } from "@/components/ui/skeleton";
import type { UnitResponseDto } from "@/lib/api/generated";

interface UnitWithProperty extends UnitResponseDto {
  propertyName?: string;
}

const STATUS_STYLES: Record<string, string> = {
  VACANT: "bg-success-tint text-success border-success/20",
  OCCUPIED: "bg-info-tint text-info border-info/20",
  MAINTENANCE: "bg-warning-tint text-warning border-warning/20",
};

export function UnitMatrix({
  units,
  isLoading,
}: {
  units: UnitWithProperty[];
  isLoading?: boolean;
}) {
  if (isLoading) {
    return (
      <div className="grid grid-cols-6 gap-2">
        {Array.from({ length: 18 }).map((_, i) => (
          <Skeleton key={i} className="h-16 w-full" />
        ))}
      </div>
    );
  }

  if (units.length === 0) {
    return (
      <div className="py-12 text-center text-sm text-text-muted">
        No units registered yet. Add a property and units to see your portfolio here.
      </div>
    );
  }

  const grouped = units.reduce<Record<string, UnitWithProperty[]>>((acc, unit) => {
    const key = unit.propertyName ?? "Unassigned";
    (acc[key] ??= []).push(unit);
    return acc;
  }, {});

  return (
    <div className="space-y-6 max-h-[520px] overflow-y-auto pr-1">
      {Object.entries(grouped).map(([propertyName, propertyUnits]) => (
        <div key={propertyName}>
          <h3 className="text-sm font-medium text-text-primary mb-2">{propertyName}</h3>
          <div className="grid grid-cols-4 sm:grid-cols-6 gap-2">
            {propertyUnits.map((unit) => (
              <div
                key={unit.id}
                title={`Unit ${unit.unitNumber} — ${unit.status}`}
                className={cn(
                  "border rounded-md px-2 py-2 text-xs cursor-default transition-colors duration-fast",
                  STATUS_STYLES[unit.status ?? "VACANT"]
                )}
              >
                <div className="font-semibold truncate">{unit.unitNumber}</div>
                <div className="opacity-80 truncate">{unit.status}</div>
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}