"use client";

import { Sheet, SheetContent, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { TenantForm, type TenantFormValues } from "./tenant-form";
import { useUpdateTenant } from "@/hooks/use-tenants";
import { toast } from "sonner";
import type { TenantProfileResponseDto } from "@/lib/api/generated";

interface Props {
  tenant: TenantProfileResponseDto;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function EditTenantSheet({ tenant, open, onOpenChange }: Props) {
  const updateTenant = useUpdateTenant(tenant.id!);

  async function handleSubmit(values: TenantFormValues) {
    try {
      await updateTenant.mutateAsync(values);
      toast.success("Tenant updated successfully.");
      onOpenChange(false);
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? "Failed to update tenant.");
    }
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent side="right" className="w-full sm:max-w-md">
        <SheetHeader>
          <SheetTitle>Edit Tenant</SheetTitle>
        </SheetHeader>
        <div className="px-4">
          <TenantForm
            defaultValues={tenant}
            onSubmit={handleSubmit}
            submitLabel="Save Changes"
            isSubmitting={updateTenant.isPending}
          />
        </div>
      </SheetContent>
    </Sheet>
  );
}