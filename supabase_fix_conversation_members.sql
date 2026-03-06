-- =============================================================
-- Fix missing conversation_members for DIRECT conversations
-- =============================================================
-- Problem: Some DIRECT conversations only have 1 member row
-- (the creator) instead of 2. This causes the app to show
-- "Unknown" for the other user's name.
--
-- This script:
-- 1. Diagnoses the problem (shows which conversations are affected)
-- 2. Uses the messages table to find the missing user
-- 3. Inserts the missing conversation_members rows
-- =============================================================

-- Step 1: Diagnostic - Show DIRECT conversations with member counts
DO $$
DECLARE
    rec RECORD;
BEGIN
    RAISE NOTICE '=== DIRECT conversations and their member counts ===';
    FOR rec IN
        SELECT 
            c.id AS conv_id,
            c.type,
            COUNT(cm.user_id) AS member_count,
            STRING_AGG(cm.user_id::text, ', ') AS member_ids
        FROM conversations c
        LEFT JOIN conversation_members cm ON cm.conversation_id = c.id
        WHERE c.type = 'DIRECT'
        GROUP BY c.id, c.type
        ORDER BY member_count ASC
    LOOP
        RAISE NOTICE 'Conv: %, members: %, ids: %', rec.conv_id, rec.member_count, rec.member_ids;
    END LOOP;
END $$;

-- Step 2: Diagnostic - Show senders in those conversations (to find missing members)
DO $$
DECLARE
    rec RECORD;
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '=== Unique senders per DIRECT conversation ===';
    FOR rec IN
        SELECT 
            c.id AS conv_id,
            COUNT(DISTINCT m.sender_id) AS sender_count,
            STRING_AGG(DISTINCT m.sender_id::text, ', ') AS sender_ids
        FROM conversations c
        LEFT JOIN messages m ON m.conversation_id = c.id
        WHERE c.type = 'DIRECT'
        GROUP BY c.id
    LOOP
        RAISE NOTICE 'Conv: %, senders: %, ids: %', rec.conv_id, rec.sender_count, rec.sender_ids;
    END LOOP;
END $$;

-- Step 3: Backfill - Insert missing conversation_members from messages senders
-- For each DIRECT conversation, find senders who are not in conversation_members and add them
INSERT INTO conversation_members (conversation_id, user_id)
SELECT DISTINCT m.conversation_id, m.sender_id
FROM messages m
INNER JOIN conversations c ON c.id = m.conversation_id
WHERE c.type = 'DIRECT'
  AND NOT EXISTS (
    SELECT 1 FROM conversation_members cm
    WHERE cm.conversation_id = m.conversation_id
      AND cm.user_id = m.sender_id
  )
ON CONFLICT (conversation_id, user_id) DO NOTHING;

-- Step 4: Verify fix
DO $$
DECLARE
    rec RECORD;
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '=== After fix: DIRECT conversations and their member counts ===';
    FOR rec IN
        SELECT 
            c.id AS conv_id,
            COUNT(cm.user_id) AS member_count,
            STRING_AGG(cm.user_id::text, ', ') AS member_ids
        FROM conversations c
        LEFT JOIN conversation_members cm ON cm.conversation_id = c.id
        WHERE c.type = 'DIRECT'
        GROUP BY c.id
        ORDER BY member_count ASC
    LOOP
        RAISE NOTICE 'Conv: %, members: %, ids: %', rec.conv_id, rec.member_count, rec.member_ids;
    END LOOP;
END $$;
