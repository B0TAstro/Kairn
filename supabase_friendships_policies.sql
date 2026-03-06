-- ============================================
-- Kairn - Friendships Table RLS Policies
-- ============================================

-- Drop existing policies if they exist
DROP POLICY IF EXISTS "Users can view their own friendships" ON friendships;
DROP POLICY IF EXISTS "Users can create friend requests" ON friendships;
DROP POLICY IF EXISTS "Users can update their own friendship requests" ON friendships;
DROP POLICY IF EXISTS "Users can delete their own friendships" ON friendships;

-- Enable RLS
ALTER TABLE friendships ENABLE ROW LEVEL SECURITY;

-- Policy 1: Users can view friendships where they are involved
CREATE POLICY "Users can view their own friendships"
ON friendships
FOR SELECT
USING (
  requester_id = auth.uid() 
  OR addressee_id = auth.uid()
);

-- Policy 2: Users can create friend requests (as requester)
CREATE POLICY "Users can create friend requests"
ON friendships
FOR INSERT
WITH CHECK (requester_id = auth.uid());

-- Policy 3: Users can update friendship status
-- Only addressee can accept/decline, both can block
CREATE POLICY "Users can update their own friendship requests"
ON friendships
FOR UPDATE
USING (
  requester_id = auth.uid() 
  OR addressee_id = auth.uid()
)
WITH CHECK (
  requester_id = auth.uid() 
  OR addressee_id = auth.uid()
);

-- Policy 4: Users can delete their own friendships
CREATE POLICY "Users can delete their own friendships"
ON friendships
FOR DELETE
USING (
  requester_id = auth.uid() 
  OR addressee_id = auth.uid()
);

-- ============================================
-- Verification
-- ============================================

-- Check if RLS is enabled
-- SELECT tablename, rowsecurity FROM pg_tables WHERE tablename = 'friendships';

-- Check existing policies
-- SELECT * FROM pg_policies WHERE tablename = 'friendships';
