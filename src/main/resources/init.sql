--DROP table IF EXISTS gallery CASCADE;
--DROP table IF EXISTS abajur_user CASCADE;
--DROP table IF EXISTS chat_message CASCADE;
--DROP table IF EXISTS team_statistic CASCADE;
--DROP table IF EXISTS settings CASCADE;

CREATE TABLE IF NOT EXISTS gallery(
    id BIGINT PRIMARY KEY,
    uid VARCHAR(100) UNIQUE
);

CREATE TABLE IF NOT EXISTS settings(
    code VARCHAR(100) PRIMARY KEY,
    val VARCHAR(1000)
);

truncate table settings;

insert into settings (code, val) values('INVITE', 'test');
insert into settings (code, val) values('PAGE_SIZE', '50');
insert into settings (code, val) values('STORAGE_DIR', '/opt/storage/abajur');

CREATE TABLE IF NOT EXISTS team_statistic(
    team_id INT not null,
    games INT not null,
    pos INT,
    points INT,
    percent VARCHAR(10)
);

CREATE SEQUENCE IF NOT EXISTS SEQ_USER;
CREATE SEQUENCE IF NOT EXISTS SEQ_MESSAGE;
CREATE SEQUENCE IF NOT EXISTS SEQ_GALLERY;

CREATE TABLE IF NOT EXISTS abajur_user(
    id BIGINT PRIMARY KEY,
    secret VARCHAR(100),
    name VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS chat_message(
    id BIGINT PRIMARY KEY,
    text VARCHAR(4000),
    type VARCHAR(20),
    create_date TIMESTAMP,
    author VARCHAR(100),
    author_name VARCHAR(200),
    file_id VARCHAR(100)
);

CREATE PRIMARY KEY IF NOT EXISTS PK_TEAM_STATISTIC ON team_statistic(team_id, games);
CREATE UNIQUE INDEX IF NOT EXISTS IDX_CM_FILE_ID ON chat_message(file_id);