-- Align user-service schema with monolith-style column names while keeping UUID IDs
-- This script is idempotent: it checks for existing column names before renaming.
-- PostgreSQL 11+ required for DO blocks used here.

BEGIN;

-- users: id -> user_id, password_hash -> password, avatar_url -> avatar
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'user_id'
    ) THEN
        EXECUTE 'ALTER TABLE public.users RENAME COLUMN id TO user_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'password_hash'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'password'
    ) THEN
        EXECUTE 'ALTER TABLE public.users RENAME COLUMN password_hash TO password';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'avatar_url'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'avatar'
    ) THEN
        EXECUTE 'ALTER TABLE public.users RENAME COLUMN avatar_url TO avatar';
    END IF;
END $$;

-- addresses: id -> address_id
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'addresses' AND column_name = 'id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'addresses' AND column_name = 'address_id'
    ) THEN
        EXECUTE 'ALTER TABLE public.addresses RENAME COLUMN id TO address_id';
    END IF;
END $$;

-- roles: name -> role_name
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'roles' AND column_name = 'name'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'roles' AND column_name = 'role_name'
    ) THEN
        EXECUTE 'ALTER TABLE public.roles RENAME COLUMN name TO role_name';
    END IF;
END $$;

-- permissions: name -> permission_name
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'permissions' AND column_name = 'name'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'permissions' AND column_name = 'permission_name'
    ) THEN
        EXECUTE 'ALTER TABLE public.permissions RENAME COLUMN name TO permission_name';
    END IF;
END $$;

-- role_permissions & user_roles already use role_name/permission_name/user_id columns.
-- tokens table left unchanged (no monolith column naming provided for it in dump excerpt).

COMMIT;
