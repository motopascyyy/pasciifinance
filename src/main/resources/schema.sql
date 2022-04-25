create sequence HIBERNATE_SEQUENCE;

-- create sequence BATCH_STEP_EXECUTION_SEQ;
--
-- create sequence BATCH_JOB_EXECUTION_SEQ;
--
-- create sequence BATCH_JOB_SEQ;

create table ACCOUNT
(
    ID                     BIGINT  not null
        primary key,
    ACCOUNT_LABEL          VARCHAR(255),
    ACCOUNT_TYPE           VARCHAR(255),
    ACTIVE                 BOOLEAN not null,
    INSTITUTION            VARCHAR(255),
    INSTITUTION_ACCOUNT_ID VARCHAR(255),
    JOINT_ACCOUNT          BOOLEAN not null,
    constraint UQ_Account_Inst_AccountLable
        unique (INSTITUTION, ACCOUNT_LABEL)
);

create table ACCOUNT_ENTRY
(
    ID                      BIGINT not null
        primary key,
    BOOK_VALUE              DECIMAL(19, 2),
    ENTRY_DATE              TIMESTAMP,
    MARKET_VALUE            DECIMAL(19, 2),
    ACCOUNT_ID              BIGINT,
    CAD_FIXED_INCOME_PCT    DECIMAL(6, 5),
    CANADIAN_EQT_PCT        DECIMAL(6, 5),
    CASH_PCT                DECIMAL(6, 5),
    EMERGING_MKTS_EQT_PCT   DECIMAL(6, 5),
    GLOBAL_FIXED_INCOME_PCT DECIMAL(6, 5),
    INTERNATIONAL_EQT_PCT   DECIMAL(6, 5),
    OTHER_PCT               DECIMAL(6, 5),
    US_EQT_PCT              DECIMAL(6, 5),
    constraint UQ_AccountEntry_EntryDate_AccountId
        unique (ENTRY_DATE, ACCOUNT_ID),
    constraint FK_AccountEntry_Account
        foreign key (ACCOUNT_ID) references ACCOUNT (ID)
);

create table SECURITY
(
    TICKER                  VARCHAR(255) not null
        primary key,
    CAD_FIXED_INCOME_PCT    DECIMAL(5, 4),
    CANADIAN_EQT_PCT        DECIMAL(5, 4),
    CASH_PCT                DECIMAL(5, 4),
    DESCRIPTION             VARCHAR(255),
    EMERGING_MKTS_EQT_PCT   DECIMAL(5, 4),
    GLOBAL_FIXED_INCOME_PCT DECIMAL(5, 4),
    INTERNATIONAL_EQT_PCT   DECIMAL(5, 4),
    OTHER_PCT               DECIMAL(5, 4),
    US_EQT_PCT              DECIMAL(5, 4)
);

create view LATEST_MONTHLY_ACCOUNT_ENTRY_VIEW as
select latest.entry_date, latest.ENTRY_ID, ae.ACCOUNT_ID,
       CASE
           WHEN JOINT_ACCOUNT = 'TRUE' THEN BOOK_VALUE / 2
           ELSE BOOK_VALUE
           END as BOOK_VALUE,
       CASE
           WHEN JOINT_ACCOUNT = 'TRUE' THEN MARKET_VALUE / 2
           ELSE MARKET_VALUE
           END as MARKET_VALUE
from (
         select distinct ACCOUNT_ID,
                         max(id) over (partition by cast(DATEADD(dd, -DAY(DATEADD(m, 1, entry_date)),
                                                                 DATEADD(m, 1, ENTRY_DATE)) as DATE), ACCOUNT_ID) as ENTRY_ID,
                         cast(DATEADD(dd, -DAY(DATEADD(m, 1, entry_date)),
                                      DATEADD(m, 1, ENTRY_DATE)) as DATE)                                         as entry_date
         from ACCOUNT_ENTRY
     ) as latest
         inner join ACCOUNT_ENTRY ae
                    on latest.ENTRY_ID = ae.ID
         inner join ACCOUNT A on A.ID = ae.ACCOUNT_ID
order by latest.entry_date asc, ae.ACCOUNT_ID asc;