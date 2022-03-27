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
    constraint UK5KQ42CN79NSD1LF4SEJXG644H
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
    constraint UKKVDLOO9N8BR04XKE2U7MSFCPI
        unique (ENTRY_DATE, ACCOUNT_ID),
    constraint FKHCCHQMVQ6VQBE3ECG303CR68U
        foreign key (ACCOUNT_ID) references ACCOUNT (ID)
);

create table ACCOUNT_ALLOCATION_ENTRY
(
    ID                    BIGINT not null
        primary key,
    CANADIAN_EQUITY       DECIMAL(19, 2),
    CANADIAN_FIXED_INCOME DECIMAL(19, 2),
    CASH_EQUIVALENT       DECIMAL(19, 2),
    ENTRY_DATE            TIMESTAMP,
    GLOBAL_FIXED_INCOME   DECIMAL(19, 2),
    INTERNATIONAL_EQUITY  DECIMAL(19, 2),
    OTHER_ASSETS          DECIMAL(19, 2),
    US_EQUITY             DECIMAL(19, 2),
    ACCOUNT_ID            BIGINT,
    constraint FKSFL6K36DBD1DWLSCMUB6W8ORL
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


create view LATEST_ACCOUNT_ENTRY as
(select
    max(id) as entry_id,
    account_id,
    to_char(entry_date, 'yyyy-mm-dd') as e_date
from
    account_entry
group by
    account_id,
    e_date);

CREATE VIEW LATEST_WEEKLY_ACCOUNT_ENTRY(ENTRY_ID, ACCOUNT_ID, E_DATE) AS
SELECT
    MAX(ID) AS ENTRY_ID,
    ACCOUNT_ID,
    TO_CHAR(ENTRY_DATE, 'yyyy-mm') AS E_DATE
FROM
    ACCOUNT_ENTRY
GROUP BY
    ACCOUNT_ID,
    TO_CHAR(ENTRY_DATE, 'yyyy-mm');

create view SUMMARIZED_ACCOUNT_ENTRY_BY_MONTH as
select
    ENTRY_ID,
    e_date as entry_date,
    sum(coalesce(BOOK_VALUE, PREVIOUS_BOOK_VALUE)) as book_value,
    sum(coalesce(MARKET_VALUE, PREVIOUS_MARKET_VALUE)) as market_value,
    acc_id
from
    (
        select
            dates.e_date,
            acc_id,
            entry_id,
            book_value,
            LAG(book_value) OVER (
             partition by dates.acc_id
             order by
                dates.e_date
          ) as previous_book_value,
                market_value,
            LAG(market_value) OVER (
             partition by dates.acc_id
             order by
                dates.e_date
          ) as previous_market_value
        from
            (
                select
                    distinct to_char(entry_date, 'yyyy-mm') as e_date,
                             accts.id as acc_id
                from
                    account_entry
                        cross join (
                        select
                            id
                        from
                            account
                    ) accts
            ) dates
                left join (
                select
                    lae.ACCOUNT_ID,
                    lae.E_DATE,
                    lae.ENTRY_ID,
                    CASE
                        WHEN JOINT_ACCOUNT = 'TRUE' THEN BOOK_VALUE / 2
                        ELSE BOOK_VALUE
                        END as BOOK_VALUE,
                    CASE
                        WHEN JOINT_ACCOUNT = 'TRUE' THEN MARKET_VALUE / 2
                        ELSE MARKET_VALUE
                        END as MARKET_VALUE
                from
                    LATEST_WEEKLY_ACCOUNT_ENTRY lae
                        inner join account_entry ae on lae.ENTRY_ID = ae.id
                        inner join account acc on ae.account_id = acc.id
            ) entries on dates.e_date = entries.e_date
                and dates.acc_id = entries.account_id
    )
group by
    acc_id, e_date
order by
    e_date asc;