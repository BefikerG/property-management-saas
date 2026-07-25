import { differenceInDays, parseISO, format, subMonths, startOfMonth } from "date-fns";
import type { UnitResponseDto, LeaseResponseDto, InvoiceResponseDto } from "@/lib/api/generated";

export function computeOccupancyStats(units: UnitResponseDto[]) {
  const total = units.length;
  const occupied = units.filter((u) => u.status === "OCCUPIED").length;
  const vacant = units.filter((u) => u.status === "VACANT").length;
  const maintenance = units.filter((u) => u.status === "MAINTENANCE").length;
  const occupancyRate = total > 0 ? Math.round((occupied / total) * 100) : 0;

  return { total, occupied, vacant, maintenance, occupancyRate };
}

export function computeFinancialStats(invoices: InvoiceResponseDto[]) {
  const currentPeriod = format(new Date(), "yyyy-MM");

  const outstanding = invoices.filter((i) => i.status !== "PAID");
  const outstandingBalance = outstanding.reduce(
    (sum, i) => sum + (i.balance ?? 0),
    0
  );

  const revenueThisMonth = invoices
    .filter((i) => i.billingPeriod === currentPeriod)
    .reduce((sum, i) => sum + (i.amountPaid ?? 0), 0);

  const dueThisPeriod = invoices
    .filter((i) => i.billingPeriod === currentPeriod)
    .reduce((sum, i) => sum + (i.amountDue ?? 0), 0);

  const collectionRate =
    dueThisPeriod > 0 ? Math.round((revenueThisMonth / dueThisPeriod) * 100) : 0;

  return { outstandingBalance, revenueThisMonth, collectionRate, outstanding };
}

export function computeRevenueTrend(invoices: InvoiceResponseDto[], months = 6) {
  const buckets: { period: string; label: string; revenue: number }[] = [];

  for (let i = months - 1; i >= 0; i--) {
    const date = subMonths(startOfMonth(new Date()), i);
    const period = format(date, "yyyy-MM");
    const label = format(date, "MMM");

    const revenue = invoices
      .filter((inv) => inv.billingPeriod === period)
      .reduce((sum, inv) => sum + (inv.amountPaid ?? 0), 0);

    buckets.push({ period, label, revenue });
  }

  return buckets;
}

export function computeExpiringLeases(leases: LeaseResponseDto[], withinDays = 30) {
  const now = new Date();
  return leases
    .filter((lease) => {
      if (!lease.endDate) return false;
      const days = differenceInDays(parseISO(lease.endDate), now);
      return days >= 0 && days <= withinDays;
    })
    .sort((a, b) => (a.endDate! > b.endDate! ? 1 : -1));
}

export function computeTopOutstandingBalances(invoices: InvoiceResponseDto[], limit = 5) {
  return invoices
    .filter((i) => i.status !== "PAID")
    .sort((a, b) => (b.balance ?? 0) - (a.balance ?? 0))
    .slice(0, limit);
}