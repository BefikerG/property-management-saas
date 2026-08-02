import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

const STATUS_STYLES: Record<string, string> = {
  DRAFT: "bg-surface-sunken text-text-secondary",
  PENDING: "bg-warning-tint text-warning",
  ACTIVE: "bg-success-tint text-success",
  TERMINATED: "bg-danger-tint text-danger",
  EXPIRED: "bg-surface-sunken text-text-muted",
};

export function LeaseStatusBadge({ status }: { status?: string }) {
  return (
    <Badge className={cn(STATUS_STYLES[status ?? "DRAFT"])}>
      {status}
    </Badge>
  );
}