"use client";

import { Bell, LogOut, User } from "lucide-react";
import { useAuthStore } from "@/stores/auth-store";
import { logout } from "@/lib/api/auth-actions";
import { useRouter } from "next/navigation";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";

const ROLE_LABELS: Record<string, string> = {
  ADMINISTRATOR: "Administrator",
  PROPERTY_MANAGER: "Property Manager",
  VIEWER: "Viewer",
};

export function Header() {
  const { email, role } = useAuthStore();
  const router = useRouter();

  async function handleLogout() {
    await logout();
    router.push("/login");
  }

  const initials = email?.slice(0, 2).toUpperCase() ?? "??";

  return (
    <header className="h-header flex items-center justify-end gap-3 px-6 border-b border-border bg-surface shrink-0">
      <button
        className="h-control-sm w-control-sm flex items-center justify-center rounded-md text-text-muted hover:bg-surface-hover hover:text-text-primary transition-colors duration-fast"
        aria-label="Notifications"
      >
        <Bell className="h-icon-sm w-icon-sm" />
      </button>

      <DropdownMenu>
        <DropdownMenuTrigger
          render={
            <button className="flex items-center gap-2 h-control-md rounded-md px-2 hover:bg-surface-hover transition-colors duration-fast">
              <Avatar className="h-6 w-6">
                <AvatarFallback className="text-xs bg-brand-100 text-brand-700">
                  {initials}
                </AvatarFallback>
              </Avatar>
              <div className="text-left hidden sm:block">
                <div className="text-sm font-medium text-text-primary leading-none">
                  {email}
                </div>
                <div className="text-xs text-text-muted mt-0.5">
                  {role ? ROLE_LABELS[role] : ""}
                </div>
              </div>
            </button>
          }
        />
        <DropdownMenuContent align="end" className="w-48">
          <DropdownMenuItem>
            <User className="h-icon-sm w-icon-sm mr-2" />
            Profile
          </DropdownMenuItem>
          <DropdownMenuSeparator />
          <DropdownMenuItem onClick={handleLogout} className="text-danger">
            <LogOut className="h-icon-sm w-icon-sm mr-2" />
            Log out
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </header>
  );
}