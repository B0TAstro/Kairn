-- =============================================
-- KAIRN - Chat System Migrations
-- Tables: conversations, conversation_members, messages
-- =============================================

-- 1. Create conversation_type enum
CREATE TYPE public.conversation_type AS ENUM ('DIRECT', 'GROUP');

-- 2. Create conversations table
CREATE TABLE public.conversations (
  id UUID NOT NULL DEFAULT gen_random_uuid(),
  type public.conversation_type NOT NULL DEFAULT 'DIRECT',
  group_id UUID NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT conversations_pkey PRIMARY KEY (id),
  CONSTRAINT conversations_group_id_fkey FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE
) TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS idx_conversations_type ON public.conversations USING btree (type) TABLESPACE pg_default;
CREATE INDEX IF NOT EXISTS idx_conversations_group ON public.conversations USING btree (group_id) TABLESPACE pg_default;

-- Trigger to update updated_at
CREATE TRIGGER set_conversations_updated_at 
  BEFORE UPDATE ON conversations 
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at();

-- 3. Create conversation_members table
CREATE TABLE public.conversation_members (
  conversation_id UUID NOT NULL,
  user_id UUID NOT NULL,
  last_read_message_id UUID NULL,
  joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT conversation_members_pkey PRIMARY KEY (conversation_id, user_id),
  CONSTRAINT conversation_members_conversation_id_fkey FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
  CONSTRAINT conversation_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES profiles (id) ON DELETE CASCADE
) TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS idx_conversation_members_user ON public.conversation_members USING btree (user_id) TABLESPACE pg_default;

-- 4. Create message_type enum
CREATE TYPE public.message_type AS ENUM ('TEXT', 'IMAGE', 'SYSTEM');

-- 5. Create messages table
CREATE TABLE public.messages (
  id UUID NOT NULL DEFAULT gen_random_uuid(),
  conversation_id UUID NOT NULL,
  sender_id UUID NOT NULL,
  body TEXT NOT NULL,
  message_type public.message_type NOT NULL DEFAULT 'TEXT',
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT messages_pkey PRIMARY KEY (id),
  CONSTRAINT messages_conversation_id_fkey FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
  CONSTRAINT messages_sender_id_fkey FOREIGN KEY (sender_id) REFERENCES profiles (id) ON DELETE CASCADE
) TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS idx_messages_conversation ON public.messages USING btree (conversation_id, created_at DESC) TABLESPACE pg_default;
CREATE INDEX IF NOT EXISTS idx_messages_sender ON public.messages USING btree (sender_id) TABLESPACE pg_default;

-- 6. Create friendships table (for direct messaging)
CREATE TYPE public.friendship_status AS ENUM ('PENDING', 'ACCEPTED', 'DECLINED', 'BLOCKED');

CREATE TABLE public.friendships (
  id UUID NOT NULL DEFAULT gen_random_uuid(),
  requester_id UUID NOT NULL,
  addressee_id UUID NOT NULL,
  status public.friendship_status NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT friendships_pkey PRIMARY KEY (id),
  CONSTRAINT friendships_requester_id_fkey FOREIGN KEY (requester_id) REFERENCES profiles (id) ON DELETE CASCADE,
  CONSTRAINT friendships_addressee_id_fkey FOREIGN KEY (addressee_id) REFERENCES profiles (id) ON DELETE CASCADE,
  CONSTRAINT friendships_unique_pair UNIQUE (requester_id, addressee_id)
) TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS idx_friendships_requester ON public.friendships USING btree (requester_id) TABLESPACE pg_default;
CREATE INDEX IF NOT EXISTS idx_friendships_addressee ON public.friendships USING btree (addressee_id) TABLESPACE pg_default;
CREATE INDEX IF NOT EXISTS idx_friendships_status ON public.friendships USING btree (status) TABLESPACE pg_default;

CREATE TRIGGER set_friendships_updated_at 
  BEFORE UPDATE ON friendships 
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at();

-- =============================================
-- Row Level Security (RLS) Policies
-- =============================================

