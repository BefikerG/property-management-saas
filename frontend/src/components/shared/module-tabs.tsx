"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";

export interface ModuleTab {
  label: string;
  href: string;
}

/**
 * Horizontal tab bar rendered below a page header, for screens with
 * genuinely distinct sub-views (e.g. a Property's Overview/Units/Leases).
 * Not used on flat list screens like Staff or Audit Log.
 */
export function ModuleTabs({ tabs }: { tabs: ModuleTab[] }) {
  const pathname = usePathname();

  return (
    <div className="border-b border-border">
      <nav className="flex gap-1 -mb-px">
        {tabs.map((tab) => {
          const isActive = pathname === tab.href;
          return (
            <Link
              key={tab.href}
              href={tab.href}
              className={cn(
                "px-3 py-2 text-sm font-medium border-b-2 transition-colors duration-fast",
                isActive
                  ? "border-primary text-primary"
                  : "border-transparent text-text-secondary hover:text-text-primary hover:border-border-strong"
              )}
            >
              {tab.label}
            </Link>
          );
        })}
      </nav>
    </div>
  );
}