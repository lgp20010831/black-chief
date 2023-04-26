create or replace FUNCTION delete_ayc_all()
    returns TRIGGER as $$
BEGIN
    delete from byc where a1_id = old.id;
    delete from cyc where a1_id = old.id;
    return old;
end;
$$
    LANGUAGE plpgsql;
-->
drop TRIGGER if EXISTS delete_ayc_all on ayc;

create TRIGGER  delete_ayc_all
    AFTER delete on ayc
    for each row execute PROCEDURE delete_ayc_all();