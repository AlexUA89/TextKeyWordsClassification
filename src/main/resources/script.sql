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
    words     VARCHAR   NOT NULL,
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

insert into devs_strings.labelers (name) values ('alex');
insert into devs_strings.labelers (name) values ('blysh@nubip.edu.ua');
insert into devs_strings.labelers (name) values ('delffinnn@gmail.com');
insert into devs_strings.labelers (name) values ('iryna.sharova@icloud.com');
insert into devs_strings.labelers (name) values ('natalia.shchudla@gmail.com');
insert into devs_strings.labelers (name) values ('azik.haciyev@gmail.com');
insert into devs_strings.labelers (name) values ('meghdad@Archipelo.co');

insert into DEVS_STRINGS.job (name, persons_per_task) values ('JOB_1', 1);