-- spring-batch-core.jar 에서 org.springframework.batch.core package의 schema-drop-h2.sql 참조

DROP TABLE  BATCH_STEP_EXECUTION_CONTEXT IF EXISTS;
DROP TABLE  BATCH_JOB_EXECUTION_CONTEXT IF EXISTS;
DROP TABLE  BATCH_STEP_EXECUTION IF EXISTS;
DROP TABLE  BATCH_JOB_EXECUTION_PARAMS IF EXISTS;
DROP TABLE  BATCH_JOB_EXECUTION IF EXISTS;
DROP TABLE  BATCH_JOB_INSTANCE IF EXISTS;

DROP SEQUENCE  BATCH_STEP_EXECUTION_SEQ IF EXISTS;
DROP SEQUENCE  BATCH_JOB_EXECUTION_SEQ IF EXISTS;
DROP SEQUENCE  BATCH_JOB_SEQ IF EXISTS;

-- spring-batch-core.jar 에서 org.springframework.batch.core package의 schema-h2.sql 참조

CREATE TABLE BATCH_JOB_INSTANCE  (
	JOB_INSTANCE_ID BIGINT IDENTITY NOT NULL PRIMARY KEY ,
	VERSION BIGINT ,
	JOB_NAME VARCHAR(100) NOT NULL,
	JOB_KEY VARCHAR(32) NOT NULL,
	constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ;

CREATE TABLE BATCH_JOB_EXECUTION  (
	JOB_EXECUTION_ID BIGINT IDENTITY NOT NULL PRIMARY KEY ,
	VERSION BIGINT  ,
	JOB_INSTANCE_ID BIGINT NOT NULL,
	CREATE_TIME TIMESTAMP NOT NULL,
	START_TIME TIMESTAMP DEFAULT NULL ,
	END_TIME TIMESTAMP DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED TIMESTAMP,
	JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL,
	constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
	JOB_EXECUTION_ID BIGINT NOT NULL ,
	TYPE_CD VARCHAR(6) NOT NULL ,
	KEY_NAME VARCHAR(100) NOT NULL ,
	STRING_VAL VARCHAR(250) ,
	DATE_VAL TIMESTAMP DEFAULT NULL ,
	LONG_VAL BIGINT ,
	DOUBLE_VAL DOUBLE PRECISION ,
	IDENTIFYING CHAR(1) NOT NULL ,
	constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION  (
	STEP_EXECUTION_ID BIGINT IDENTITY NOT NULL PRIMARY KEY ,
	VERSION BIGINT NOT NULL,
	STEP_NAME VARCHAR(100) NOT NULL,
	JOB_EXECUTION_ID BIGINT NOT NULL,
	START_TIME TIMESTAMP NOT NULL ,
	END_TIME TIMESTAMP DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	COMMIT_COUNT BIGINT ,
	READ_COUNT BIGINT ,
	FILTER_COUNT BIGINT ,
	WRITE_COUNT BIGINT ,
	READ_SKIP_COUNT BIGINT ,
	WRITE_SKIP_COUNT BIGINT ,
	PROCESS_SKIP_COUNT BIGINT ,
	ROLLBACK_COUNT BIGINT ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED TIMESTAMP,
	constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
	STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT LONGVARCHAR ,
	constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
	references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
	JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT LONGVARCHAR ,
	constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ;
CREATE SEQUENCE BATCH_JOB_SEQ;

DROP TABLE SHOPPING_ITEM IF EXISTS;
DROP TABLE SHOPPING_CART IF EXISTS;
DROP TABLE PRODUCT IF EXISTS;
DROP TABLE USER IF EXISTS;

DROP SEQUENCE USER_IDX_SEQUENCE IF EXISTS ;
DROP SEQUENCE PRODUCT_IDX_SEQUENCE IF EXISTS ;
DROP SEQUENCE SHOPPING_ITEM_IDX_SEQUENCE IF EXISTS ;
DROP SEQUENCE SHOPPING_CART_IDX_SEQUENCE IF EXISTS;

CREATE SEQUENCE USER_IDX_SEQUENCE;
CREATE SEQUENCE PRODUCT_IDX_SEQUENCE;
CREATE SEQUENCE SHOPPING_ITEM_IDX_SEQUENCE;
CREATE SEQUENCE SHOPPING_CART_IDX_SEQUENCE;

CREATE TABLE USER(
    IDX BIGINT NOT NULL PRIMARY KEY,
    NAME VARCHAR(100) NOT NULL,
    LOGIN_ID VARCHAR(100) NOT NULL,
);

CREATE TABLE PRODUCT (
    IDX BIGINT NOT NULL PRIMARY KEY,
    PRODUCT_NAME VARCHAR NOT NULL,
    PRODUCT_PRICE INT NOT NULL DEFAULT 0
);

CREATE TABLE SHOPPING_CART(
    IDX BIGINT NOT NULL PRIMARY KEY,
    USER_IDX BIGINT NOT NULL,
    TOTAL_PRICE BIGINT NOT NULL,
    constraint FK_SHOPPiNG_CART_USER foreign key (USER_IDX) references USER(IDX)
);

CREATE TABLE SHOPPING_ITEM (
    IDX BIGINT NOT NULL PRIMARY KEY,
    PRODUCT_IDX BIGINT NOT NULL,
    SHOPPING_CART_IDX BIGINT NOT NULL,
    CNT INT,
    TOTAL_PRICE BIGINT,
    constraint FK_SHOPPING_ITEM_PRODUCT foreign key (PRODUCT_IDX) references PRODUCT(IDX),
    constraint FK_SHOPPING_ITEM_SHOPPING_CART foreign key (SHOPPING_CART_IDX) references SHOPPING_CART(IDX)
);



