BEGIN;

CREATE TABLE public.category (
    id UUID CONSTRAINT pk_category PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_category_code UNIQUE (code),
    CONSTRAINT uq_category_name UNIQUE (name),
    CONSTRAINT chk_category_code_not_blank CHECK (BTRIM(code) <> ''),
    CONSTRAINT chk_category_name_not_blank CHECK (BTRIM(name) <> ''),
    CONSTRAINT chk_category_code_format CHECK (
        code = UPPER(code)
        AND code ~ '^[A-Z0-9_]+$'
    )
);

CREATE INDEX idx_category_is_active
    ON public.category (is_active);

COMMIT;
