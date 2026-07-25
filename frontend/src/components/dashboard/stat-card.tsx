import type { LucideIcon } from "lucide-react";
import { cn } from "@/lib/utils";
import { Skeleton } from "@/components/ui/skeleton";

interface StatCardProps {
  label: string;
  value: string;
  icon: LucideIcon;
  isLoading?: boolean;
  tone?: "default" | "danger" | "success";
}

export function StatCard({ label, value, icon: Icon, isLoading, tone = "default" }: StatCardProps) {
  return (
    <div className="bg-surface border border-border rounded-md p-4 shadow-xs">
      <div className="flex items-center justify-between mb-2">
        <span className="text-sm text-text-secondary">{label}</span>
        <Icon className="h-icon-sm w-icon-sm text-text-muted" />
      </div>
      {isLoading ? (
        <Skeleton className="h-7 w-20" />
      ) : (
        <div
          className={cn(
            "text-2xl font-bold",
            tone === "danger" && "text-danger",
            tone === "success" && "text-success",
            tone === "default" && "text-text-primary"
          )}
        >
          {value}
        </div>
      )}
    </div>
  );
}