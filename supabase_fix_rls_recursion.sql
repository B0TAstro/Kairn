-- =============================================================
-- Fix RLS infinite recursion across all chat & group tables
-- =============================================================
--
-- PROBLEM:
--   Several RLS SELECT policies do subqueries on their own table
--   (e.g. conversation_members policy queries conversation_members,
--   group_members policy queries group_members), causing PostgreSQL
--   to re-evaluate the same policy → infinite recursion.
--
-- FIX:
--   Create SECURITY DEFINER helper functions that bypass RLS for
--   the inner lookups, breaking every recursion cycle.
--
-- This script is idempotent — safe to run multiple times.
-- =============================================================

-- ==================== HELPER FUNCTIONS ====================

-- Returns all conversation_ids that a user belongs to.
CREATE OR REPLACE FUNCTION get_user_conversation_ids(p_user_id uuid)
RETURNS SETOF uuid
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT conversation_id
  FROM conversation_members
  WHERE user_id = p_user_id;
$$;

-- Returns all group_ids that a user belongs to.
CREATE OR REPLACE FUNCTION get_user_group_ids(p_user_id uuid)
RETURNS SETOF uuid
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT group_id
  FROM group_members
  WHERE user_id = p_user_id;
$$;

-- Returns all group_ids where a user has OWNER or ADMIN role.
CREATE OR REPLACE FUNCTION get_user_admin_group_ids(p_user_id uuid)
RETURNS SETOF uuid
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT group_id
  FROM group_members
  WHERE user_id = p_user_id AND role IN ('OWNER', 'ADMIN');
$$;

-- Returns all group_ids where a user has OWNER role.
CREATE OR REPLACE FUNCTION get_user_owner_group_ids(p_user_id uuid)
RETURNS SETOF uuid
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT group_id
  FROM group_members
  WHERE user_id = p_user_id AND role = 'OWNER';
$$;

DO $$
BEGIN
    RAISE NOTICE 'Helper functions created';
END $$;

-- ==================== CONVERSATION_MEMBERS POLICIES ====================

ALTER TABLE conversation_members ENABLE ROW LEVEL SECURITY;

-- Drop ALL existing SELECT policies (both old and new) to start clean
DROP POLICY IF EXISTS "Users can view conversation members" ON conversation_members;
DROP POLICY IF EXISTS "Users can view members of their conversations" ON conversation_members;
DROP POLICY IF EXISTS "Users can create conversation memberships" ON conversation_members;
DROP POLICY IF EXISTS "Users can insert conversation members" ON conversation_members;
DROP POLICY IF EXISTS "Users can update their own membership" ON conversation_members;
DROP POLICY IF EXISTS "Users can delete conversation members" ON conversation_members;

-- SELECT: See all members of conversations you belong to.
-- Uses the SECURITY DEFINER function to avoid recursion.
CREATE POLICY "Users can view members of their conversations"
  ON conversation_members FOR SELECT
  USING (
    conversation_id IN (SELECT get_user_conversation_ids(auth.uid()))
  );

-- INSERT: Anyone authenticated can add members (app logic controls who can add)
CREATE POLICY "Users can insert conversation members"
  ON conversation_members FOR INSERT
  WITH CHECK (true);

-- UPDATE: Users can update their own membership (e.g., last_read_message_id)
CREATE POLICY "Users can update their own membership"
  ON conversation_members FOR UPDATE
  USING (user_id = auth.uid());

-- DELETE: Users can remove their own membership (leave) or app handles admin removal
CREATE POLICY "Users can delete conversation members"
  ON conversation_members FOR DELETE
  USING (user_id = auth.uid());

DO $$
BEGIN
    RAISE NOTICE 'conversation_members policies applied (no recursion)';
END $$;

-- ==================== PROFILES POLICIES ====================

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

-- Drop the problematic profiles policy that caused the circular dependency
DROP POLICY IF EXISTS "Users can view profiles in their conversations" ON profiles;
DROP POLICY IF EXISTS "Users can view their own profile" ON profiles;
DROP POLICY IF EXISTS "Users can view profiles of their friends" ON profiles;
DROP POLICY IF EXISTS "Users can update their own profile" ON profiles;
DROP POLICY IF EXISTS "Users can insert their own profile" ON profiles;

-- Own profile
CREATE POLICY "Users can view their own profile"
  ON profiles FOR SELECT
  USING (auth.uid() = id);

-- Profiles of people in your conversations — uses helper function to avoid recursion
CREATE POLICY "Users can view profiles in their conversations"
  ON profiles FOR SELECT
  USING (
    id IN (
      SELECT DISTINCT cm.user_id
      FROM conversation_members cm
      WHERE cm.conversation_id IN (SELECT get_user_conversation_ids(auth.uid()))
    )
  );

-- Profiles of friends
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

-- Update own profile
CREATE POLICY "Users can update their own profile"
  ON profiles FOR UPDATE
  USING (auth.uid() = id)
  WITH CHECK (auth.uid() = id);

-- Insert own profile (trigger also does this)
CREATE POLICY "Users can insert their own profile"
  ON profiles FOR INSERT
  WITH CHECK (auth.uid() = id);

