CREATE TABLE IF NOT EXISTS public.app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_app_user_username_not_blank CHECK (BTRIM(username) <> ''),
    CONSTRAINT chk_app_user_password_hash_not_blank CHECK (BTRIM(password_hash) <> ''),
    CONSTRAINT chk_app_user_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_user_username_ci
    ON public.app_user (LOWER(BTRIM(username)));

-- Development-only account. Change or remove it before deploying.
-- Username: admin
-- Password: Admin@123
INSERT INTO public.app_user (username, password_hash, role, is_active)
VALUES (
    'admin',
    '$2y$12$stslMH7nYfwzrqJc7UK6jeS.puWcQaxEf6ZvfNvKkk7mBtCUv01C6',
    'ADMIN',
    TRUE
)
ON CONFLICT DO NOTHING;
