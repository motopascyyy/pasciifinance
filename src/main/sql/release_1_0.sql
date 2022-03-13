alter table ACCOUNT_ENTRY alter column CAD_FIXED_INCOME_PCT DECIMAL(6, 5);
alter table ACCOUNT_ENTRY alter column CANADIAN_EQT_PCT        DECIMAL(6, 5);
alter table ACCOUNT_ENTRY alter column CASH_PCT                DECIMAL(6, 5);
alter table ACCOUNT_ENTRY alter column EMERGING_MKTS_EQT_PCT   DECIMAL(6, 5);
alter table ACCOUNT_ENTRY alter column GLOBAL_FIXED_INCOME_PCT DECIMAL(6, 5);
alter table ACCOUNT_ENTRY alter column INTERNATIONAL_EQT_PCT   DECIMAL(6, 5);
alter table ACCOUNT_ENTRY alter column OTHER_PCT               DECIMAL(6, 5);
alter table ACCOUNT_ENTRY alter column US_EQT_PCT              DECIMAL(6, 5);


create view SUMMARIZE_ACCOUNT_ENTRY_BY_MONTH as
    select
        ENTRY_ID,
        e_date as entryDate,
        sum(coalesce(BOOK_VALUE, PREVIOUS_BOOK_VALUE)) as bookValue,
        sum(coalesce(MARKET_VALUE, PREVIOUS_MARKET_VALUE)) as marketValue,
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