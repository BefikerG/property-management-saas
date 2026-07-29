"use client";

import { useState } from "react";
import { useParams } from "next/navigation";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useProperty } from "@/hooks/use-properties";
import { usePropertyStructures } from "@/hooks/use-structures";
import { usePropertyUnits } from "@/hooks/use-units";
import { computeOccupancyStats } from "@/lib/dashboard-metrics";
import { Skeleton } from "@/components/ui/skeleton";
import { CreateStructureSheet } from "@/components/properties/create-structure-sheet";

export default function PropertyOverviewPage() {
    const params = useParams<{ id: string }>();
    const [structureSheetOpen, setStructureSheetOpen] = useState(false);
    const { data: property, isLoading: propertyLoading } = useProperty(params.id);
    const { data: structures, isLoading: structuresLoading } = usePropertyStructures(params.id);
    const { data: units, isLoading: unitsLoading } = usePropertyUnits(params.id);

    const stats = units ? computeOccupancyStats(units.content ?? []) : null;
    const isLoading = propertyLoading || structuresLoading || unitsLoading;

    return (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="md:col-span-2 bg-surface border border-border rounded-md p-4 shadow-xs">
                <h3 className="text-sm font-semibold text-text-primary mb-3">Description</h3>
                {isLoading ? (
                    <Skeleton className="h-16 w-full" />
                ) : (
                    <p className="text-sm text-text-secondary">
                        {property?.description || "No description provided."}
                    </p>
                )}

                <div className="flex items-center justify-between mt-6 mb-3">
                    <h3 className="text-sm font-semibold text-text-primary">
                        Structural Segments ({structures?.content?.length ?? 0})
                    </h3>
                    <Button
                        size="sm"
                        variant="outline"
                        onClick={() => setStructureSheetOpen(true)}
                    >
                        <Plus className="h-icon-xs w-icon-xs mr-1" />
                        Add Structure
                    </Button>
                </div>

                <CreateStructureSheet
                    propertyId={params.id}
                    open={structureSheetOpen}
                    onOpenChange={setStructureSheetOpen}
                />

                {isLoading ? (
                    <Skeleton className="h-24 w-full" />
                ) : structures?.content?.length ? (
                    <ul className="space-y-1">
                        {structures.content.map((s) => (
                            <li key={s.id} className="text-sm text-text-secondary flex items-center gap-2">
                                <span className="text-xs bg-surface-sunken px-2 py-0.5 rounded-sm">{s.nodeType}</span>
                                {s.name}
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p className="text-sm text-text-muted">No structural segments defined.</p>
                )}
            </div>

            <div className="bg-surface border border-border rounded-md p-4 shadow-xs space-y-4">
                <h3 className="text-sm font-semibold text-text-primary">Occupancy Summary</h3>
                {isLoading || !stats ? (
                    <Skeleton className="h-32 w-full" />
                ) : (
                    <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                            <span className="text-text-secondary">Total Units</span>
                            <span className="font-medium text-text-primary">{stats.total}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-text-secondary">Occupied</span>
                            <span className="font-medium text-info">{stats.occupied}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-text-secondary">Vacant</span>
                            <span className="font-medium text-success">{stats.vacant}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-text-secondary">Maintenance</span>
                            <span className="font-medium text-warning">{stats.maintenance}</span>
                        </div>
                        <div className="pt-2 border-t border-border flex justify-between">
                            <span className="text-text-secondary">Occupancy Rate</span>
                            <span className="font-bold text-text-primary">{stats.occupancyRate}%</span>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}