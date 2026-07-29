"use client";

import { useForm, Controller, type Resolver } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Sheet, SheetContent, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Field, FieldLabel, FieldError, FieldGroup } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import { useCreateUnit } from "@/hooks/use-units";
import { toast } from "sonner";
import type { PropertyStructureResponseDto } from "@/lib/api/generated";

const unitSchema = z.object({
  unitNumber: z.string().min(1, "Unit number is required").max(50),
  specification: z.string().max(2000).optional(),
  baselinePrice: z.coerce.number().min(0.01, "Baseline price must be greater than zero"),
  structureId: z.string().optional(),
});

type UnitFormValues = z.infer<typeof unitSchema>;

interface Props {
  propertyId: string;
  structures: PropertyStructureResponseDto[];
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function CreateUnitSheet({ propertyId, structures, open, onOpenChange }: Props) {
  if (!propertyId) {
    console.error("CreateUnitSheet rendered without a valid propertyId");
    return null;
  }

  const createUnit = useCreateUnit(propertyId);

  const {
    register,
    handleSubmit,
    control,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<UnitFormValues>({
    // Cast required: z.coerce.number() makes zodResolver infer baselinePrice
    // as 'unknown' (the pre-coercion input type) rather than 'number' (the
    // output type). The cast is safe — coercion still runs at validation time.
    resolver: zodResolver(unitSchema) as Resolver<UnitFormValues>,
  });

  async function onSubmit(values: UnitFormValues) {
    try {
      await createUnit.mutateAsync({
        unitNumber: values.unitNumber,
        specification: values.specification,
        baselinePrice: values.baselinePrice,
        currencyCode: "ETB",
        structureId: values.structureId || undefined,
      });
      toast.success("Unit created successfully.");
      reset();
      onOpenChange(false);
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? "Failed to create unit.");
    }
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent side="right" className="w-full sm:max-w-md">
        <SheetHeader>
          <SheetTitle>Add Unit</SheetTitle>
        </SheetHeader>
        <div className="px-4">
          <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
            <FieldGroup>
              <Field data-invalid={!!errors.unitNumber}>
                <FieldLabel htmlFor="unitNumber">Unit Number</FieldLabel>
                <Input id="unitNumber" placeholder="GF-01" {...register("unitNumber")} aria-invalid={!!errors.unitNumber} />
                <FieldError errors={errors.unitNumber ? [errors.unitNumber] : undefined} />
              </Field>

              {structures.length > 0 && (
                <Field>
                  <FieldLabel htmlFor="structureId">Structural Segment (optional)</FieldLabel>
                  <Controller
                    control={control}
                    name="structureId"
                    render={({ field }) => (
                      <Select value={field.value ?? ""} onValueChange={field.onChange}>
                        <SelectTrigger id="structureId">
                          <SelectValue placeholder="Attach directly to property" />
                        </SelectTrigger>
                        <SelectContent>
                          {structures.map((s) => (
                            <SelectItem key={s.id} value={s.id!}>
                              {s.nodeType}: {s.name}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    )}
                  />
                </Field>
              )}

              <Field data-invalid={!!errors.baselinePrice}>
                <FieldLabel htmlFor="baselinePrice">Baseline Rent (ETB)</FieldLabel>
                <Input
                  id="baselinePrice"
                  type="number"
                  step="0.01"
                  placeholder="45000.00"
                  {...register("baselinePrice")}
                  aria-invalid={!!errors.baselinePrice}
                />
                <FieldError errors={errors.baselinePrice ? [errors.baselinePrice] : undefined} />
              </Field>

              <Field>
                <FieldLabel htmlFor="specification">Specification</FieldLabel>
                <Textarea id="specification" rows={2} placeholder="85 sqm retail front" {...register("specification")} />
              </Field>

              <Button type="submit" className="w-full" disabled={isSubmitting}>
                {isSubmitting ? "Creating..." : "Create Unit"}
              </Button>
            </FieldGroup>
          </form>
        </div>
      </SheetContent>
    </Sheet>
  );
}