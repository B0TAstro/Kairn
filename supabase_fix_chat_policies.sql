-- =============================================================
-- Fix RLS policies for conversations and conversation_members
-- =============================================================
-- Problem: conversation_members RLS policy is missing or too
-- restrictive, blocking reads of other members in conversations
-- the current user belongs to.
-- =============================================================

-- ==================== CONVERSATIONS ====================

ALTER TABLE conversations ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view their own conversations" ON conversations;
DROP POLICY IF EXISTS "Users can create conversations" ON conversations;
DROP POLICY IF EXISTS "Users can update their own conversations" ON conversations;

CREATE POLICY "Users can view their own conversations"
  ON conversations FOR SELECT
  USING (
    id IN (
      SELECT conversation_id FROM conversation_members WHERE user_id = auth.uid()
    )
  );

CREATE POLICY "Users can create conversations"
  ON conversations FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Users can update their own conversations"
  ON conversations FOR UPDATE
  USING (
    id IN (
      SELECT conversation_id FROM conversation_members WHERE user_id = auth.uid()
    )
  );

DO $$
BEGIN
    RAISE NOTICE 'Conversations policies applied';
END $$;

-- ==================== CONVERSATION_MEMBERS ====================

ALTER TABLE conversation_members ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view members of their conversations" ON conversation_members;
DROP POLICY IF EXISTS "Users can insert conversation members" ON conversation_members;
DROP POLICY IF EXISTS "Users can update their own membership" ON conversation_members;
DROP POLICY IF EXISTS "Users can delete conversation members" ON conversation_members;

-- KEY FIX: Allow users to see ALL members of conversations they belong to
CREATE POLICY "Users can view members of their conversations"
  ON conversation_members FOR SELECT
  USING (
    conversation_id IN (
      SELECT conversation_id FROM conversation_members WHERE user_id = auth.uid()
    )
  );

CREATE POLICY "Users can insert conversation members"
  ON conversation_members FOR INSERT
  WITH CHECK (true);

CREATE POLICY "Users can update their own membership"
  ON conversation_members FOR UPDATE
  USING (user_id = auth.uid());

CREATE POLICY "Users can delete conversation members"
  ON conversation_members FOR DELETE
  USING (user_id = auth.uid());

DO $$
BEGIN
    RAISE NOTICE 'Conversation_members policies applied';
END $$;

-- ==================== MESSAGES ====================

ALTER TABLE messages ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view messages in their conversations" ON messages;
DROP POLICY IF EXISTS "Users can send messages in their conversations" ON messages;

CREATE POLICY "Users can view messages in their conversations"
  ON messages FOR SELECT
  USING (
    conversation_id IN (
      SELECT conversation_id FROM conversation_members WHERE user_id = auth.uid()
    )
  );

CREATE POLICY "Users can send messages in their conversations"
  ON messages FOR INSERT
  WITH CHECK (
    sender_id = auth.uid() AND
    conversation_id IN (
      SELECT conversation_id FROM conversation_members WHERE user_id = auth.uid()
    )
  );

DO $$
BEGIN
    RAISE NOTICE 'Messages policies applied';
END $$;

-- ==================== VERIFICATION ====================

SELECT 
    tablename,
    policyname
FROM pg_policies 
WHERE tablename IN ('conversations', 'conversation_members', 'messages')
ORDER BY tablename, policyname;

DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'ALL CHAT POLICIES APPLIED!';
    RAISE NOTICE '========================================';
END $$;
