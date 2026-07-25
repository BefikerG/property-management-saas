"use client";

import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from "recharts";
import { Skeleton } from "@/components/ui/skeleton";

interface Props {
  vacant: number;
  occupied: number;
  maintenance: number;
  isLoading?: boolean;
}

export function UnitStatusDonut({ vacant, occupied, maintenance, isLoading }: Props) {
  const data = [
    { name: "Occupied", value: occupied, color: "var(--color-info)" },
    { name: "Vacant", value: vacant, color: "var(--color-success)" },
    { name: "Maintenance", value: maintenance, color: "var(--color-warning)" },
  ];

  const total = vacant + occupied + maintenance;

  if (isLoading) {
    return <Skeleton className="h-[220px] w-full" />;
  }

  if (total === 0) {
    return (
      <div className="h-[220px] flex items-center justify-center text-sm text-text-muted">
        No units registered yet.
      </div>
    );
  }

  return (
    <div className="h-[220px] flex items-center">
      <ResponsiveContainer width="60%" height="100%">
        <PieChart>
          <Pie
            data={data}
            dataKey="value"
            nameKey="name"
            innerRadius={55}
            outerRadius={80}
            paddingAngle={2}
          >
            {data.map((entry) => (
              <Cell key={entry.name} fill={entry.color} stroke="none" />
            ))}
          </Pie>
          <Tooltip
            contentStyle={{
              backgroundColor: "var(--color-surface)",
              border: "1px solid var(--color-border)",
              borderRadius: "8px",
              fontSize: "13px",
            }}
          />
        </PieChart>
      </ResponsiveContainer>
      <div className="flex flex-col gap-2">
        {data.map((entry) => (
          <div key={entry.name} className="flex items-center gap-2 text-sm">
            <span
              className="h-2.5 w-2.5 rounded-full shrink-0"
              style={{ backgroundColor: entry.color }}
            />
            <span className="text-text-secondary">{entry.name}</span>
            <span className="font-medium text-text-primary ml-auto">{entry.value}</span>
          </div>
        ))}
      </div>
    </div>
  );
}