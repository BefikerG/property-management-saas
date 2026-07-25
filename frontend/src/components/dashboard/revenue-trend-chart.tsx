"use client";

import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import { Skeleton } from "@/components/ui/skeleton";

interface Props {
  data: { label: string; revenue: number }[];
  isLoading?: boolean;
}

export function RevenueTrendChart({ data, isLoading }: Props) {
  if (isLoading) {
    return <Skeleton className="h-[220px] w-full" />;
  }

  return (
    <ResponsiveContainer width="100%" height={220}>
      <LineChart data={data} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" vertical={false} />
        <XAxis
          dataKey="label"
          tick={{ fontSize: 12, fill: "var(--color-text-muted)" }}
          axisLine={{ stroke: "var(--color-border)" }}
          tickLine={false}
        />
        <YAxis
          tick={{ fontSize: 12, fill: "var(--color-text-muted)" }}
          axisLine={false}
          tickLine={false}
          tickFormatter={(v) => `${(v / 1000).toFixed(0)}k`}
        />
        <Tooltip
          contentStyle={{
            backgroundColor: "var(--color-surface)",
            border: "1px solid var(--color-border)",
            borderRadius: "8px",
            fontSize: "13px",
          }}
          formatter={(value: any) => [`ETB ${Number(value).toLocaleString()}`, "Revenue"]}
        />
        <Line
          type="monotone"
          dataKey="revenue"
          stroke="var(--color-primary)"
          strokeWidth={2}
          dot={{ r: 3, fill: "var(--color-primary)" }}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}