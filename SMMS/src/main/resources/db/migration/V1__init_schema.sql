CREATE TABLE organization (
                              id BIGSERIAL PRIMARY KEY,
                              name VARCHAR(255) NOT NULL
);

CREATE TABLE ticket (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        organization_id BIGINT NOT NULL REFERENCES organization(id)
);

INSERT INTO organization (name) VALUES ('Acme Corp'), ('Globex Inc');

INSERT INTO ticket (title, organization_id) VALUES
                                                ('Fix AC unit', 1),
                                                ('Replace light bulb', 1),
                                                ('Broken elevator', 2);