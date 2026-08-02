"use client";

import { useState } from "react";
import Link from "next/link";
import { FileText, X } from "lucide-react";
import { useLeases, type LeaseStatusFilter } from "@/hooks/use-leases";
import { CreateLeaseSheet } from "@/components/leases/create-lease-sheet";
import { LeaseStatusBadge } from "@/components/leases/lease-status-badge";
import { LeaseActionsMenu } from "@/components/leases/lease-actions-menu";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import {
  Pagination, PaginationContent, PaginationItem, PaginationNext, PaginationPrevious,
} from "@/components/ui/pagination";

const STATUS_FILTERS: LeaseStatusFilter[] = ["DRAFT", "PENDING", "ACTIVE", "TERMINATED", "EXPIRED"];

export default function LeasesPage() {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<LeaseStatusFilter | undefined>(undefined);

  const { data, isLoading } = useLeases(page, 20, statusFilter);
  const leases = data?.content ?? [];

  function setFilter(status: LeaseStatusFilter | undefined) {
    setStatusFilter(status);
    setPage(0);
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-text-primary">Leases</h1>
          <p className="text-sm text-text-secondary mt-1">
            Manage lease agreements and their lifecycle.
          </p>
        </div>
        <CreateLeaseSheet />
      </div>

      {/* ── Filter chips ───────────────────────────────────────── */}
      <div className="flex flex-wrap gap-2">
        <button
          onClick={() => setFilter(undefined)}
          className={cn(
            "h-control-sm px-3 rounded-full text-sm font-medium border transition-colors duration-fast",
            !statusFilter
              ? "bg-primary text-primary-foreground border-primary"
              : "bg-surface text-text-secondary border-border hover:bg-surface-hover"
          )}
        >
          All
        </button>
        {STATUS_FILTERS.map((status) => (
          <button
            key={status}
            onClick={() => setFilter(status)}
            className={cn(
              "h-control-sm px-3 rounded-full text-sm font-medium border transition-colors duration-fast inline-flex items-center gap-1",
              statusFilter === status
                ? "bg-primary text-primary-foreground border-primary"
                : "bg-surface text-text-secondary border-border hover:bg-surface-hover"
            )}
          >
            {status}
            {statusFilter === status && (
              <X
                className="h-3 w-3"
                onClick={(e) => {
                  e.stopPropagation();
                  setFilter(undefined);
                }}
              />
            )}
          </button>
        ))}
      </div>

      <div className="bg-surface border border-border rounded-md overflow-hidden">
        {isLoading ? (
          <div className="p-4 space-y-2">
            {Array.from({ length: 5 }).map((_, i) => <Skeleton key={i} className="h-row w-full" />)}
          </div>
        ) : leases.length === 0 ? (
          <div className="py-16 flex flex-col items-center gap-3">
            <FileText className="h-icon-xl w-icon-xl text-text-muted" />
            <p className="text-sm text-text-secondary">
              {statusFilter ? `No ${statusFilter.toLowerCase()} leases.` : "No leases created yet."}
            </p>
          </div>
        ) : (
          <Table>
            <TableHeader className="sticky top-0 bg-surface-sunken">
              <TableRow>
                <TableHead>Status</TableHead>
                <TableHead>Monthly Rent (ETB)</TableHead>
                <TableHead>Start Date</TableHead>
                <TableHead>End Date</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {leases.map((lease) => (
                <TableRow key={lease.id} className="hover:bg-surface-hover">
                  <TableCell>
                    <Link href={`/leases/${lease.id}`}>
                      <LeaseStatusBadge status={lease.status} />
                    </Link>
                  </TableCell>
                  <TableCell className="font-mono text-sm">
                    {lease.monthlyRent?.toLocaleString()}
                  </TableCell>
                  <TableCell className="text-text-secondary">{lease.startDate}</TableCell>
                  <TableCell className="text-text-secondary">{lease.endDate}</TableCell>
                  <TableCell className="text-right">
                    <LeaseActionsMenu lease={lease} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </div>

      {data && data.totalPages! > 1 && (
        <Pagination>
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious onClick={() => setPage((p) => Math.max(0, p - 1))} aria-disabled={page === 0} />
            </PaginationItem>
            <PaginationItem>
              <span className="text-sm text-text-secondary px-2">Page {page + 1} of {data.totalPages}</span>
            </PaginationItem>
            <PaginationItem>
              <PaginationNext
                onClick={() => setPage((p) => Math.min((data.totalPages ?? 1) - 1, p + 1))}
                aria-disabled={page + 1 >= (data.totalPages ?? 1)}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      )}
    </div>
  );
}