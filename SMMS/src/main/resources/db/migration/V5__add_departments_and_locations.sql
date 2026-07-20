CREATE TABLE department (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            organization_id BIGINT NOT NULL REFERENCES organization(id)
);

CREATE TABLE location (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          parent_location_id BIGINT REFERENCES location(id),
                          organization_id BIGINT NOT NULL REFERENCES organization(id),
                          UNIQUE (organization_id, parent_location_id, name)
);

ALTER TABLE ticket ADD COLUMN department_id BIGINT REFERENCES department(id);
ALTER TABLE ticket ADD COLUMN location_id BIGINT REFERENCES location(id);