"use client";

import { useState } from "react";
import { useParams } from "next/navigation";
import { usePropertyUnits, useCreateUnit, useUpdateUnitStatus } from "@/hooks/use-units";
import { usePropertyStructures } from "@/hooks/use-structures";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Plus, DoorClosed } from "lucide-react";
import {
    Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import {
    DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { CreateUnitSheet } from "@/components/properties/create-unit-sheet";
import { toast } from "sonner";

const STATUS_VARIANT: Record<string, string> = {
    VACANT: "bg-success-tint text-success",
    OCCUPIED: "bg-info-tint text-info",
    MAINTENANCE: "bg-warning-tint text-warning",
};

export default function PropertyUnitsPage() {
    const params = useParams<{ id: string }>();
    const { data, isLoading } = usePropertyUnits(params.id);
    const { data: structures } = usePropertyStructures(params.id);
    const updateStatus = useUpdateUnitStatus(params.id);
    const [sheetOpen, setSheetOpen] = useState(false);

    const units = data?.content ?? [];

    async function handleStatusChange(unitId: string, status: "VACANT" | "MAINTENANCE") {
        try {
            await updateStatus.mutateAsync({ unitId, status });
            toast.success(`Unit status updated to ${status}.`);
        } catch (err: any) {
            toast.error(err?.response?.data?.message ?? "Failed to update unit status.");
        }
    }

    return (
        <div className="space-y-4">
            <div className="flex justify-end">
                <Button onClick={() => setSheetOpen(true)}>
                    <Plus className="h-icon-sm w-icon-sm mr-2" />
                    Add Unit
                </Button>
            </div>

            <CreateUnitSheet
                propertyId={params.id}
                structures={structures?.content ?? []}
                open={sheetOpen}
                onOpenChange={setSheetOpen}
            />

            <div className="bg-surface border border-border rounded-md overflow-hidden">
                {isLoading ? (
                    <div className="p-4 space-y-2">
                        {Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-row w-full" />)}
                    </div>
                ) : units.length === 0 ? (
                    <div className="py-16 flex flex-col items-center gap-3">
                        <DoorClosed className="h-icon-xl w-icon-xl text-text-muted" />
                        <p className="text-sm text-text-secondary">No units registered for this property yet.</p>
                    </div>
                ) : (
                    <Table>
                        <TableHeader className="sticky top-0 bg-surface-sunken">
                            <TableRow>
                                <TableHead>Unit</TableHead>
                                <TableHead>Status</TableHead>
                                <TableHead>Specification</TableHead>
                                <TableHead className="text-right">Rent (ETB)</TableHead>
                                <TableHead className="text-right">Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {units.map((unit) => (
                                <TableRow key={unit.id} className="hover:bg-surface-hover">
                                    <TableCell className="font-medium">{unit.unitNumber}</TableCell>
                                    <TableCell>
                                        <Badge className={STATUS_VARIANT[unit.status ?? "VACANT"]}>
                                            {unit.status}
                                        </Badge>
                                    </TableCell>
                                    <TableCell className="text-text-secondary">{unit.specification || "—"}</TableCell>
                                    <TableCell className="text-right font-mono text-sm">
                                        {unit.baselinePrice?.toLocaleString()}
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <DropdownMenu>
                                            <DropdownMenuTrigger
                                                render={
                                                    <button className="text-sm text-text-link hover:underline">
                                                        Actions
                                                    </button>
                                                }
                                            />
                                            <DropdownMenuContent align="end">
                                                {unit.status === "VACANT" && (
                                                    <DropdownMenuItem onClick={() => handleStatusChange(unit.id!, "MAINTENANCE")}>
                                                        Mark as Maintenance
                                                    </DropdownMenuItem>
                                                )}
                                                {unit.status === "MAINTENANCE" && (
                                                    <DropdownMenuItem onClick={() => handleStatusChange(unit.id!, "VACANT")}>
                                                        Mark as Vacant
                                                    </DropdownMenuItem>
                                                )}
                                                {unit.status === "OCCUPIED" && (
                                                    <DropdownMenuItem disabled>
                                                        Terminate lease to change status
                                                    </DropdownMenuItem>
                                                )}
                                            </DropdownMenuContent>
                                        </DropdownMenu>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                )}
            </div>
        </div>
    );
}