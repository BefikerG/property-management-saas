"use client";

import { useParams } from "next/navigation";
import { useState } from "react";
import { Pencil, Trash2 } from "lucide-react";
import { useRouter } from "next/navigation";
import { useTenant, useDeleteTenant } from "@/hooks/use-tenants";
import { EditTenantSheet } from "@/components/tenants/edit-tenant-sheet";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent,
  AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { toast } from "sonner";

export default function TenantOverviewPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const { data: tenant, isLoading } = useTenant(params.id);
  const deleteTenant = useDeleteTenant();

  const [editOpen, setEditOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);

  async function handleDelete() {
    try {
      await deleteTenant.mutateAsync(params.id);
      toast.success("Tenant deleted.");
      router.push("/tenants");
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? "Failed to delete tenant.");
    }
  }

  if (isLoading || !tenant) {
    return <Skeleton className="h-48 w-full" />;
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div className="md:col-span-2 bg-surface border border-border rounded-md p-4 shadow-xs">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-semibold text-text-primary">Contact Information</h3>
          <div className="flex gap-2">
            <Button size="sm" variant="outline" onClick={() => setEditOpen(true)}>
              <Pencil className="h-icon-xs w-icon-xs mr-1" />
              Edit
            </Button>
            <Button size="sm" variant="destructive" onClick={() => setDeleteOpen(true)}>
              <Trash2 className="h-icon-xs w-icon-xs mr-1" />
              Delete
            </Button>
          </div>
        </div>

        <dl className="space-y-3 text-sm">
          <div className="flex justify-between">
            <dt className="text-text-secondary">Full Name</dt>
            <dd className="text-text-primary font-medium">{tenant.fullName}</dd>
          </div>
          <div className="flex justify-between">
            <dt className="text-text-secondary">Email</dt>
            <dd className="text-text-primary font-medium">{tenant.email}</dd>
          </div>
          <div className="flex justify-between">
            <dt className="text-text-secondary">Phone</dt>
            <dd className="text-text-primary font-medium">{tenant.phone || "—"}</dd>
          </div>
          <div className="flex justify-between">
            <dt className="text-text-secondary">ID Reference</dt>
            <dd className="text-text-primary font-medium">{tenant.identificationReference || "—"}</dd>
          </div>
          <div className="flex justify-between pt-3 border-t border-border">
            <dt className="text-text-secondary">Registered On</dt>
            <dd className="text-text-secondary">
              {tenant.createdAt && new Date(tenant.createdAt).toLocaleDateString()}
            </dd>
          </div>
        </dl>
      </div>

      <EditTenantSheet tenant={tenant} open={editOpen} onOpenChange={setEditOpen} />

      <AlertDialog open={deleteOpen} onOpenChange={setDeleteOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete this tenant?</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently remove {tenant.fullName} from your tenant directory.
              This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              className="bg-danger text-white hover:bg-danger/90"
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}