import axios from "axios";
import {
  Configuration,
  AuditLogApi,
  AuthenticationApi,
  BillingEngineApi,
  InvoicesPaymentsApi,
  LeasesApi,
  OrganizationsApi,
  PropertiesApi,
  StaffMembersApi,
  TenantProfilesApi,
} from "./generated";
import { useAuthStore } from "@/stores/auth-store";
import { refreshAccessToken } from "./auth-actions";

export const axiosInstance = axios.create({
  baseURL: typeof window !== "undefined" ? window.location.origin : "",
  headers: { "Content-Type": "application/json" },
});

// ── Request interceptor: attach the current access token ──────────
axiosInstance.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ── Response interceptor: transparent 401 → refresh → retry ───────
let isRefreshing = false;
let pendingRequests: Array<() => void> = [];

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (isRefreshing) {
        // Another request already triggered a refresh — queue this one.
        return new Promise((resolve) => {
          pendingRequests.push(() => resolve(axiosInstance(originalRequest)));
        });
      }

      isRefreshing = true;
      const newToken = await refreshAccessToken();
      isRefreshing = false;

      if (newToken) {
        pendingRequests.forEach((retry) => retry());
        pendingRequests = [];
        return axiosInstance(originalRequest);
      }

      // Refresh failed — session is genuinely over.
      useAuthStore.getState().clearAuth();
      window.location.href = "/login";
      return Promise.reject(error);
    }

    return Promise.reject(error);
  }
);

const configuration = new Configuration({ basePath: "" });

export const auditLogApi = new AuditLogApi(configuration, "", axiosInstance);
export const authApi = new AuthenticationApi(configuration, "", axiosInstance);
export const billingApi = new BillingEngineApi(configuration, "", axiosInstance);
export const invoicesApi = new InvoicesPaymentsApi(configuration, "", axiosInstance);
export const leasesApi = new LeasesApi(configuration, "", axiosInstance);
export const organizationsApi = new OrganizationsApi(configuration, "", axiosInstance);
export const propertiesApi = new PropertiesApi(configuration, "", axiosInstance);
export const staffMembersApi = new StaffMembersApi(configuration, "", axiosInstance);
export const tenantProfilesApi = new TenantProfilesApi(configuration, "", axiosInstance);