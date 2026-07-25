"use client";

import { Building2, DoorOpen, DoorClosed, FileText, Wallet, TrendingUp } from "lucide-react";
import { StatCard } from "@/components/dashboard/stat-card";
import { UnitStatusDonut } from "@/components/dashboard/unit-status-donut";
import { RevenueTrendChart } from "@/components/dashboard/revenue-trend-chart";
import { UnitMatrix } from "@/components/dashboard/unit-matrix";
import {
  UpcomingRenewalsPanel,
  RecentActivityPanel,
  TopOutstandingPanel,
} from "@/components/dashboard/right-rail";
import { usePortfolioUnits } from "@/hooks/use-portfolio-units";
import {
  useAllActiveLeases,
  useAllInvoices,
  useRecentAuditLog,
} from "@/hooks/use-dashboard-data";
import {
  computeOccupancyStats,
  computeFinancialStats,
  computeRevenueTrend,
  computeExpiringLeases,
  computeTopOutstandingBalances,
} from "@/lib/dashboard-metrics";

export default function DashboardPage() {
  const { units, isLoading: unitsLoading } = usePortfolioUnits();
  const { data: activeLeases = [], isLoading: leasesLoading } = useAllActiveLeases();
  const { data: invoices = [], isLoading: invoicesLoading } = useAllInvoices();
  const { data: recentActivity = [], isLoading: activityLoading } = useRecentAuditLog();

  const occupancy = computeOccupancyStats(units);
  const financials = computeFinancialStats(invoices);
  const revenueTrend = computeRevenueTrend(invoices);
  const expiringLeases = computeExpiringLeases(activeLeases);
  const topOutstanding = computeTopOutstandingBalances(invoices);

  const isLoading = unitsLoading || leasesLoading || invoicesLoading;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-text-primary">Dashboard</h1>
        <p className="text-sm text-text-secondary mt-1">
          Your portfolio at a glance.
        </p>
      </div>

      {/* ── Stat Row ─────────────────────────────────────────── */}
      <div className="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <StatCard
          label="Occupancy Rate"
          value={`${occupancy.occupancyRate}%`}
          icon={TrendingUp}
          isLoading={isLoading}
        />
        <StatCard
          label="Total Units"
          value={String(occupancy.total)}
          icon={Building2}
          isLoading={isLoading}
        />
        <StatCard
          label="Vacant Units"
          value={String(occupancy.vacant)}
          icon={DoorOpen}
          isLoading={isLoading}
        />
        <StatCard
          label="Active Leases"
          value={String(activeLeases.length)}
          icon={DoorClosed}
          isLoading={isLoading}
        />
        <StatCard
          label="Outstanding Balance"
          value={`ETB ${financials.outstandingBalance.toLocaleString()}`}
          icon={FileText}
          tone={financials.outstandingBalance > 0 ? "danger" : "default"}
          isLoading={isLoading}
        />
        <StatCard
          label="Revenue This Month"
          value={`ETB ${financials.revenueThisMonth.toLocaleString()}`}
          icon={Wallet}
          tone="success"
          isLoading={isLoading}
        />
      </div>

      {/* ── Trend Charts ─────────────────────────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="bg-surface border border-border rounded-md p-4 shadow-xs">
          <h3 className="text-sm font-semibold text-text-primary mb-3">Revenue Trend</h3>
          <RevenueTrendChart data={revenueTrend} isLoading={invoicesLoading} />
        </div>
        <div className="bg-surface border border-border rounded-md p-4 shadow-xs">
          <h3 className="text-sm font-semibold text-text-primary mb-3">Unit Status Distribution</h3>
          <UnitStatusDonut
            vacant={occupancy.vacant}
            occupied={occupancy.occupied}
            maintenance={occupancy.maintenance}
            isLoading={unitsLoading}
          />
        </div>
      </div>

      {/* ── Matrix + Right Rail ──────────────────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div className="lg:col-span-2 bg-surface border border-border rounded-md p-4 shadow-xs">
          <h3 className="text-sm font-semibold text-text-primary mb-3">Unit Matrix</h3>
          <UnitMatrix units={units} isLoading={unitsLoading} />
        </div>
        <div className="space-y-4">
          <UpcomingRenewalsPanel leases={expiringLeases} isLoading={leasesLoading} />
          <RecentActivityPanel logs={recentActivity} isLoading={activityLoading} />
          <TopOutstandingPanel invoices={topOutstanding} isLoading={invoicesLoading} />
        </div>
      </div>
    </div>
  );
}