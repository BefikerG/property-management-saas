"use client";

import { useParams } from "next/navigation";
import { useLease } from "@/hooks/use-leases";
import { LeaseStatusBadge } from "@/components/leases/lease-status-badge";
import { LeaseActionsMenu } from "@/components/leases/lease-actions-menu";
import { Skeleton } from "@/components/ui/skeleton";

export default function LeaseDetailPage() {
  const params = useParams<{ id: string }>();
  const { data: lease, isLoading } = useLease(params.id);

  if (isLoading || !lease) {
    return <Skeleton className="h-64 w-full" />;
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <h1 className="text-xl font-semibold text-text-primary">Lease Detail</h1>
          <LeaseStatusBadge status={lease.status} />
        </div>
        <LeaseActionsMenu lease={lease} />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-surface border border-border rounded-md p-4 shadow-xs">
          <h3 className="text-sm font-semibold text-text-primary mb-3">Terms</h3>
          <dl className="space-y-2 text-sm">
            <div className="flex justify-between">
              <dt className="text-text-secondary">Start Date</dt>
              <dd className="text-text-primary font-medium">{lease.startDate}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-text-secondary">End Date</dt>
              <dd className="text-text-primary font-medium">{lease.endDate}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-text-secondary">Monthly Rent</dt>
              <dd className="text-text-primary font-medium font-mono">
                ETB {lease.monthlyRent?.toLocaleString()}
              </dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-text-secondary">Billing Day</dt>
              <dd className="text-text-primary font-medium">{lease.billingDay}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-text-secondary">Security Deposit</dt>
              <dd className="text-text-primary font-medium font-mono">
                ETB {lease.securityDeposit?.toLocaleString() ?? "0.00"}
              </dd>
            </div>
          </dl>
          {lease.escalationTerms && (
            <div className="mt-4 pt-3 border-t border-border">
              <dt className="text-text-secondary text-sm mb-1">Escalation Terms</dt>
              <dd className="text-text-primary text-sm">{lease.escalationTerms}</dd>
            </div>
          )}
        </div>

        <div className="bg-surface border border-border rounded-md p-4 shadow-xs">
          <h3 className="text-sm font-semibold text-text-primary mb-3">Record Info</h3>
          <dl className="space-y-2 text-sm">
            <div className="flex justify-between">
              <dt className="text-text-secondary">Lease ID</dt>
              <dd className="text-text-muted font-mono text-xs">{lease.id}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-text-secondary">Created</dt>
              <dd className="text-text-secondary">
                {lease.createdAt && new Date(lease.createdAt).toLocaleString()}
              </dd>
            </div>
          </dl>
        </div>
      </div>
    </div>
  );
}