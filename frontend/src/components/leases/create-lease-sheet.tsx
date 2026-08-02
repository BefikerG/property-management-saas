"use client";

import { useState } from "react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import type { Resolver } from "react-hook-form";
import { Sheet, SheetContent, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Field, FieldLabel, FieldError, FieldGroup, FieldDescription } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import { cn } from "@/lib/utils";
import { useCreateLease } from "@/hooks/use-leases";
import { useLeaseFormOptions } from "@/hooks/use-lease-form-options";
import { toast } from "sonner";

const leaseSchema = z.object({
  unitId: z.string().min(1, "Select a unit"),
  tenantProfileId: z.string().min(1, "Select a tenant"),
  startDate: z.string().min(1, "Start date is required"),
  endDate: z.string().min(1, "End date is required"),
  monthlyRent: z.coerce.number().min(0.01, "Monthly rent must be greater than zero"),
  billingDay: z.coerce.number().int().min(1).max(28),
  securityDeposit: z.coerce.number().min(0).optional(),
  escalationTerms: z.string().max(2000).optional(),
}).refine((data) => data.endDate > data.startDate, {
  message: "End date must be after start date",
  path: ["endDate"],
});

type LeaseFormValues = z.infer<typeof leaseSchema>;

const STEPS = ["Unit", "Tenant", "Terms"] as const;

