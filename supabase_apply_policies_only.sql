-- ============================================
-- Kairn - Apply RLS Policies Only
-- Run this AFTER tables and enums are created
-- ============================================

-- ==================== PROFILES ====================

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view their own profile" ON profiles;
DROP POLICY IF EXISTS "Users can view profiles of their friends" ON profiles;
DROP POLICY IF EXISTS "Users can view profiles in their conversations" ON profiles;
DROP POLICY IF EXISTS "Users can update their own profile" ON profiles;
DROP POLICY IF EXISTS "Users can insert their own profile" ON profiles;

CREATE POLICY "Users can view their own profile"
ON profiles FOR SELECT
USING (auth.uid() = id);

CREATE POLICY "Users can view profiles in their conversations"
ON profiles FOR SELECT
USING (
  id IN (
    SELECT DISTINCT cm.user_id
    FROM conversation_members cm
    WHERE cm.conversation_id IN (
      SELECT conversation_id FROM conversation_members WHERE user_id = auth.uid()
    )
  )
);

CREATE POLICY "Users can view profiles of their friends"
ON profiles FOR SELECT
USING (
  id IN (
    SELECT CASE
      WHEN requester_id = auth.uid() THEN addressee_id
      WHEN addressee_id = auth.uid() THEN requester_id
    END
    FROM friendships
    WHERE (requester_id = auth.uid() OR addressee_id = auth.uid())
      AND status = 'ACCEPTED'
  )
);

CREATE POLICY "Users can update their own profile"
ON profiles FOR UPDATE
USING (auth.uid() = id)
WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can insert their own profile"
ON profiles FOR INSERT
WITH CHECK (auth.uid() = id);

DO $$
BEGIN
    RAISE NOTICE '✅ Profiles policies applied';
END $$;

-- ==================== FRIENDSHIPS ====================

ALTER TABLE friendships ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view their own friendships" ON friendships;
DROP POLICY IF EXISTS "Users can create friend requests" ON friendships;
DROP POLICY IF EXISTS "Users can update their own friendship requests" ON friendships;
DROP POLICY IF EXISTS "Users can delete their own friendships" ON friendships;

CREATE POLICY "Users can view their own friendships"
ON friendships FOR SELECT
USING (requester_id = auth.uid() OR addressee_id = auth.uid());

CREATE POLICY "Users can create friend requests"
ON friendships FOR INSERT
WITH CHECK (requester_id = auth.uid());

CREATE POLICY "Users can update their own friendship requests"
ON friendships FOR UPDATE
USING (requester_id = auth.uid() OR addressee_id = auth.uid())
WITH CHECK (requester_id = auth.uid() OR addressee_id = auth.uid());

CREATE POLICY "Users can delete their own friendships"
ON friendships FOR DELETE
USING (requester_id = auth.uid() OR addressee_id = auth.uid());

DO $$
BEGIN
    RAISE NOTICE '✅ Friendships policies applied';
END $$;

-- ==================== GROUPS (only if table exists) ====================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'groups' AND schemaname = 'public') THEN
        ALTER TABLE groups ENABLE ROW LEVEL SECURITY;
        
        DROP POLICY IF EXISTS "Members can view their groups" ON groups;
        DROP POLICY IF EXISTS "Users can create groups" ON groups;
        DROP POLICY IF EXISTS "Owners and admins can update groups" ON groups;
        DROP POLICY IF EXISTS "Owners can delete groups" ON groups;
        
        EXECUTE 'CREATE POLICY "Members can view their groups" ON groups FOR SELECT USING (id IN (SELECT group_id FROM group_members WHERE user_id = auth.uid()))';
        EXECUTE 'CREATE POLICY "Users can create groups" ON groups FOR INSERT WITH CHECK (owner_id = auth.uid())';
        EXECUTE 'CREATE POLICY "Owners and admins can update groups" ON groups FOR UPDATE USING (id IN (SELECT group_id FROM group_members WHERE user_id = auth.uid() AND role IN (''OWNER'', ''ADMIN''))) WITH CHECK (id IN (SELECT group_id FROM group_members WHERE user_id = auth.uid() AND role IN (''OWNER'', ''ADMIN'')))';
        EXECUTE 'CREATE POLICY "Owners can delete groups" ON groups FOR DELETE USING (id IN (SELECT group_id FROM group_members WHERE user_id = auth.uid() AND role = ''OWNER''))';
        
        RAISE NOTICE '✅ Groups policies applied';
    ELSE
        RAISE NOTICE '⚠️  Table groups does not exist, skipping';
    END IF;
