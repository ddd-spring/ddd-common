-- Table: common.wl_es_event_store

-- DROP TABLE common.wl_es_event_store;

CREATE TABLE common.wl_es_event_store
(
    event_id bigint NOT NULL DEFAULT nextval('wl_es_event_store_event_id_seq'::regclass),
    event_body character varying(255) COLLATE pg_catalog."default",
    event_type character varying(250) COLLATE pg_catalog."default" NOT NULL,
    stream_name character varying(250) COLLATE pg_catalog."default" NOT NULL,
    stream_version integer NOT NULL,
    CONSTRAINT wl_es_stored_event_pkey PRIMARY KEY (event_id)
)

TABLESPACE pg_default;

ALTER TABLE common.wl_es_event_store
    OWNER to postgres;

-- Index: stream_name

-- DROP INDEX common.stream_name;

CREATE INDEX stream_name
    ON common.wl_es_event_store USING btree
    (stream_name COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: stream_name_version

-- DROP INDEX common.stream_name_version;

CREATE UNIQUE INDEX stream_name_version
    ON common.wl_es_event_store USING btree
    (stream_name COLLATE pg_catalog."default" ASC NULLS LAST, stream_version ASC NULLS LAST)
    TABLESPACE pg_default;


-- wl_published_notification_tracker ->>mapper>>PublishedNotificationTracker.hbm.xml;
-- wl_stored_event>>mapper>>StoredEvent.hbm.xml;
-- wl_time_constrained_process_tracker>>mapper>>TimeConstrainedProcessTracker.hbm.xml;
-- TestableTimeConstrainedProcess.hbm.xml