export function CreateLeaseSheet() {
  const [open, setOpen] = useState(false);
  const [step, setStep] = useState(0);
  const createLease = useCreateLease();
  const { vacantUnits, tenants, isLoading } = useLeaseFormOptions();

  const {
    register,
    handleSubmit,
    control,
    watch,
    trigger,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<LeaseFormValues>({
    resolver: zodResolver(leaseSchema) as Resolver<LeaseFormValues>,
    defaultValues: { billingDay: 1 },
  });

  const selectedUnitId = watch("unitId");
  const selectedTenantId = watch("tenantProfileId");
  const selectedUnit = vacantUnits.find((u) => u.id === selectedUnitId);
  const selectedTenant = tenants.find((t) => t.id === selectedTenantId);

  async function goNext() {
    const fieldsPerStep: (keyof LeaseFormValues)[][] = [
      ["unitId"],
      ["tenantProfileId"],
      ["startDate", "endDate", "monthlyRent", "billingDay"],
    ];
    const valid = await trigger(fieldsPerStep[step]);
    if (valid) setStep((s) => Math.min(s + 1, STEPS.length - 1));
  }

  function goBack() {
    setStep((s) => Math.max(s - 1, 0));
  }

  async function onSubmit(values: LeaseFormValues) {
    try {
      await createLease.mutateAsync({
        unitId: values.unitId,
        tenantProfileId: values.tenantProfileId,
        startDate: values.startDate,
        endDate: values.endDate,
        monthlyRent: values.monthlyRent,
        billingDay: values.billingDay,
        securityDeposit: values.securityDeposit,
        escalationTerms: values.escalationTerms,
      });
      toast.success("Lease created in DRAFT status.");
      reset();
      setStep(0);
      setOpen(false);
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? "Failed to create lease.");
    }
  }

  function handleOpenChange(next: boolean) {
    if (!next) {
      reset();
      setStep(0);
    }
    setOpen(next);
  }

  return (
    <Sheet open={open} onOpenChange={handleOpenChange}>
      <Button onClick={() => setOpen(true)}>Create Lease</Button>
      <SheetContent side="right" className="w-full sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>Create Lease</SheetTitle>
        </SheetHeader>

        {/* ── Step indicator ─────────────────────────────────── */}
        <div className="flex items-center gap-2 px-4 mb-4">
          {STEPS.map((label, i) => (
            <div key={label} className="flex items-center gap-2 flex-1">
              <div
                className={cn(
                  "h-6 w-6 rounded-full flex items-center justify-center text-xs font-medium shrink-0",
                  i === step
                    ? "bg-primary text-primary-foreground"
                    : i < step
                    ? "bg-success text-white"
                    : "bg-surface-sunken text-text-muted"
                )}
              >
                {i + 1}
              </div>
              <span className={cn("text-xs", i === step ? "text-text-primary font-medium" : "text-text-muted")}>
                {label}
              </span>
              {i < STEPS.length - 1 && <div className="flex-1 h-px bg-border" />}
            </div>
          ))}
        </div>

        <form onSubmit={handleSubmit(onSubmit)} noValidate className="px-4 space-y-4">
          {/* ── Step 1: Unit ───────────────────────────────────── */}
          {step === 0 && (
            <FieldGroup>
              <Field data-invalid={!!errors.unitId}>
                <FieldLabel htmlFor="unitId">Vacant Unit</FieldLabel>
                <Controller
                  control={control}
                  name="unitId"
                  render={({ field }) => (
                    <Select value={field.value ?? ""} onValueChange={field.onChange}>
                      <SelectTrigger id="unitId" aria-invalid={!!errors.unitId}>
                        <SelectValue placeholder={isLoading ? "Loading units..." : "Select a vacant unit"} />
                      </SelectTrigger>
                      <SelectContent>
                        {vacantUnits.length === 0 && (
                          <div className="px-3 py-2 text-sm text-text-muted">
                            No vacant units available.
                          </div>
                        )}
                        {vacantUnits.map((unit) => (
                          <SelectItem key={unit.id} value={unit.id!}>
                            {unit.propertyName} — {unit.unitNumber}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                <FieldError errors={errors.unitId ? [errors.unitId] : undefined} />
                <FieldDescription>
                  Only units currently marked VACANT are shown. Units under MAINTENANCE
                  or already OCCUPIED cannot receive a new lease.
                </FieldDescription>
              </Field>
            </FieldGroup>
          )}

          {/* ── Step 2: Tenant ─────────────────────────────────── */}
          {step === 1 && (
            <FieldGroup>
              <Field data-invalid={!!errors.tenantProfileId}>
                <FieldLabel htmlFor="tenantProfileId">Tenant</FieldLabel>
                <Controller
                  control={control}
                  name="tenantProfileId"
                  render={({ field }) => (
                    <Select value={field.value ?? ""} onValueChange={field.onChange}>
                      <SelectTrigger id="tenantProfileId" aria-invalid={!!errors.tenantProfileId}>
                        <SelectValue placeholder={isLoading ? "Loading tenants..." : "Select a tenant"} />
                      </SelectTrigger>
                      <SelectContent>
                        {tenants.length === 0 && (
                          <div className="px-3 py-2 text-sm text-text-muted">
                            No tenants registered yet.
                          </div>
                        )}
                        {tenants.map((tenant) => (
                          <SelectItem key={tenant.id} value={tenant.id!}>
                            {tenant.fullName} ({tenant.email})
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                <FieldError errors={errors.tenantProfileId ? [errors.tenantProfileId] : undefined} />
              </Field>
            </FieldGroup>
          )}

          {/* ── Step 3: Terms ──────────────────────────────────── */}
          {step === 2 && (
            <FieldGroup>
              <div className="grid grid-cols-2 gap-3">
                <Field data-invalid={!!errors.startDate}>
                  <FieldLabel htmlFor="startDate">Start Date</FieldLabel>
                  <Input id="startDate" type="date" {...register("startDate")} aria-invalid={!!errors.startDate} />
                  <FieldError errors={errors.startDate ? [errors.startDate] : undefined} />
                </Field>
                <Field data-invalid={!!errors.endDate}>
                  <FieldLabel htmlFor="endDate">End Date</FieldLabel>
                  <Input id="endDate" type="date" {...register("endDate")} aria-invalid={!!errors.endDate} />
                  <FieldError errors={errors.endDate ? [errors.endDate] : undefined} />
                </Field>
              </div>

              <Field data-invalid={!!errors.monthlyRent}>
                <FieldLabel htmlFor="monthlyRent">Monthly Rent (ETB)</FieldLabel>
                <Input
                  id="monthlyRent"
                  type="number"
                  step="0.01"
                  placeholder={selectedUnit?.baselinePrice?.toString() ?? "45000.00"}
                  {...register("monthlyRent")}
                  aria-invalid={!!errors.monthlyRent}
                />
                <FieldError errors={errors.monthlyRent ? [errors.monthlyRent] : undefined} />
              </Field>

              <div className="grid grid-cols-2 gap-3">
                <Field data-invalid={!!errors.billingDay}>
                  <FieldLabel htmlFor="billingDay">Billing Day</FieldLabel>
                  <Input id="billingDay" type="number" min={1} max={28} {...register("billingDay")} aria-invalid={!!errors.billingDay} />
                  <FieldError errors={errors.billingDay ? [errors.billingDay] : undefined} />
                </Field>
                <Field>
                  <FieldLabel htmlFor="securityDeposit">Security Deposit (ETB)</FieldLabel>
                  <Input id="securityDeposit" type="number" step="0.01" placeholder="0.00" {...register("securityDeposit")} />
                </Field>
              </div>

              <Field>
                <FieldLabel htmlFor="escalationTerms">Escalation Terms (optional)</FieldLabel>
                <Textarea id="escalationTerms" rows={2} placeholder="e.g. 5% annual increase" {...register("escalationTerms")} />
              </Field>

              {/* ── Review summary ─────────────────────────────── */}
              <div className="bg-surface-sunken rounded-md p-3 text-sm space-y-1">
                <div className="flex justify-between">
                  <span className="text-text-secondary">Unit</span>
                  <span className="text-text-primary font-medium">
                    {selectedUnit?.propertyName} — {selectedUnit?.unitNumber}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-text-secondary">Tenant</span>
                  <span className="text-text-primary font-medium">{selectedTenant?.fullName}</span>
                </div>
              </div>
            </FieldGroup>
          )}

          {/* ── Navigation ─────────────────────────────────────── */}
          <div className="flex gap-2 pt-2">
            {step > 0 && (
              <Button type="button" variant="outline" className="flex-1" onClick={goBack}>
                Back
              </Button>
            )}
            {step < STEPS.length - 1 ? (
              <Button type="button" className="flex-1" onClick={goNext}>
                Next
              </Button>
            ) : (
              <Button type="submit" className="flex-1" disabled={isSubmitting}>
                {isSubmitting ? "Creating..." : "Create Lease"}
              </Button>
            )}
          </div>
        </form>
      </SheetContent>
    </Sheet>
  );
}