DROP INDEX IF EXISTS public.idx_audit_tenant_occurred;
DROP INDEX IF EXISTS public.idx_audit_tenant_actor;
DROP INDEX IF EXISTS public.idx_audit_tenant_entity;
DROP TABLE IF EXISTS public.system_audit_logs CASCADE;
