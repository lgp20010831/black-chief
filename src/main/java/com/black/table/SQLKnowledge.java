package com.black.table;

final class SQLKnowledge {

    //触发器
    /*
        //创建函数, 以 ayc 表 id 为参数去删除 byc 和 cyc 表里的数据
        create or replace FUNCTION delete_ayc_all()
        returns TRIGGER as $$
        BEGIN
        delete from byc where a1_id = old.id;
        delete from cyc where a1_id = old.id;
        return old;
        end;
        $$
        LANGUAGE plpgsql;

        //如果存在此触发器则删除
        drop TRIGGER if EXISTS delete_ayc_all on ayc;

        //创建触发器, 监听删除事件
        create TRIGGER  delete_ayc_all
        AFTER delete on ayc
        for each row execute PROCEDURE delete_ayc_all();

        //尝试删除
        delete from ayc where id = '128'

        //语句触发器
        每执行一个 sql 语句触发一次
        create trigger.sql {触发器名称}
        after [insert or update or delete] on {表名}
        for statement execute procedure [函数名称]

        //行级触发器
         create trigger.sql {触发器名称}
        after [insert or update or delete] on {表名}
        for row execute procedure [函数名称]

        //before 触发器
        create trigger.sql {触发器名称}
        before [insert or update or delete] on {表名}
        for [操作类型] execute procedure [函数名称]


        before 可以修改 NEW 的值
        after 可以利用 old 值
        对于语句级触发器来说,返回值应该为 null
        对于 befoer 和 insertd of 这类行级触发器如果返回 null, 则表示
        忽略对当前行的操作, 如果返回非 null 则成为即将被插入和更新的行
        对于 after 类触发器,返回值将被忽略
     */

}
