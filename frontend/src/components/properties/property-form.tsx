"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Field, FieldLabel, FieldError, FieldGroup } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import type { PropertyResponseDto } from "@/lib/api/generated";

const propertySchema = z.object({
  name: z.string().min(1, "Property name is required").max(255),
  address: z.string().min(1, "Address is required"),
  locationCity: z.string().min(1, "City is required").max(100),
  description: z.string().max(2000).optional(),
});

export type PropertyFormValues = z.infer<typeof propertySchema>;

interface Props {
  defaultValues?: Partial<PropertyResponseDto>;
  onSubmit: (values: PropertyFormValues) => Promise<void>;
  submitLabel: string;
  isSubmitting?: boolean;
}

export function PropertyForm({ defaultValues, onSubmit, submitLabel, isSubmitting }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<PropertyFormValues>({
    resolver: zodResolver(propertySchema),
    defaultValues: {
      name: defaultValues?.name ?? "",
      address: defaultValues?.address ?? "",
      locationCity: defaultValues?.locationCity ?? "",
      description: defaultValues?.description ?? "",
    },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
      <FieldGroup>
        <Field data-invalid={!!errors.name}>
          <FieldLabel htmlFor="name">Property Name</FieldLabel>
          <Input id="name" placeholder="Bole Commercial Plaza" {...register("name")} aria-invalid={!!errors.name} />
          <FieldError errors={errors.name ? [errors.name] : undefined} />
        </Field>

        <Field data-invalid={!!errors.address}>
          <FieldLabel htmlFor="address">Address</FieldLabel>
          <Input id="address" placeholder="Bole Road, Kebele 03" {...register("address")} aria-invalid={!!errors.address} />
          <FieldError errors={errors.address ? [errors.address] : undefined} />
        </Field>

        <Field data-invalid={!!errors.locationCity}>
          <FieldLabel htmlFor="locationCity">City</FieldLabel>
          <Input id="locationCity" placeholder="Addis Ababa" {...register("locationCity")} aria-invalid={!!errors.locationCity} />
          <FieldError errors={errors.locationCity ? [errors.locationCity] : undefined} />
        </Field>

        <Field>
          <FieldLabel htmlFor="description">Description</FieldLabel>
          <Textarea id="description" rows={3} placeholder="7-storey mixed-use commercial complex" {...register("description")} />
        </Field>

        <Button type="submit" className="w-full" disabled={isSubmitting}>
          {isSubmitting ? "Saving..." : submitLabel}
        </Button>
      </FieldGroup>
    </form>
  );
}