-- ============================================
-- Kairn - Groups & Group Members RLS Policies
-- ============================================

-- ==================== GROUPS TABLE ====================

-- Drop existing policies
DROP POLICY IF EXISTS "Members can view their groups" ON groups;
DROP POLICY IF EXISTS "Users can create groups" ON groups;
DROP POLICY IF EXISTS "Owners and admins can update groups" ON groups;
DROP POLICY IF EXISTS "Owners can delete groups" ON groups;

-- Enable RLS
ALTER TABLE groups ENABLE ROW LEVEL SECURITY;

-- Policy 1: Users can view groups they are members of
CREATE POLICY "Members can view their groups"
ON groups
FOR SELECT
USING (
  id IN (
    SELECT group_id 
    FROM group_members 
    WHERE user_id = auth.uid()
  )
);

-- Policy 2: Users can create groups (as owner)
CREATE POLICY "Users can create groups"
ON groups
FOR INSERT
WITH CHECK (owner_id = auth.uid());

-- Policy 3: Owners and admins can update group info
CREATE POLICY "Owners and admins can update groups"
ON groups
FOR UPDATE
USING (
  id IN (
    SELECT group_id 
    FROM group_members 
    WHERE user_id = auth.uid() 
      AND role IN ('OWNER', 'ADMIN')
  )
)
WITH CHECK (
  id IN (
    SELECT group_id 
    FROM group_members 
    WHERE user_id = auth.uid() 
      AND role IN ('OWNER', 'ADMIN')
  )
);

-- Policy 4: Only owners can delete groups
CREATE POLICY "Owners can delete groups"
ON groups
FOR DELETE
USING (
  id IN (
    SELECT group_id 
    FROM group_members 
    WHERE user_id = auth.uid() 
      AND role = 'OWNER'
  )
);

-- ==================== GROUP_MEMBERS TABLE ====================

-- Drop existing policies
DROP POLICY IF EXISTS "Members can view group members" ON group_members;
DROP POLICY IF EXISTS "Owners and admins can add members" ON group_members;
DROP POLICY IF EXISTS "Owners and admins can remove members" ON group_members;
DROP POLICY IF EXISTS "Members can leave groups" ON group_members;

-- Enable RLS
ALTER TABLE group_members ENABLE ROW LEVEL SECURITY;

-- Policy 1: Users can view members of groups they belong to
CREATE POLICY "Members can view group members"
ON group_members
FOR SELECT
USING (
  group_id IN (
    SELECT group_id 
    FROM group_members 
    WHERE user_id = auth.uid()
  )
);

-- Policy 2: Owners and admins can add members
CREATE POLICY "Owners and admins can add members"
ON group_members
FOR INSERT
WITH CHECK (
  group_id IN (
    SELECT group_id 
    FROM group_members 
    WHERE user_id = auth.uid() 
      AND role IN ('OWNER', 'ADMIN')
  )
);

-- Policy 3: Owners and admins can remove members (but not owners)
CREATE POLICY "Owners and admins can remove members"
ON group_members
FOR DELETE
USING (
  group_id IN (
    SELECT gm.group_id 
    FROM group_members gm
    WHERE gm.user_id = auth.uid() 
      AND gm.role IN ('OWNER', 'ADMIN')
  )
  AND role != 'OWNER' -- Cannot remove owners
);

-- Policy 4: Members can remove themselves (leave group)
CREATE POLICY "Members can leave groups"
ON group_members
FOR DELETE
USING (user_id = auth.uid());

-- ============================================
-- Verification
-- ============================================

-- Check if RLS is enabled
-- SELECT tablename, rowsecurity FROM pg_tables WHERE tablename IN ('groups', 'group_members');

-- Check existing policies
-- SELECT tablename, policyname, cmd FROM pg_policies WHERE tablename IN ('groups', 'group_members') ORDER BY tablename, policyname;
