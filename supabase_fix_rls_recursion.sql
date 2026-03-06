-- =============================================================
-- Fix RLS infinite recursion between profiles & conversation_members
-- =============================================================
--
-- PROBLEM:
--   profiles SELECT policy "Users can view profiles in their conversations"
--   does a subquery on conversation_members, which triggers conversation_members
--   SELECT policy "Users can view members of their conversations", which does
--   a subquery back on conversation_members itself → infinite recursion.
--
-- FIX:
--   1. Create a SECURITY DEFINER function that returns the conversation IDs
--      for a given user. Because it runs as the function owner (superuser),
--      it bypasses RLS on conversation_members, breaking the cycle.
--   2. Replace ALL SELECT policies on conversation_members with ONE that
--      calls this helper function instead of a raw subquery.
--   3. Replace the profiles "in their conversations" policy to also use
--      the helper function.
--
-- This script is idempotent — safe to run multiple times.
-- =============================================================

-- ==================== HELPER FUNCTION ====================

-- Returns all conversation_ids that a user belongs to.
-- SECURITY DEFINER = runs as the function owner, bypassing RLS.
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

DO $$
BEGIN
    RAISE NOTICE 'Helper function get_user_conversation_ids created';
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

-- ==================== VERIFICATION ====================

SELECT 
    tablename,
    policyname,
    cmd
FROM pg_policies 
WHERE tablename IN ('profiles', 'conversations', 'conversation_members', 'messages')
ORDER BY tablename, policyname;

DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'ALL RLS POLICIES FIXED — NO RECURSION';
    RAISE NOTICE '========================================';
END $$;
