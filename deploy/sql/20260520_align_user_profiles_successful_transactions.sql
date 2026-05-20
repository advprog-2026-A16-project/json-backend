DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'user_profiles'
          AND column_name = 'successful_transaction'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'user_profiles'
          AND column_name = 'successful_transactions'
    ) THEN
        ALTER TABLE public.user_profiles
            RENAME COLUMN successful_transaction TO successful_transactions;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'user_profiles'
          AND column_name = 'successful_transactions'
    ) THEN
        ALTER TABLE public.user_profiles
            ADD COLUMN successful_transactions integer NOT NULL DEFAULT 0;
    END IF;
END $$;
