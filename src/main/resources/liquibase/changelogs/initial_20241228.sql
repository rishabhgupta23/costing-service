--liquibase formatted sql

--changeset Rishabh:20241228-1
--tagDatabase: 'init'
--rollback empty

--changeset Rishabh:20241228-2
CREATE SCHEMA app;
--rollback DROP SCHEMA app;

--changeset Rishabh:20241228-3
CREATE TABLE IF NOT EXISTS app.app_user
(
    delete_flag integer DEFAULT 0,
    created_by bigint,
    created_date_time timestamp(6) without time zone,
    updated_by bigint,
    updated_date_time timestamp(6) without time zone,
    user_id bigserial NOT NULL,
    email_id character varying(50) COLLATE pg_catalog."default" NOT NULL,
    name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    password character varying(100) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT app_user_pkey PRIMARY KEY (user_id),
    CONSTRAINT app_user_email_id_key UNIQUE (email_id)
);
CREATE TABLE IF NOT EXISTS app.vendor
(
    delete_flag integer DEFAULT 0,
    created_by bigint,
    created_date_time timestamp(6) without time zone,
    updated_by bigint,
    updated_date_time timestamp(6) without time zone,
    vendor_id bigint NOT NULL,
    address character varying(255) COLLATE pg_catalog."default",
    contact_number character varying(15) COLLATE pg_catalog."default",
    email_id character varying(50) COLLATE pg_catalog."default" NOT NULL,
    name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT vendor_pkey PRIMARY KEY (vendor_id),
    CONSTRAINT vendor_name_key UNIQUE (name)
);
CREATE TABLE IF NOT EXISTS app.part
(
    delete_flag integer DEFAULT 0,
    created_by bigint,
    created_date_time timestamp(6) without time zone,
    part_id bigint NOT NULL,
    updated_by bigint,
    updated_date_time timestamp(6) without time zone,
    category_name character varying(50) COLLATE pg_catalog."default",
    part_name character varying(50) COLLATE pg_catalog."default",
    part_number character varying(50) COLLATE pg_catalog."default",
    type character varying(10) COLLATE pg_catalog."default",
    unit character varying(10) COLLATE pg_catalog."default",
    CONSTRAINT part_pkey PRIMARY KEY (part_id),
    CONSTRAINT part_part_number_key UNIQUE (part_number)
);
CREATE TABLE IF NOT EXISTS app.part_category
(
    delete_flag integer DEFAULT 0,
    category_id bigint NOT NULL,
    created_by bigint,
    created_date_time timestamp(6) without time zone,
    updated_by bigint,
    updated_date_time timestamp(6) without time zone,
    name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT part_category_pkey PRIMARY KEY (category_id),
    CONSTRAINT part_category_name_key UNIQUE (name)
);
CREATE TABLE IF NOT EXISTS app.cost_factor
(
    delete_flag integer DEFAULT 0,
    created_by bigint,
    created_date_time timestamp(6) without time zone,
    factor_id bigint NOT NULL,
    updated_by bigint,
    updated_date_time timestamp(6) without time zone,
    factor_name character varying(50) COLLATE pg_catalog."default",
    CONSTRAINT cost_factor_pkey PRIMARY KEY (factor_id)
);
CREATE TABLE IF NOT EXISTS app.part_cost
(
    delete_flag integer DEFAULT 0,
    created_by bigint,
    created_date_time timestamp(6) without time zone,
    part_cost_id bigint NOT NULL,
    part_id bigint,
    updated_by bigint,
    updated_date_time timestamp(6) without time zone,
    vendor_id bigint,
    CONSTRAINT part_cost_pkey PRIMARY KEY (part_cost_id),
    CONSTRAINT part_cost_vendor_id_fkey FOREIGN KEY (vendor_id)
        REFERENCES app.vendor (vendor_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT part_cost_part_id_fkey FOREIGN KEY (part_id)
        REFERENCES app.part (part_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
CREATE TABLE IF NOT EXISTS app.part_cost_cost_factor
(
    delete_flag integer DEFAULT 0,
    value double precision NOT NULL,
    created_by bigint,
    created_date_time timestamp(6) without time zone,
    factor_id bigint,
    part_cost_cost_factor_id bigint NOT NULL,
    part_cost_id bigint,
    updated_by bigint,
    updated_date_time timestamp(6) without time zone,
    CONSTRAINT part_cost_cost_factor_pkey PRIMARY KEY (part_cost_cost_factor_id),
    CONSTRAINT part_cost_cost_factor_part_cost_id_fkey FOREIGN KEY (part_cost_id)
        REFERENCES app.part_cost (part_cost_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT part_cost_cost_factor_factor_id_fkey FOREIGN KEY (factor_id)
        REFERENCES app.cost_factor (factor_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
CREATE TABLE IF NOT EXISTS app.bom
(
    delete_flag integer DEFAULT 0,
    quantity double precision,
    created_by bigint,
    created_date_time timestamp(6) without time zone,
    parent_part_id bigint NOT NULL,
    part_id bigint NOT NULL,
    updated_by bigint,
    updated_date_time timestamp(6) without time zone,
    CONSTRAINT bom_pkey PRIMARY KEY (parent_part_id, part_id),
    CONSTRAINT bom_parent_part_id_key UNIQUE (parent_part_id),
    CONSTRAINT bom_part_id_key UNIQUE (part_id),
    CONSTRAINT bom_parent_part_id_fkey FOREIGN KEY (parent_part_id)
        REFERENCES app.part (part_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT bom_part_id_fkey FOREIGN KEY (part_id)
        REFERENCES app.part (part_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

/* liquibase rollback
DROP TABLE app.app_user CASCADE;
DROP TABLE app.vendor CASCADE;
DROP TABLE app.part CASCADE;
DROP TABLE app.part_category CASCADE;
DROP TABLE app.cost_factor CASCADE;
DROP TABLE app.part_cost CASCADE;
DROP TABLE app.part_cost_cost_factor CASCADE;
DROP TABLE app.bom CASCADE;
*/

--changeset Rishabh:20241228-4
INSERT INTO app.vendor(
	delete_flag, created_by, created_date_time, updated_by, updated_date_time, vendor_id, address, contact_number, email_id, name)
	VALUES (0, -9, now(), -9, now(), 1, 'XYZ Company, Patna, Bihar', '9999999999', 'xyz@gmail.com', 'XYZ Company');
INSERT INTO app.vendor(
	delete_flag, created_by, created_date_time, updated_by, updated_date_time, vendor_id, address, contact_number, email_id, name)
	VALUES (0, -9, now(), -9, now(), 2, 'ABC Company, Patna, Bihar', '9999999999', 'abc@gmail.com', 'ABC Company');
--rollback TRUNCATE app.vendor CASCADE

--changeset Rishabh:20241228-5
INSERT INTO app.cost_factor(
	delete_flag, created_by, created_date_time, factor_id, updated_by, updated_date_time, factor_name)
	VALUES (0, -9, now(), 1, -9, now(), 'Labor Cost');
INSERT INTO app.cost_factor(
	delete_flag, created_by, created_date_time, factor_id, updated_by, updated_date_time, factor_name)
	VALUES (0, -9, now(), 2, -9, now(), 'Cost Price');
--rollback TRUNCATE app.cost_factor CASCADE

