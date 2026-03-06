-- ============================================
-- Kairn - Fix Missing Usernames
-- Update profiles with NULL or empty usernames
-- ============================================

-- Step 1: Check current state
SELECT 
    'Profiles with NULL/empty username' as check_name,
    COUNT(*) as count
FROM profiles
WHERE username IS NULL OR username = '' OR TRIM(username) = '';

-- Step 2: Show profiles that need fixing
SELECT 
    p.id,
    p.username,
    au.email,
    SPLIT_PART(au.email, '@', 1) as proposed_username
FROM profiles p
JOIN auth.users au ON p.id = au.id
WHERE p.username IS NULL OR p.username = '' OR TRIM(p.username) = ''
ORDER BY p.created_at DESC;

-- Step 3: Update NULL/empty usernames with email prefix
UPDATE profiles
SET 
    username = SPLIT_PART(
        (SELECT email FROM auth.users WHERE id = profiles.id),
        '@', 
        1
    ),
    updated_at = NOW()
WHERE username IS NULL OR username = '' OR TRIM(username) = '';

-- Step 4: Verify the fix
SELECT 
    'Profiles with NULL/empty username AFTER fix' as check_name,
    COUNT(*) as count
FROM profiles
WHERE username IS NULL OR username = '' OR TRIM(username) = '';

-- Step 5: Show all profiles with their usernames
SELECT 
    p.id,
    p.username,
    au.email,
    p.created_at
FROM profiles p
JOIN auth.users au ON p.id = au.id
ORDER BY p.created_at DESC
LIMIT 20;

-- Success message
DO $$
DECLARE
    updated_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO updated_count
    FROM profiles
    WHERE username IS NOT NULL AND username != '' AND TRIM(username) != '';
    
    RAISE NOTICE '========================================';
    RAISE NOTICE '✅ USERNAMES FIXED!';
    RAISE NOTICE '========================================';
    RAISE NOTICE '% profiles now have usernames', updated_count;
END $$;
