import {
  LayoutDashboard,
  Building2,
  Users,
  FileText,
  Receipt,
  UserCog,
  ScrollText,
  type LucideIcon,
} from "lucide-react";
import type { StaffRole } from "@/stores/auth-store";

export interface NavItem {
  label: string;
  href: string;
  icon: LucideIcon;
  roles: StaffRole[]; // which roles can see this item
}

export const NAV_ITEMS: NavItem[] = [
  {
    label: "Dashboard",
    href: "/",
    icon: LayoutDashboard,
    roles: ["ADMINISTRATOR", "PROPERTY_MANAGER", "VIEWER"],
  },
  {
    label: "Properties",
    href: "/properties",
    icon: Building2,
    roles: ["ADMINISTRATOR", "PROPERTY_MANAGER", "VIEWER"],
  },
  {
    label: "Tenants",
    href: "/tenants",
    icon: Users,
    roles: ["ADMINISTRATOR", "PROPERTY_MANAGER", "VIEWER"],
  },
  {
    label: "Leases",
    href: "/leases",
    icon: FileText,
    roles: ["ADMINISTRATOR", "PROPERTY_MANAGER", "VIEWER"],
  },
  {
    label: "Invoices",
    href: "/invoices",
    icon: Receipt,
    roles: ["ADMINISTRATOR", "PROPERTY_MANAGER", "VIEWER"],
  },
  {
    label: "Staff",
    href: "/staff",
    icon: UserCog,
    roles: ["ADMINISTRATOR"],
  },
  {
    label: "Audit Log",
    href: "/audit-log",
    icon: ScrollText,
    roles: ["ADMINISTRATOR"],
  },
];