-- Enable RLS
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversation_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.friendships ENABLE ROW LEVEL SECURITY;

-- Conversations: Users can only see conversations they are members of
CREATE POLICY "Users can view their own conversations"
  ON public.conversations FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM conversation_members
      WHERE conversation_members.conversation_id = conversations.id
        AND conversation_members.user_id = auth.uid()
    )
  );

-- Conversation Members: Users can view members of conversations they belong to
CREATE POLICY "Users can view members of their conversations"
  ON public.conversation_members FOR SELECT
  USING (
    conversation_id IN (
      SELECT conversation_id FROM conversation_members
      WHERE user_id = auth.uid()
    )
  );

-- Messages: Users can view messages in conversations they belong to
CREATE POLICY "Users can view messages in their conversations"
  ON public.messages FOR SELECT
  USING (
    conversation_id IN (
      SELECT conversation_id FROM conversation_members
      WHERE user_id = auth.uid()
    )
  );

-- Messages: Users can insert messages in conversations they belong to
CREATE POLICY "Users can send messages in their conversations"
  ON public.messages FOR INSERT
  WITH CHECK (
    sender_id = auth.uid() AND
    conversation_id IN (
      SELECT conversation_id FROM conversation_members
      WHERE user_id = auth.uid()
    )
  );

-- Friendships: Users can view friendships involving them
CREATE POLICY "Users can view their friendships"
  ON public.friendships FOR SELECT
  USING (
    requester_id = auth.uid() OR addressee_id = auth.uid()
  );

-- Friendships: Users can create friend requests
CREATE POLICY "Users can create friend requests"
  ON public.friendships FOR INSERT
  WITH CHECK (requester_id = auth.uid());

-- Friendships: Users can update friendships involving them
CREATE POLICY "Users can update their friendships"
  ON public.friendships FOR UPDATE
  USING (requester_id = auth.uid() OR addressee_id = auth.uid());

-- =============================================
-- Helper Functions
-- =============================================

-- Function to get or create a direct conversation between two users
CREATE OR REPLACE FUNCTION get_or_create_direct_conversation(
  user1_id UUID,
  user2_id UUID
)
RETURNS UUID
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
  conversation_id UUID;
BEGIN
  -- Check if conversation already exists
  SELECT cm1.conversation_id INTO conversation_id
  FROM conversation_members cm1
  INNER JOIN conversation_members cm2 
    ON cm1.conversation_id = cm2.conversation_id
  INNER JOIN conversations c 
    ON c.id = cm1.conversation_id
  WHERE cm1.user_id = user1_id
    AND cm2.user_id = user2_id
    AND c.type = 'DIRECT'
  LIMIT 1;

  -- If not found, create new conversation
  IF conversation_id IS NULL THEN
    INSERT INTO conversations (type) 
    VALUES ('DIRECT')
    RETURNING id INTO conversation_id;

    -- Add both users as members
    INSERT INTO conversation_members (conversation_id, user_id)
    VALUES 
      (conversation_id, user1_id),
      (conversation_id, user2_id);
  END IF;

  RETURN conversation_id;
END;
$$;

-- Function to get unread message count for a conversation
CREATE OR REPLACE FUNCTION get_unread_count(
  p_conversation_id UUID,
  p_user_id UUID
)
RETURNS INTEGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
  last_read_id UUID;
  unread_count INTEGER;
BEGIN
  -- Get last read message id
  SELECT last_read_message_id INTO last_read_id
  FROM conversation_members
  WHERE conversation_id = p_conversation_id
    AND user_id = p_user_id;

  -- Count unread messages
  IF last_read_id IS NULL THEN
    SELECT COUNT(*) INTO unread_count
    FROM messages
    WHERE conversation_id = p_conversation_id
      AND sender_id != p_user_id;
  ELSE
    SELECT COUNT(*) INTO unread_count
    FROM messages
    WHERE conversation_id = p_conversation_id
      AND sender_id != p_user_id
      AND created_at > (
        SELECT created_at FROM messages WHERE id = last_read_id
      );
  END IF;

  RETURN COALESCE(unread_count, 0);
END;
$$;
