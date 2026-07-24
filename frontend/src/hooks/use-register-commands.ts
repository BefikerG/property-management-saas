"use client";

import { useEffect } from "react";
import { useCommandStore, type Command } from "@/stores/command-store";

/**
 * Registers a set of commands into the global command palette for as
 * long as the calling component is mounted. Cleans up automatically
 * on unmount — a screen leaving means its screen-specific actions
 * (e.g. "Create Lease" while on the Leases page) disappear from ⌘K.
 */
export function useRegisterCommands(commands: Command[]) {
  const registerCommands = useCommandStore((s) => s.registerCommands);
  const unregisterCommands = useCommandStore((s) => s.unregisterCommands);

  useEffect(() => {
    registerCommands(commands);
    return () => unregisterCommands(commands.map((c) => c.id));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(commands.map((c) => c.id))]);
}