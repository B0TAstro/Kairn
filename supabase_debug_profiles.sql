-- ============================================
-- Kairn - Debug Script for Profiles Issue
-- ============================================

-- 1. Check if profiles table exists and has data
SELECT 
    'Total profiles' as check_name,
    COUNT(*) as count
FROM profiles;

-- 2. Check specific user profiles with username
SELECT 
    id,
    email,
    username,
    first_name,
    last_name,
    pseudo,
    avatar_url,
    created_at
FROM profiles
ORDER BY created_at DESC
LIMIT 10;

-- 3. Check if RLS is enabled on profiles
SELECT 
    tablename,
    rowsecurity as rls_enabled
FROM pg_tables 
WHERE schemaname = 'public' 
  AND tablename = 'profiles';

-- 4. Check existing RLS policies on profiles
SELECT 
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual,
    with_check
FROM pg_policies 
WHERE tablename = 'profiles';

-- 5. Check conversations and their members
SELECT 
    c.id as conversation_id,
    c.type,
    c.created_at,
    cm.user_id,
    p.username,
    p.email
FROM conversations c
JOIN conversation_members cm ON c.id = cm.conversation_id
LEFT JOIN profiles p ON cm.user_id = p.id
ORDER BY c.created_at DESC
LIMIT 20;

-- 6. Check if there are conversations with missing profile data
SELECT 
    c.id as conversation_id,
    c.type,
    cm.user_id,
    CASE WHEN p.id IS NULL THEN 'MISSING PROFILE' ELSE 'OK' END as profile_status
FROM conversations c
JOIN conversation_members cm ON c.id = cm.conversation_id
LEFT JOIN profiles p ON cm.user_id = p.id
WHERE p.id IS NULL;

-- 7. Check auth.users vs profiles (find users without profiles)
SELECT 
    au.id,
    au.email,
    au.created_at as user_created_at,
    p.id as profile_id,
    CASE WHEN p.id IS NULL THEN 'MISSING PROFILE' ELSE 'OK' END as status
FROM auth.users au
LEFT JOIN profiles p ON au.id = p.id
ORDER BY au.created_at DESC;

-- ============================================
-- Fix: Create missing profiles for auth.users
-- ============================================
-- Uncomment and run this if users are missing profiles:
/*
INSERT INTO profiles (id, email, username, created_at, updated_at)
SELECT 
    au.id,
    au.email,
    SPLIT_PART(au.email, '@', 1) as username, -- Use email prefix as username
    NOW(),
    NOW()
FROM auth.users au
LEFT JOIN profiles p ON au.id = p.id
WHERE p.id IS NULL;
*/
