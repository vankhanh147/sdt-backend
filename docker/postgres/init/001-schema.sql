CREATE TABLE category (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_category_code UNIQUE (code),
    CONSTRAINT uq_category_name UNIQUE (name)
);

CREATE TABLE sensitive_word (
    id UUID PRIMARY KEY,
    keyword VARCHAR(255) NOT NULL,
    priority_weight INTEGER NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_sensitive_word_keyword UNIQUE (keyword)
);

CREATE TABLE raw_feedback (
    id UUID PRIMARY KEY,
    source VARCHAR(30) NOT NULL,
    source_ref VARCHAR(255) NOT NULL,
    raw_title VARCHAR(500),
    raw_content TEXT NOT NULL,
    raw_author_name VARCHAR(255),
    raw_author_contact VARCHAR(255),
    raw_location VARCHAR(500),
    category_hint VARCHAR(100),
    raw_metadata JSONB,
    received_at TIMESTAMPTZ NOT NULL,
    processing_status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_raw_feedback_source_ref UNIQUE (source, source_ref)
);

CREATE TABLE feedback (
    id UUID PRIMARY KEY,
    raw_feedback_id UUID NOT NULL,
    title VARCHAR(500),
    content TEXT NOT NULL,
    author_name VARCHAR(255),
    author_contact VARCHAR(255),
    location VARCHAR(500),
    category VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_ANALYSIS',
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_feedback_raw_feedback_id UNIQUE (raw_feedback_id),
    CONSTRAINT fk_feedback_raw_feedback FOREIGN KEY (raw_feedback_id) REFERENCES raw_feedback (id)
);

CREATE TABLE analysis_result (
    id UUID PRIMARY KEY,
    feedback_id UUID NOT NULL,
    sentiment VARCHAR(30),
    sentiment_score NUMERIC(5, 4),
    category VARCHAR(100),
    category_score NUMERIC(5, 4),
    matched_keywords JSONB,
    priority VARCHAR(30),
    priority_score INTEGER,
    priority_reason TEXT,
    model_name VARCHAR(100),
    model_version VARCHAR(50),
    analysis_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    analyzed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_analysis_result_feedback FOREIGN KEY (feedback_id) REFERENCES feedback (id)
);

CREATE INDEX idx_category_is_active ON category (is_active);
CREATE INDEX idx_feedback_created_at ON feedback (created_at);
CREATE INDEX idx_analysis_result_feedback_created_at ON analysis_result (feedback_id, created_at DESC, id DESC);
