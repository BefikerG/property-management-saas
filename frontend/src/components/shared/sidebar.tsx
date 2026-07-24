"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { ChevronsLeft, ChevronsRight, ChevronDown, Search } from "lucide-react";
import { NAV_ITEMS } from "@/lib/nav-config";
import { useAuthStore } from "@/stores/auth-store";
import { useUIStore } from "@/stores/ui-store";
import { cn } from "@/lib/utils";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export function Sidebar() {
  const pathname = usePathname();
  const { role, email, organizationId } = useAuthStore();
  const { sidebarCollapsed, toggleSidebar, setCommandPaletteOpen } = useUIStore();

  const visibleItems = NAV_ITEMS.filter(
    (item) => role && item.roles.includes(role)
  );

  return (
    <aside
      className={cn(
        "flex flex-col h-screen bg-surface border-r border-border transition-all duration-base",
        sidebarCollapsed ? "w-sidebar-collapsed" : "w-sidebar"
      )}
    >
      {/* ── Workspace switcher ──────────────────────────────── */}
      <div className="h-header flex items-center px-3 border-b border-border">
        <DropdownMenu>
          <DropdownMenuTrigger
            render={
              <button
                className={cn(
                  "flex items-center gap-2 rounded-md px-2 h-control-sm w-full",
                  "hover:bg-surface-hover transition-colors duration-fast",
                  sidebarCollapsed && "justify-center px-0"
                )}
              >
                <div className="h-6 w-6 rounded-sm bg-primary flex items-center justify-center shrink-0">
                  <span className="text-xs font-bold text-primary-foreground">
                    {organizationId ? "P" : ""}
                  </span>
                </div>
                {!sidebarCollapsed && (
                  <>
                    <span className="text-sm font-medium text-text-primary truncate flex-1 text-left">
                      Addis Prime Properties
                    </span>
                    <ChevronDown className="h-icon-sm w-icon-sm text-text-muted shrink-0" />
                  </>
                )}
              </button>
            }
          />
          <DropdownMenuContent align="start" className="w-56">
            <DropdownMenuItem disabled>{email}</DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      {/* ── Search / command palette trigger ────────────────── */}
      <div className="p-2">
        <button
          onClick={() => setCommandPaletteOpen(true)}
          className={cn(
            "flex items-center gap-2 w-full h-control-md rounded-md border border-border",
            "px-3 text-sm text-text-muted hover:bg-surface-hover transition-colors duration-fast",
            sidebarCollapsed && "justify-center px-0"
          )}
        >
          <Search className="h-icon-sm w-icon-sm shrink-0" />
          {!sidebarCollapsed && (
            <>
              <span className="flex-1 text-left">Search...</span>
              <kbd className="text-xs bg-surface-sunken px-1.5 py-0.5 rounded-sm border border-border">
                ⌘K
              </kbd>
            </>
          )}
        </button>
      </div>

      {/* ── Nav items ────────────────────────────────────────── */}
      <nav className="flex-1 overflow-y-auto px-2 space-y-1">
        {visibleItems.map((item) => {
          const isActive =
            item.href === "/" ? pathname === "/" : pathname.startsWith(item.href);
          const Icon = item.icon;

          return (
            <Link
              key={item.href}
              href={item.href}
              title={sidebarCollapsed ? item.label : undefined}
              className={cn(
                "flex items-center gap-2 h-control-md rounded-md px-2 text-sm font-medium",
                "transition-colors duration-fast",
                isActive
                  ? "bg-primary-tint text-primary"
                  : "text-text-secondary hover:bg-surface-hover hover:text-text-primary",
                sidebarCollapsed && "justify-center"
              )}
            >
              <Icon className="h-icon-md w-icon-md shrink-0" />
              {!sidebarCollapsed && <span>{item.label}</span>}
            </Link>
          );
        })}
      </nav>

      {/* ── Collapse toggle ──────────────────────────────────── */}
      <div className="p-2 border-t border-border">
        <button
          onClick={toggleSidebar}
          className={cn(
            "flex items-center gap-2 w-full h-control-sm rounded-md px-2",
            "text-text-muted hover:bg-surface-hover hover:text-text-primary",
            "transition-colors duration-fast",
            sidebarCollapsed && "justify-center"
          )}
        >
          {sidebarCollapsed ? (
            <ChevronsRight className="h-icon-sm w-icon-sm" />
          ) : (
            <>
              <ChevronsLeft className="h-icon-sm w-icon-sm" />
              <span className="text-xs">Collapse</span>
            </>
          )}
        </button>
      </div>
    </aside>
  );
}