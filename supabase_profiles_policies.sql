-- ============================================
-- Kairn - Profiles Table RLS Policies
-- ============================================

-- Drop existing policies if they exist
DROP POLICY IF EXISTS "Users can view their own profile" ON profiles;
DROP POLICY IF EXISTS "Users can view profiles of their friends" ON profiles;
DROP POLICY IF EXISTS "Users can view profiles in their conversations" ON profiles;
DROP POLICY IF EXISTS "Users can update their own profile" ON profiles;

-- Enable RLS
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

-- Policy 1: Users can view their own profile
CREATE POLICY "Users can view their own profile"
ON profiles
FOR SELECT
USING (auth.uid() = id);

-- Policy 2: Users can view profiles of users in their conversations
-- This allows seeing names/avatars of people you chat with
CREATE POLICY "Users can view profiles in their conversations"
ON profiles
FOR SELECT
USING (
  id IN (
    SELECT DISTINCT cm.user_id
    FROM conversation_members cm
    WHERE cm.conversation_id IN (
      SELECT conversation_id
      FROM conversation_members
      WHERE user_id = auth.uid()
    )
  )
);

-- Policy 3: Users can view profiles of their friends
CREATE POLICY "Users can view profiles of their friends"
ON profiles
FOR SELECT
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

-- Policy 4: Users can update their own profile
CREATE POLICY "Users can update their own profile"
ON profiles
FOR UPDATE
USING (auth.uid() = id)
WITH CHECK (auth.uid() = id);

-- Policy 5: Users can insert their own profile
CREATE POLICY "Users can insert their own profile"
ON profiles
FOR INSERT
WITH CHECK (auth.uid() = id);

-- ============================================
-- Verification queries (run these to debug)
-- ============================================

-- Check if profiles exist for users
-- SELECT id, username, email, created_at FROM profiles;

-- Check if RLS is enabled
-- SELECT tablename, rowsecurity FROM pg_tables WHERE tablename = 'profiles';

-- Check existing policies
-- SELECT * FROM pg_policies WHERE tablename = 'profiles';
