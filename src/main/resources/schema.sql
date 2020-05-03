-- new addition with 2.2.0-SNAPSHOT
CREATE TABLE KEYSTORE_LOCK
(
    ID     NUMBER(19, 0) NOT NULL,
    LOCKED NUMBER(19, 0) NOT NULL,
    CONSTRAINT KEYSTORE_LOCK_PK PRIMARY KEY (ID)
);

INSERT INTO KEYSTORE_LOCK (ID, LOCKED)
VALUES (1, 1);

-- Original
CREATE SEQUENCE KEYSTORE_ID_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE KEYSTORE
(
    ID      NUMBER(19, 0) NOT NULL,
    RSAKEY  VARCHAR(2000) NOT NULL,
    EXPIRES TIMESTAMP     NOT NULL,
    CONSTRAINT KEYSTORE_PK PRIMARY KEY (ID)
);

DROP TABLE KEYSTORE;

DROP SEQUENCE KEYSTORE_ID_SEQ;