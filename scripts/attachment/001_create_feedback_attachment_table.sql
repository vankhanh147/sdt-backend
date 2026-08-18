BEGIN;

CREATE TABLE public.feedback_attachment (
    id UUID CONSTRAINT pk_feedback_attachment PRIMARY KEY DEFAULT gen_random_uuid(),
    feedback_id UUID NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_path VARCHAR(1024) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_feedback_attachment_feedback FOREIGN KEY (feedback_id)
        REFERENCES public.feedback (id)
        ON DELETE CASCADE,
    CONSTRAINT uq_feedback_attachment_storage_path UNIQUE (storage_path),
    CONSTRAINT chk_feedback_attachment_file_size CHECK (
        file_size > 0
        AND file_size <= 5242880
    ),
    CONSTRAINT chk_feedback_attachment_content_type CHECK (
        content_type IN ('image/jpeg', 'image/png', 'image/webp')
    )
);

CREATE INDEX idx_feedback_attachment_feedback_created
    ON public.feedback_attachment (feedback_id, created_at, id);

COMMIT;
