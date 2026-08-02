"use client";

import { useState } from "react";
import { MoreHorizontal } from "lucide-react";
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent,
  AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { useActivateLease, useTerminateLease, useExpireLease } from "@/hooks/use-leases";
import { toast } from "sonner";
import type { LeaseResponseDto } from "@/lib/api/generated";

/**
 * Renders the correct set of lifecycle actions for a lease based on
 * its current status — mirrors the backend's own state machine
 * guards exactly, so a user never sees an action that would fail.
 */
export function LeaseActionsMenu({ lease }: { lease: LeaseResponseDto }) {
  const [confirmAction, setConfirmAction] = useState<"terminate" | "expire" | null>(null);

  const activateLease = useActivateLease();
  const terminateLease = useTerminateLease();
  const expireLease = useExpireLease();

  async function handleActivate() {
    try {
      await activateLease.mutateAsync(lease.id!);
      toast.success("Lease activated. Unit is now OCCUPIED.");
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? "Failed to activate lease.");
    }
  }

  async function handleConfirmedAction() {
    try {
      if (confirmAction === "terminate") {
        await terminateLease.mutateAsync(lease.id!);
        toast.success("Lease terminated. Unit is now VACANT.");
      } else if (confirmAction === "expire") {
        await expireLease.mutateAsync(lease.id!);
        toast.success("Lease marked as expired. Unit is now VACANT.");
      }
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? "Action failed.");
    } finally {
      setConfirmAction(null);
    }
  }

  const canActivate = lease.status === "DRAFT" || lease.status === "PENDING";
  const canTerminateOrExpire = lease.status === "ACTIVE";

  if (!canActivate && !canTerminateOrExpire) {
    return <span className="text-xs text-text-muted">No actions available</span>;
  }

  return (
    <>
      <DropdownMenu>
        <DropdownMenuTrigger
          render={
            <button className="h-control-sm w-control-sm flex items-center justify-center rounded-md hover:bg-surface-hover">
              <MoreHorizontal className="h-icon-sm w-icon-sm text-text-muted" />
            </button>
          }
        />
        <DropdownMenuContent align="end">
          {canActivate && (
            <DropdownMenuItem onClick={handleActivate}>Activate Lease</DropdownMenuItem>
          )}
          {canTerminateOrExpire && (
            <>
              <DropdownMenuItem onClick={() => setConfirmAction("terminate")}>
                Terminate Lease
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setConfirmAction("expire")}>
                Mark as Expired
              </DropdownMenuItem>
            </>
          )}
        </DropdownMenuContent>
      </DropdownMenu>

      <AlertDialog open={!!confirmAction} onOpenChange={(open) => !open && setConfirmAction(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {confirmAction === "terminate" ? "Terminate this lease?" : "Mark this lease as expired?"}
            </AlertDialogTitle>
            <AlertDialogDescription>
              This will immediately release the unit back to VACANT status,
              making it available for a new lease. This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleConfirmedAction} className="bg-danger text-white hover:bg-danger/90">
              Confirm
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}