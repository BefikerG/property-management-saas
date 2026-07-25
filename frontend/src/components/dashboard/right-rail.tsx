"use client";

import Link from "next/link";
import { format, parseISO } from "date-fns";
import { Skeleton } from "@/components/ui/skeleton";
import type { LeaseResponseDto, InvoiceResponseDto, AuditLogResponseDto } from "@/lib/api/generated";

function Panel({ title, action, children }: { title: string; action?: React.ReactNode; children: React.ReactNode }) {
  return (
    <div className="bg-surface border border-border rounded-md p-4 shadow-xs">
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-semibold text-text-primary">{title}</h3>
        {action}
      </div>
      {children}
    </div>
  );
}

export function UpcomingRenewalsPanel({
  leases,
  isLoading,
}: {
  leases: LeaseResponseDto[];
  isLoading?: boolean;
}) {
  return (
    <Panel title="Upcoming Lease Renewals">
      {isLoading ? (
        <div className="space-y-2">
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
        </div>
      ) : leases.length === 0 ? (
        <p className="text-sm text-text-muted py-4 text-center">
          No leases expiring in the next 30 days.
        </p>
      ) : (
        <ul className="space-y-2">
          {leases.slice(0, 5).map((lease) => (
            <li key={lease.id} className="flex items-center justify-between text-sm">
              <span className="text-text-secondary truncate">Lease #{lease.id?.slice(0, 8)}</span>
              <span className="text-warning font-medium shrink-0 ml-2">
                {lease.endDate && format(parseISO(lease.endDate), "MMM d")}
              </span>
            </li>
          ))}
        </ul>
      )}
    </Panel>
  );
}

export function RecentActivityPanel({
  logs,
  isLoading,
}: {
  logs: AuditLogResponseDto[];
  isLoading?: boolean;
}) {
  return (
    <Panel
      title="Recent Activity"
      action={
        <Link href="/audit-log" className="text-xs text-text-link hover:underline">
          View all
        </Link>
      }
    >
      {isLoading ? (
        <div className="space-y-2">
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
        </div>
      ) : logs.length === 0 ? (
        <p className="text-sm text-text-muted py-4 text-center">No recent activity.</p>
      ) : (
        <ul className="space-y-3">
          {logs.map((log) => (
            <li key={log.id} className="text-sm">
              <span className="text-text-primary font-medium">{log.actionType}</span>{" "}
              <span className="text-text-secondary">{log.entityType?.toLowerCase()}</span>
              <div className="text-xs text-text-muted mt-0.5">
                {log.occurredAt && format(parseISO(log.occurredAt), "MMM d, h:mm a")}
              </div>
            </li>
          ))}
        </ul>
      )}
    </Panel>
  );
}

export function TopOutstandingPanel({
  invoices,
  isLoading,
}: {
  invoices: InvoiceResponseDto[];
  isLoading?: boolean;
}) {
  return (
    <Panel
      title="Largest Outstanding Balances"
      action={
        <Link href="/invoices?status=UNPAID" className="text-xs text-text-link hover:underline">
          View all
        </Link>
      }
    >
      {isLoading ? (
        <div className="space-y-2">
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
        </div>
      ) : invoices.length === 0 ? (
        <p className="text-sm text-text-muted py-4 text-center">No outstanding balances.</p>
      ) : (
        <ul className="space-y-2">
          {invoices.map((invoice) => (
            <li key={invoice.id} className="flex items-center justify-between text-sm">
              <span className="text-text-secondary">{invoice.billingPeriod}</span>
              <span className="text-danger font-medium">
                ETB {invoice.balance?.toLocaleString()}
              </span>
            </li>
          ))}
        </ul>
      )}
    </Panel>
  );
}