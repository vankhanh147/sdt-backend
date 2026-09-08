-- Development-only account. Run after 001_create_app_user_table.sql.
-- Username: admin
-- Password: Admin@123
-- Change or remove this account before deploying outside local development.

INSERT INTO public.app_user (id, username, password_hash, role, is_active)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'admin',
    '$2y$12$stslMH7nYfwzrqJc7UK6jeS.puWcQaxEf6ZvfNvKkk7mBtCUv01C6',
    'ADMIN',
    TRUE
)
ON CONFLICT DO NOTHING;
