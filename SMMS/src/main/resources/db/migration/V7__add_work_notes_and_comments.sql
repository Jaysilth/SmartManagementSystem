CREATE TABLE work_note (
                           id BIGSERIAL PRIMARY KEY,
                           ticket_id BIGINT NOT NULL REFERENCES ticket(id),
                           author_id BIGINT NOT NULL REFERENCES app_user(id),
                           content TEXT NOT NULL,
                           organization_id BIGINT NOT NULL REFERENCES organization(id),
                           created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE ticket_comment (
                                id BIGSERIAL PRIMARY KEY,
                                ticket_id BIGINT NOT NULL REFERENCES ticket(id),
                                author_id BIGINT NOT NULL REFERENCES app_user(id),
                                content TEXT NOT NULL,
                                organization_id BIGINT NOT NULL REFERENCES organization(id),
                                created_at TIMESTAMP NOT NULL DEFAULT now()
);