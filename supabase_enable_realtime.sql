-- =============================================
-- Enable Realtime for Chat Tables
-- =============================================
-- This script adds the necessary tables to the supabase_realtime publication
-- so that Realtime subscriptions work for chat functionality.
--
-- Execute this in the Supabase SQL Editor:
-- Database → SQL Editor → New Query → Paste this → Run

-- Add messages table to Realtime publication
ALTER PUBLICATION supabase_realtime ADD TABLE messages;

-- Optionally, also add conversations table if you want real-time conversation updates
ALTER PUBLICATION supabase_realtime ADD TABLE conversations;

-- Verify the publication includes our tables
SELECT schemaname, tablename 
FROM pg_publication_tables 
WHERE pubname = 'supabase_realtime'
ORDER BY tablename;

-- Expected output should include:
-- public | messages
-- public | conversations (if added)
