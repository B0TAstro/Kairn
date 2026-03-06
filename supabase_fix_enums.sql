-- ============================================
-- Kairn - Fix Group Enums
-- This script handles all cases for group_role and group_visibility enums
-- ============================================

-- STEP 1: Check what enums exist
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'CHECKING EXISTING ENUMS...';
    RAISE NOTICE '========================================';
END $$;

SELECT 
    t.typname as enum_name,
    e.enumlabel as value
FROM pg_type t
JOIN pg_enum e ON t.oid = e.enumtypid
WHERE t.typname IN ('group_role', 'group_visibility')
ORDER BY t.typname, e.enumlabel;

-- STEP 2: Drop and recreate group_role enum if needed
DO $$
BEGIN
    -- Check if enum exists
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'group_role') THEN
        RAISE NOTICE 'Enum group_role exists, dropping it...';
        
        -- Drop dependent tables first if they exist
        DROP TABLE IF EXISTS public.group_members CASCADE;
        RAISE NOTICE 'Dropped table: group_members';
        
        -- Now drop the enum
        DROP TYPE IF EXISTS public.group_role CASCADE;
        RAISE NOTICE 'Dropped enum: group_role';
    ELSE
        RAISE NOTICE 'Enum group_role does not exist';
    END IF;
    
    -- Create the enum with correct values
    CREATE TYPE public.group_role AS ENUM ('OWNER', 'ADMIN', 'MEMBER');
    RAISE NOTICE '✅ Created enum: group_role (OWNER, ADMIN, MEMBER)';
END $$;

-- STEP 3: Drop and recreate group_visibility enum if needed
DO $$
BEGIN
    -- Check if enum exists
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'group_visibility') THEN
        RAISE NOTICE 'Enum group_visibility exists, dropping it...';
        
        -- Drop dependent tables first if they exist
        DROP TABLE IF EXISTS public.groups CASCADE;
        RAISE NOTICE 'Dropped table: groups';
        
        -- Now drop the enum
        DROP TYPE IF EXISTS public.group_visibility CASCADE;
        RAISE NOTICE 'Dropped enum: group_visibility';
    ELSE
        RAISE NOTICE 'Enum group_visibility does not exist';
    END IF;
    
    -- Create the enum with correct values
    CREATE TYPE public.group_visibility AS ENUM ('PUBLIC', 'PRIVATE');
    RAISE NOTICE '✅ Created enum: group_visibility (PUBLIC, PRIVATE)';
END $$;

-- STEP 4: Recreate groups table
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
);

CREATE INDEX IF NOT EXISTS idx_groups_owner ON public.groups USING btree (owner_id);
CREATE INDEX IF NOT EXISTS idx_groups_name ON public.groups USING btree (name);

DO $$
BEGIN
    RAISE NOTICE '✅ Created table: groups';
END $$;

-- STEP 5: Recreate group_members table
CREATE TABLE IF NOT EXISTS public.group_members (
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role public.group_role NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT group_members_pkey PRIMARY KEY (group_id, user_id),
    CONSTRAINT group_members_group_id_fkey FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
    CONSTRAINT group_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES profiles (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_group_members_user ON public.group_members USING btree (user_id);
CREATE INDEX IF NOT EXISTS idx_group_members_role ON public.group_members USING btree (role);

DO $$
BEGIN
    RAISE NOTICE '✅ Created table: group_members';
END $$;

-- STEP 6: Update conversations table foreign key if needed
DO $$
BEGIN
    -- Check if conversations table has group_id column
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'conversations' 
          AND column_name = 'group_id'
    ) THEN
        -- Drop old foreign key if exists
        ALTER TABLE public.conversations 
        DROP CONSTRAINT IF EXISTS conversations_group_id_fkey;
        
        -- Add new foreign key
        ALTER TABLE public.conversations
        ADD CONSTRAINT conversations_group_id_fkey 
        FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE;
        
        RAISE NOTICE '✅ Updated conversations foreign key to groups';
    END IF;
END $$;

-- VERIFICATION
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ ENUMS AND TABLES FIXED!';
    RAISE NOTICE '========================================';
END $$;

-- Show final enum values
SELECT 
    t.typname as enum_name,
    e.enumlabel as value,
    e.enumsortorder as order_num
FROM pg_type t
JOIN pg_enum e ON t.oid = e.enumtypid
WHERE t.typname IN ('group_role', 'group_visibility')
ORDER BY t.typname, e.enumsortorder;

-- Show tables
SELECT 
    tablename,
    schemaname
FROM pg_tables
WHERE schemaname = 'public' 
  AND tablename IN ('groups', 'group_members')
ORDER BY tablename;
