BEGIN;

CREATE TABLE public.notification (
    id UUID CONSTRAINT pk_notification PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    related_feedback_id UUID,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notification_feedback FOREIGN KEY (related_feedback_id)
        REFERENCES public.feedback (id)
        ON DELETE SET NULL,
    CONSTRAINT chk_notification_type CHECK (
        type IN (
            'NEW_FEEDBACK',
            'FEEDBACK_STATUS_CHANGED',
            'ANALYSIS_COMPLETED',
            'HIGH_PRIORITY',
            'URGENT_PRIORITY'
        )
    ),
    CONSTRAINT chk_notification_title_not_blank CHECK (BTRIM(title) <> ''),
    CONSTRAINT chk_notification_message_not_blank CHECK (BTRIM(message) <> ''),
    CONSTRAINT chk_notification_read_state CHECK (
        (is_read = FALSE AND read_at IS NULL)
        OR
        (is_read = TRUE AND read_at IS NOT NULL)
    )
);

CREATE INDEX idx_notification_created
    ON public.notification (created_at DESC, id DESC);

CREATE INDEX idx_notification_unread_created
    ON public.notification (created_at DESC, id DESC)
    WHERE is_read = FALSE;

CREATE INDEX idx_notification_feedback
    ON public.notification (related_feedback_id)
    WHERE related_feedback_id IS NOT NULL;

COMMIT;
