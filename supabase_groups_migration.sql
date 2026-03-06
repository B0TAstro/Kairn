-- ============================================
-- Kairn - Groups Tables Migration
-- Create groups and group_members tables with enums
-- ============================================

-- 1. Create group_role enum
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'group_role') THEN
        CREATE TYPE public.group_role AS ENUM ('OWNER', 'ADMIN', 'MEMBER');
    END IF;
END $$;

-- 2. Create group_visibility enum
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'group_visibility') THEN
        CREATE TYPE public.group_visibility AS ENUM ('PUBLIC', 'PRIVATE');
    END IF;
END $$;

-- 3. Create groups table
CREATE TABLE IF NOT EXISTS public.groups (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    visibility public.group_visibility NOT NULL DEFAULT 'PRIVATE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT groups_pkey PRIMARY KEY (id),
    CONSTRAINT groups_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES profiles (id) ON DELETE CASCADE
) TABLESPACE pg_default;

-- Index for owner lookup
CREATE INDEX IF NOT EXISTS idx_groups_owner ON public.groups USING btree (owner_id) TABLESPACE pg_default;

-- Index for name search
CREATE INDEX IF NOT EXISTS idx_groups_name ON public.groups USING btree (name) TABLESPACE pg_default;

-- Trigger to update updated_at
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger 
        WHERE tgname = 'set_groups_updated_at'
    ) THEN
        CREATE TRIGGER set_groups_updated_at
            BEFORE UPDATE ON groups
            FOR EACH ROW
            EXECUTE FUNCTION update_updated_at();
    END IF;
END $$;

-- 4. Create group_members table
CREATE TABLE IF NOT EXISTS public.group_members (
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role public.group_role NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT group_members_pkey PRIMARY KEY (group_id, user_id),
    CONSTRAINT group_members_group_id_fkey FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
    CONSTRAINT group_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES profiles (id) ON DELETE CASCADE
) TABLESPACE pg_default;

-- Index for user lookup (find all groups for a user)
CREATE INDEX IF NOT EXISTS idx_group_members_user ON public.group_members USING btree (user_id) TABLESPACE pg_default;

-- Index for role filtering
CREATE INDEX IF NOT EXISTS idx_group_members_role ON public.group_members USING btree (role) TABLESPACE pg_default;

-- ============================================
-- Verification
-- ============================================

-- Check if enums were created
SELECT 
    typname as enum_name,
    enumlabel as value
FROM pg_type t
JOIN pg_enum e ON t.oid = e.enumtypid
WHERE typname IN ('group_role', 'group_visibility')
ORDER BY typname, enumlabel;

-- Check if tables were created
SELECT 
    tablename,
    schemaname
FROM pg_tables
WHERE schemaname = 'public' 
  AND tablename IN ('groups', 'group_members');

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ Groups tables created successfully!';
    RAISE NOTICE '✅ Enums: group_role (OWNER, ADMIN, MEMBER)';
    RAISE NOTICE '✅ Enums: group_visibility (PUBLIC, PRIVATE)';
    RAISE NOTICE '✅ Tables: groups, group_members';
END $$;
