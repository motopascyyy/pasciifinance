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