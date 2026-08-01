"use client";

import Link from "next/link";
import { useState } from "react";
import { Search, Users } from "lucide-react";
import { useTenants } from "@/hooks/use-tenants";
import { useDebouncedValue } from "@/hooks/use-debounced-value";
import { CreateTenantSheet } from "@/components/tenants/create-tenant-sheet";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import {
  Pagination, PaginationContent, PaginationItem, PaginationNext, PaginationPrevious,
} from "@/components/ui/pagination";

export default function TenantsPage() {
  const [page, setPage] = useState(0);
  const [searchInput, setSearchInput] = useState("");
  const debouncedQuery = useDebouncedValue(searchInput, 300);

  const { data, isLoading, isFetching } = useTenants(page, 20, debouncedQuery);
  const tenants = data?.content ?? [];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-text-primary">Tenants</h1>
          <p className="text-sm text-text-secondary mt-1">
            Your centralized renter directory.
          </p>
        </div>
        <CreateTenantSheet />
      </div>

      <div className="relative max-w-sm">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-icon-sm w-icon-sm text-text-muted" />
        <Input
          placeholder="Search by name or email..."
          className="pl-9"
          value={searchInput}
          onChange={(e) => {
            setSearchInput(e.target.value);
            setPage(0); // reset to first page on new search
          }}
        />
        {isFetching && !isLoading && (
          <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-text-muted">
            Searching...
          </span>
        )}
      </div>

      <div className="bg-surface border border-border rounded-md overflow-hidden">
        {isLoading ? (
          <div className="p-4 space-y-2">
            {Array.from({ length: 5 }).map((_, i) => (
              <Skeleton key={i} className="h-row w-full" />
            ))}
          </div>
        ) : tenants.length === 0 ? (
          <div className="py-16 flex flex-col items-center gap-3">
            <Users className="h-icon-xl w-icon-xl text-text-muted" />
            <p className="text-sm text-text-secondary">
              {debouncedQuery ? "No tenants match your search." : "No tenants registered yet."}
            </p>
          </div>
        ) : (
          <Table>
            <TableHeader className="sticky top-0 bg-surface-sunken">
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Phone</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {tenants.map((tenant) => (
                <TableRow key={tenant.id} className="hover:bg-surface-hover">
                  <TableCell className="font-medium">
                    <Link href={`/tenants/${tenant.id}`} className="hover:text-primary">
                      {tenant.fullName}
                    </Link>
                  </TableCell>
                  <TableCell className="text-text-secondary">{tenant.email}</TableCell>
                  <TableCell className="text-text-secondary">{tenant.phone || "—"}</TableCell>
                  <TableCell className="text-right">
                    <Link href={`/tenants/${tenant.id}`} className="text-sm text-text-link hover:underline">
                      View
                    </Link>
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
              <span className="text-sm text-text-secondary px-2">
                Page {page + 1} of {data.totalPages}
              </span>
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