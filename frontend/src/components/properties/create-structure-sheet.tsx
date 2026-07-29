"use client";

import { useForm, Controller, type Resolver } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Sheet, SheetContent, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Field, FieldLabel, FieldError, FieldGroup } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import { useCreateStructure } from "@/hooks/use-structures";
import { toast } from "sonner";

const structureSchema = z.object({
  nodeType: z.enum(["BLOCK", "FLOOR", "WING", "ZONE"], {
    // NOTE: Zod v4 renamed required_error → error
    error: "Structure type is required",
  }),
  name: z.string().min(1, "Name is required").max(100),
  ordinal: z.coerce.number().int().min(0).optional(),
});

type StructureFormValues = z.infer<typeof structureSchema>;

interface Props {
  propertyId: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const NODE_TYPE_LABELS: Record<string, string> = {
  BLOCK: "Block",
  FLOOR: "Floor",
  WING: "Wing",
  ZONE: "Zone",
};

export function CreateStructureSheet({ propertyId, open, onOpenChange }: Props) {
  if (!propertyId) {
    console.error("CreateStructureSheet rendered without a valid propertyId");
    return null;
  }

  const createStructure = useCreateStructure(propertyId);

  const {
    register,
    handleSubmit,
    control,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<StructureFormValues>({
    // Cast required: z.coerce.number() makes zodResolver infer ordinal
    // as 'unknown' (pre-coercion input type) instead of 'number | undefined'.
    // Same pattern as create-unit-sheet.tsx. Safe — coercion runs at validation time.
    resolver: zodResolver(structureSchema) as Resolver<StructureFormValues>,
  });

  async function onSubmit(values: StructureFormValues) {
    try {
      await createStructure.mutateAsync({
        nodeType: values.nodeType,
        name: values.name,
        ordinal: values.ordinal,
      });
      toast.success("Structure added successfully.");
      reset();
      onOpenChange(false);
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? "Failed to add structure.");
    }
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent side="right" className="w-full sm:max-w-md">
        <SheetHeader>
          <SheetTitle>Add Structural Segment</SheetTitle>
        </SheetHeader>
        <div className="px-4">
          <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
            <FieldGroup>
              <Field data-invalid={!!errors.nodeType}>
                <FieldLabel htmlFor="nodeType">Type</FieldLabel>
                <Controller
                  control={control}
                  name="nodeType"
                  render={({ field }) => (
                    <Select value={field.value ?? ""} onValueChange={field.onChange}>
                      <SelectTrigger id="nodeType" aria-invalid={!!errors.nodeType}>
                        <SelectValue placeholder="Select a type" />
                      </SelectTrigger>
                      <SelectContent>
                        {Object.entries(NODE_TYPE_LABELS).map(([value, label]) => (
                          <SelectItem key={value} value={value}>
                            {label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                <FieldError errors={errors.nodeType ? [errors.nodeType] : undefined} />
              </Field>

              <Field data-invalid={!!errors.name}>
                <FieldLabel htmlFor="name">Name</FieldLabel>
                <Input id="name" placeholder="Ground Floor" {...register("name")} aria-invalid={!!errors.name} />
                <FieldError errors={errors.name ? [errors.name] : undefined} />
              </Field>

              <Field>
                <FieldLabel htmlFor="ordinal">Display Order (optional)</FieldLabel>
                <Input id="ordinal" type="number" placeholder="0" {...register("ordinal")} />
              </Field>

              <Button type="submit" className="w-full" disabled={isSubmitting}>
                {isSubmitting ? "Adding..." : "Add Structure"}
              </Button>
            </FieldGroup>
          </form>
        </div>
      </SheetContent>
    </Sheet>
  );
}