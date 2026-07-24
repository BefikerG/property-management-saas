import { create } from "zustand";
import type { LucideIcon } from "lucide-react";

export interface Command {
  id: string;
  label: string;
  group: "Navigation" | "Actions" | "Search";
  icon?: LucideIcon;
  shortcut?: string;
  action: () => void;
}

interface CommandState {
  commands: Record<string, Command>;
  registerCommands: (commands: Command[]) => void;
  unregisterCommands: (ids: string[]) => void;
}

/**
 * Global command registry. Screens register their contextual actions
 * on mount and unregister on unmount via the useRegisterCommands hook.
 * The command palette itself just renders whatever is currently registered.
 */
export const useCommandStore = create<CommandState>((set) => ({
  commands: {},
  registerCommands: (commands) =>
    set((state) => {
      const next = { ...state.commands };
      commands.forEach((cmd) => (next[cmd.id] = cmd));
      return { commands: next };
    }),
  unregisterCommands: (ids) =>
    set((state) => {
      const next = { ...state.commands };
      ids.forEach((id) => delete next[id]);
      return { commands: next };
    }),
}));