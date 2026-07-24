CREATE TABLE refresh_tokens
(
    id          UUID         NOT NULL,
    token       VARCHAR(255) NOT NULL,
    user_id     UUID,
    expiry_date TIMESTAMP(6) WITHOUT TIME ZONE,
    revoked     BOOLEAN      NOT NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id)
);

CREATE TABLE task_execution
(
    id                    UUID NOT NULL,
    created_at            TIMESTAMP(6) WITHOUT TIME ZONE,
    updated_at            TIMESTAMP(6) WITHOUT TIME ZONE,
    workflow_execution_id UUID,
    node_id               VARCHAR(255),
    node_type             VARCHAR(255),
    status                VARCHAR(255),
    output_data           JSONB,
    log_message           VARCHAR(255),
    CONSTRAINT pk_task_execution PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         UUID         NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(255) NOT NULL,
    enabled    BOOLEAN      NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE workflow
(
    id          UUID         NOT NULL,
    created_at  TIMESTAMP(6) WITHOUT TIME ZONE,
    updated_at  TIMESTAMP(6) WITHOUT TIME ZONE,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    user_id     UUID         NOT NULL,
    status      VARCHAR(255) NOT NULL,
    CONSTRAINT pk_workflow PRIMARY KEY (id)
);

CREATE TABLE workflow_definition
(
    id              UUID  NOT NULL,
    created_at      TIMESTAMP(6) WITHOUT TIME ZONE,
    updated_at      TIMESTAMP(6) WITHOUT TIME ZONE,
    workflow_id     UUID  NOT NULL,
    definition_json JSONB NOT NULL,
    version         INTEGER,
    CONSTRAINT pk_workflow_definition PRIMARY KEY (id)
);

CREATE TABLE workflow_execution
(
    id              UUID NOT NULL,
    created_at      TIMESTAMP(6) WITHOUT TIME ZONE,
    updated_at      TIMESTAMP(6) WITHOUT TIME ZONE,
    workflow_id     UUID,
    status          VARCHAR(255),
    error_message   VARCHAR(255),
    resume_at       TIMESTAMP(6) WITHOUT TIME ZONE,
    waiting_node_id VARCHAR(255),
    CONSTRAINT pk_workflow_execution PRIMARY KEY (id)
);

CREATE TABLE workflow_webhook
(
    id          UUID NOT NULL,
    workflow_id UUID,
    webhook_key VARCHAR(255),
    CONSTRAINT pk_workflow_webhook PRIMARY KEY (id)
);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT uc_refresh_tokens_token UNIQUE (token);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE workflow_webhook
    ADD CONSTRAINT uc_workflow_webhook_webhookkey UNIQUE (webhook_key);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT FK_REFRESH_TOKENS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);