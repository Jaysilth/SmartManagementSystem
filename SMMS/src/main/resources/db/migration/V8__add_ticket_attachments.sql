CREATE TABLE ticket_attachment (
                                   id BIGSERIAL PRIMARY KEY,
                                   ticket_id BIGINT NOT NULL REFERENCES ticket(id),
                                   organization_id BIGINT NOT NULL REFERENCES organization(id),
                                   uploaded_by BIGINT NOT NULL REFERENCES app_user(id),
                                   file_name VARCHAR(255) NOT NULL,
                                   content_type VARCHAR(100) NOT NULL,
                                   storage_key VARCHAR(500) NOT NULL,
                                   created_at TIMESTAMP NOT NULL DEFAULT now()
);