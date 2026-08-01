"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import type { Resolver } from "react-hook-form";
import { Field, FieldLabel, FieldError, FieldGroup } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import type { TenantProfileResponseDto } from "@/lib/api/generated";

const tenantSchema = z.object({
  fullName: z.string().min(1, "Full name is required").max(255),
  email: z.string().min(1, "Email is required").email("Enter a valid email address"),
  phone: z.string().max(30).optional(),
  identificationReference: z.string().max(255).optional(),
});

export type TenantFormValues = z.infer<typeof tenantSchema>;

interface Props {
  defaultValues?: Partial<TenantProfileResponseDto>;
  onSubmit: (values: TenantFormValues) => Promise<void>;
  submitLabel: string;
  isSubmitting?: boolean;
}

export function TenantForm({ defaultValues, onSubmit, submitLabel, isSubmitting }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<TenantFormValues>({
    resolver: zodResolver(tenantSchema) as Resolver<TenantFormValues>,
    defaultValues: {
      fullName: defaultValues?.fullName ?? "",
      email: defaultValues?.email ?? "",
      phone: defaultValues?.phone ?? "",
      identificationReference: defaultValues?.identificationReference ?? "",
    },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
      <FieldGroup>
        <Field data-invalid={!!errors.fullName}>
          <FieldLabel htmlFor="fullName">Full Name</FieldLabel>
          <Input id="fullName" placeholder="Almaz Tadesse" {...register("fullName")} aria-invalid={!!errors.fullName} />
          <FieldError errors={errors.fullName ? [errors.fullName] : undefined} />
        </Field>

        <Field data-invalid={!!errors.email}>
          <FieldLabel htmlFor="email">Email</FieldLabel>
          <Input id="email" type="email" placeholder="tenant@example.com" {...register("email")} aria-invalid={!!errors.email} />
          <FieldError errors={errors.email ? [errors.email] : undefined} />
        </Field>

        <Field>
          <FieldLabel htmlFor="phone">Phone</FieldLabel>
          <Input id="phone" placeholder="+251911000001" {...register("phone")} />
        </Field>

        <Field>
          <FieldLabel htmlFor="identificationReference">ID Reference</FieldLabel>
          <Input id="identificationReference" placeholder="ETH-ID-2024-001" {...register("identificationReference")} />
        </Field>

        <Button type="submit" className="w-full" disabled={isSubmitting}>
          {isSubmitting ? "Saving..." : submitLabel}
        </Button>
      </FieldGroup>
    </form>
  );
}