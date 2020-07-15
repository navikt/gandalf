CREATE TABLE RSAKEYS
(
    ID          bigint        not null,
    CURRENTKEY  varchar(2000) not null,
    EXPIRES     timestamp     not null,
    NEXTKEY     varchar(2000) not null,
    PREVIOUSKEY varchar(2000) not null,
    PRIMARY KEY (ID)
)
