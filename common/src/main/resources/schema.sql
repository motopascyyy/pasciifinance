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