DO $$
BEGIN
    RAISE NOTICE 'profiles policies applied (no recursion)';
END $$;

-- ==================== CONVERSATIONS POLICIES ====================
-- (Reapply cleanly — these also reference conversation_members)

ALTER TABLE conversations ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view their own conversations" ON conversations;
DROP POLICY IF EXISTS "Users can create conversations" ON conversations;
DROP POLICY IF EXISTS "Users can update their own conversations" ON conversations;

CREATE POLICY "Users can view their own conversations"
  ON conversations FOR SELECT
  USING (
    id IN (SELECT get_user_conversation_ids(auth.uid()))
  );

CREATE POLICY "Users can create conversations"
  ON conversations FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Users can update their own conversations"
  ON conversations FOR UPDATE
  USING (
    id IN (SELECT get_user_conversation_ids(auth.uid()))
  );

DO $$
BEGIN
    RAISE NOTICE 'conversations policies applied';
END $$;

-- ==================== MESSAGES POLICIES ====================
-- (Reapply cleanly — these also reference conversation_members)

ALTER TABLE messages ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view messages in their conversations" ON messages;
DROP POLICY IF EXISTS "Users can send messages in their conversations" ON messages;

CREATE POLICY "Users can view messages in their conversations"
  ON messages FOR SELECT
  USING (
    conversation_id IN (SELECT get_user_conversation_ids(auth.uid()))
  );

CREATE POLICY "Users can send messages in their conversations"
  ON messages FOR INSERT
  WITH CHECK (
    sender_id = auth.uid() AND
    conversation_id IN (SELECT get_user_conversation_ids(auth.uid()))
  );

DO $$
BEGIN
    RAISE NOTICE 'messages policies applied';
END $$;

-- ==================== GROUPS POLICIES ====================

ALTER TABLE groups ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Members can view their groups" ON groups;
DROP POLICY IF EXISTS "Users can create groups" ON groups;
DROP POLICY IF EXISTS "Owners and admins can update groups" ON groups;
DROP POLICY IF EXISTS "Owners can delete groups" ON groups;

-- SELECT: See groups you belong to (uses helper to avoid recursion via group_members)
CREATE POLICY "Members can view their groups"
  ON groups FOR SELECT
  USING (
    id IN (SELECT get_user_group_ids(auth.uid()))
  );

-- INSERT: Authenticated users can create groups (must be owner)
CREATE POLICY "Users can create groups"
  ON groups FOR INSERT
  WITH CHECK (owner_id = auth.uid());

-- UPDATE: Only owners and admins can update group details
CREATE POLICY "Owners and admins can update groups"
  ON groups FOR UPDATE
  USING (
    id IN (SELECT get_user_admin_group_ids(auth.uid()))
  )
  WITH CHECK (
    id IN (SELECT get_user_admin_group_ids(auth.uid()))
  );

-- DELETE: Only owners can delete groups
CREATE POLICY "Owners can delete groups"
  ON groups FOR DELETE
  USING (
    id IN (SELECT get_user_owner_group_ids(auth.uid()))
  );

DO $$
BEGIN
    RAISE NOTICE 'groups policies applied (no recursion)';
END $$;

-- ==================== GROUP_MEMBERS POLICIES ====================

ALTER TABLE group_members ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Members can view group members" ON group_members;
DROP POLICY IF EXISTS "Owners and admins can add members" ON group_members;
DROP POLICY IF EXISTS "Owners and admins can remove members" ON group_members;
DROP POLICY IF EXISTS "Members can leave groups" ON group_members;

-- SELECT: See all members of groups you belong to (uses helper to avoid recursion)
CREATE POLICY "Members can view group members"
  ON group_members FOR SELECT
  USING (
    group_id IN (SELECT get_user_group_ids(auth.uid()))
  );

-- INSERT: Owner of the group or existing admins can add members.
-- On first creation, the user is the group owner (groups.owner_id) but not yet
-- in group_members, so we check both group_members role AND groups.owner_id.
CREATE POLICY "Owners and admins can add members"
  ON group_members FOR INSERT
  WITH CHECK (
    group_id IN (SELECT get_user_admin_group_ids(auth.uid()))
    OR group_id IN (SELECT id FROM groups WHERE owner_id = auth.uid())
  );

-- DELETE (admin removal): Owners and admins can remove non-owner members
CREATE POLICY "Owners and admins can remove members"
  ON group_members FOR DELETE
  USING (
    group_id IN (SELECT get_user_admin_group_ids(auth.uid()))
    AND role != 'OWNER'
  );

-- DELETE (self-leave): Users can remove their own membership
CREATE POLICY "Members can leave groups"
  ON group_members FOR DELETE
  USING (user_id = auth.uid());

DO $$
BEGIN
    RAISE NOTICE 'group_members policies applied (no recursion)';
END $$;

-- ==================== VERIFICATION ====================

SELECT 
    tablename,
    policyname,
    cmd
FROM pg_policies 
WHERE tablename IN ('profiles', 'conversations', 'conversation_members', 'messages', 'groups', 'group_members')
ORDER BY tablename, policyname;

DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'ALL RLS POLICIES FIXED — NO RECURSION';
    RAISE NOTICE '========================================';
END $$;
