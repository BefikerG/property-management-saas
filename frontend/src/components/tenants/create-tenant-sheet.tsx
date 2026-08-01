"use client";

import { useState } from "react";
import { Sheet, SheetContent, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { Plus } from "lucide-react";
import { TenantForm, type TenantFormValues } from "./tenant-form";
import { useCreateTenant } from "@/hooks/use-tenants";
import { toast } from "sonner";

export function CreateTenantSheet() {
  const [open, setOpen] = useState(false);
  const createTenant = useCreateTenant();

  async function handleSubmit(values: TenantFormValues) {
    try {
      await createTenant.mutateAsync(values);
      toast.success("Tenant registered successfully.");
      setOpen(false);
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? "Failed to register tenant.");
    }
  }

  return (
    <Sheet open={open} onOpenChange={setOpen}>
      <Button onClick={() => setOpen(true)}>
        <Plus className="h-icon-sm w-icon-sm mr-2" />
        Register Tenant
      </Button>
      <SheetContent side="right" className="w-full sm:max-w-md">
        <SheetHeader>
          <SheetTitle>Register Tenant</SheetTitle>
        </SheetHeader>
        <div className="px-4">
          <TenantForm onSubmit={handleSubmit} submitLabel="Register Tenant" isSubmitting={createTenant.isPending} />
        </div>
      </SheetContent>
    </Sheet>
  );
}