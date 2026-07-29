"use client";

import { useState } from "react";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { Plus } from "lucide-react";
import { PropertyForm, type PropertyFormValues } from "./property-form";
import { useCreateProperty } from "@/hooks/use-properties";
import { toast } from "sonner";

export function CreatePropertySheet() {
  const [open, setOpen] = useState(false);
  const createProperty = useCreateProperty();

  async function handleSubmit(values: PropertyFormValues) {
    try {
      await createProperty.mutateAsync(values);
      toast.success("Property created successfully.");
      setOpen(false);
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? "Failed to create property.");
    }
  }

  return (
    <Sheet open={open} onOpenChange={setOpen}>
      <Button onClick={() => setOpen(true)}>
        <Plus className="h-icon-sm w-icon-sm mr-2" />
        Add Property
      </Button>
      <SheetContent side="right" className="w-full sm:max-w-md">
        <SheetHeader>
          <SheetTitle>Add Property</SheetTitle>
        </SheetHeader>
        <div className="px-4">
          <PropertyForm
            onSubmit={handleSubmit}
            submitLabel="Create Property"
            isSubmitting={createProperty.isPending}
          />
        </div>
      </SheetContent>
    </Sheet>
  );
}