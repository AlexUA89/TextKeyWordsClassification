DROP SCHEMA IF EXISTS DEVS_STRINGS CASCADE;
DROP
USER IF EXISTS labeler;
CREATE schema DEVS_STRINGS;
CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE DEVS_STRINGS.labelers
(
    id           uuid PRIMARY KEY   DEFAULT uuid_generate_v4(),
    name         VARCHAR   NOT NULL UNIQUE,
    created_time timestamp NOT NULL DEFAULT now()
);

CREATE TABLE DEVS_STRINGS.JOB
(
    id               uuid PRIMARY KEY   DEFAULT uuid_generate_v4(),
    name             VARCHAR   NOT NULL UNIQUE,
    keywords             VARCHAR   NOT NULL,
    persons_per_task integer   NOT NULL,
    created_time     timestamp NOT NULL DEFAULT now()
);

CREATE TABLE DEVS_STRINGS.TASKS
(
    id           uuid PRIMARY KEY   DEFAULT uuid_generate_v4(),
    job_id       uuid REFERENCES DEVS_STRINGS.JOB (id),
    query     VARCHAR   NOT NULL,
    link     VARCHAR   NOT NULL,
    title     VARCHAR   NOT NULL,
    description     VARCHAR   NOT NULL,
    content     VARCHAR   NOT NULL,
    created_time timestamp NOT NULL DEFAULT now()
);

CREATE TABLE DEVS_STRINGS.RESULTS
(
    id           uuid PRIMARY KEY   DEFAULT uuid_generate_v4(),
    task_id      uuid REFERENCES DEVS_STRINGS.tasks (id),
    labeler_id   uuid REFERENCES DEVS_STRINGS.labelers (id),
    result_json    VARCHAR,
    saved_date   timestamp,
    created_time timestamp NOT NULL DEFAULT now()
);

CREATE
USER labeler WITH PASSWORD 'labeling_archipelo';
-- ALTER DEFAULT PRIVILEGES IN SCHEMA DEVS_STRINGS GRANT INSERT ON TABLES TO labeler;
GRANT USAGE ON SCHEMA
DEVS_STRINGS TO labeler ;
GRANT
SELECT
ON ALL TABLES IN SCHEMA DEVS_STRINGS TO labeler;
GRANT INSERT ON TABLE DEVS_STRINGS.RESULTS TO labeler;
GRANT UPDATE ON TABLE DEVS_STRINGS.RESULTS TO labeler;

ALTER TABLE DEVS_STRINGS.RESULTS ADD COLUMN classification VARCHAR DEFAULT 'NON-DEV';