END $$;

-- ==================== GROUP_MEMBERS (only if table exists) ====================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'group_members' AND schemaname = 'public') THEN
        ALTER TABLE group_members ENABLE ROW LEVEL SECURITY;
        
        DROP POLICY IF EXISTS "Members can view group members" ON group_members;
        DROP POLICY IF EXISTS "Owners and admins can add members" ON group_members;
        DROP POLICY IF EXISTS "Owners and admins can remove members" ON group_members;
        DROP POLICY IF EXISTS "Members can leave groups" ON group_members;
        
        EXECUTE 'CREATE POLICY "Members can view group members" ON group_members FOR SELECT USING (group_id IN (SELECT group_id FROM group_members WHERE user_id = auth.uid()))';
        EXECUTE 'CREATE POLICY "Owners and admins can add members" ON group_members FOR INSERT WITH CHECK (group_id IN (SELECT group_id FROM group_members WHERE user_id = auth.uid() AND role IN (''OWNER'', ''ADMIN'')))';
        EXECUTE 'CREATE POLICY "Owners and admins can remove members" ON group_members FOR DELETE USING (group_id IN (SELECT gm.group_id FROM group_members gm WHERE gm.user_id = auth.uid() AND gm.role IN (''OWNER'', ''ADMIN'')) AND role != ''OWNER'')';
        EXECUTE 'CREATE POLICY "Members can leave groups" ON group_members FOR DELETE USING (user_id = auth.uid())';
        
        RAISE NOTICE '✅ Group_members policies applied';
    ELSE
        RAISE NOTICE '⚠️  Table group_members does not exist, skipping';
    END IF;
END $$;

-- ==================== PROFILE TRIGGER ====================

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
DROP FUNCTION IF EXISTS public.handle_new_user();

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.profiles (id, username, created_at, updated_at)
  VALUES (
    NEW.id,
    COALESCE(
      NEW.raw_user_meta_data->>'username',
      NEW.raw_user_meta_data->>'pseudo',
      SPLIT_PART(NEW.email, '@', 1)
    ),
    NOW(),
    NOW()
  )
  ON CONFLICT (id) DO NOTHING;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_new_user();

DO $$
BEGIN
    RAISE NOTICE '✅ Profile trigger created';
END $$;

-- ==================== BACKFILL PROFILES ====================

INSERT INTO public.profiles (id, username, created_at, updated_at)
SELECT 
    au.id,
    COALESCE(
      au.raw_user_meta_data->>'username',
      au.raw_user_meta_data->>'pseudo',
      SPLIT_PART(au.email, '@', 1)
    ) as username,
    NOW(),
    NOW()
FROM auth.users au
LEFT JOIN public.profiles p ON au.id = p.id
WHERE p.id IS NULL
ON CONFLICT (id) DO NOTHING;

DO $$
BEGIN
    RAISE NOTICE '✅ Missing profiles backfilled';
END $$;

-- ==================== VERIFICATION ====================

SELECT 
    tablename,
    COUNT(*) as policy_count
FROM pg_policies 
WHERE tablename IN ('profiles', 'friendships', 'groups', 'group_members')
GROUP BY tablename
ORDER BY tablename;

SELECT 
    COUNT(*) as users_without_profiles
FROM auth.users au
LEFT JOIN profiles p ON au.id = p.id
WHERE p.id IS NULL;

DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ ALL POLICIES APPLIED!';
    RAISE NOTICE '========================================';
END $$;
