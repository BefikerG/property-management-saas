"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { Home, Building2, Users, FileText, Receipt, UserCog, ScrollText } from "lucide-react";
import { useUIStore } from "@/stores/ui-store";
import { useCommandStore } from "@/stores/command-store";
import {
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command";

const GLOBAL_NAV_COMMANDS = [
  { id: "nav-dashboard", label: "Go to Dashboard", href: "/", icon: Home },
  { id: "nav-properties", label: "Go to Properties", href: "/properties", icon: Building2 },
  { id: "nav-tenants", label: "Go to Tenants", href: "/tenants", icon: Users },
  { id: "nav-leases", label: "Go to Leases", href: "/leases", icon: FileText },
  { id: "nav-invoices", label: "Go to Invoices", href: "/invoices", icon: Receipt },
  { id: "nav-staff", label: "Go to Staff", href: "/staff", icon: UserCog },
  { id: "nav-audit", label: "Go to Audit Log", href: "/audit-log", icon: ScrollText },
];

export function CommandPalette() {
  const { commandPaletteOpen, setCommandPaletteOpen } = useUIStore();
  const registeredCommands = useCommandStore((s) => s.commands);
  const router = useRouter();

  // Global ⌘K / Ctrl+K keyboard shortcut
  useEffect(() => {
    function handler(e: KeyboardEvent) {
      if (e.key === "k" && (e.metaKey || e.ctrlKey)) {
        e.preventDefault();
        setCommandPaletteOpen(!commandPaletteOpen);
      }
    }
    document.addEventListener("keydown", handler);
    return () => document.removeEventListener("keydown", handler);
  }, [commandPaletteOpen, setCommandPaletteOpen]);

  function runAndClose(action: () => void) {
    setCommandPaletteOpen(false);
    action();
  }

  const contextualCommands = Object.values(registeredCommands);

  return (
    <CommandDialog open={commandPaletteOpen} onOpenChange={setCommandPaletteOpen}>
      <CommandInput placeholder="Search or run a command..." />
      <CommandList>
        <CommandEmpty>No results found.</CommandEmpty>

        {contextualCommands.length > 0 && (
          <CommandGroup heading="Actions">
            {contextualCommands.map((cmd) => (
              <CommandItem
                key={cmd.id}
                onSelect={() => runAndClose(cmd.action)}
              >
                {cmd.icon && <cmd.icon className="h-icon-sm w-icon-sm mr-2" />}
                {cmd.label}
                {cmd.shortcut && (
                  <span className="ml-auto text-xs text-text-muted">{cmd.shortcut}</span>
                )}
              </CommandItem>
            ))}
          </CommandGroup>
        )}

        <CommandGroup heading="Navigation">
          {GLOBAL_NAV_COMMANDS.map((cmd) => (
            <CommandItem
              key={cmd.id}
              onSelect={() => runAndClose(() => router.push(cmd.href))}
            >
              <cmd.icon className="h-icon-sm w-icon-sm mr-2" />
              {cmd.label}
            </CommandItem>
          ))}
        </CommandGroup>
      </CommandList>
    </CommandDialog>
  );
}