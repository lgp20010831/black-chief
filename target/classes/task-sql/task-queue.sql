create table if not exists "task_queue"(
    "taskId" varchar(255),
    "size" int,
    "currentIndex" integer,
    "key" varchar(255),
    "jsonString" varchar(1000),
    "startTime" varchar(50),
    "updateTime" varchar(50),
    "alias" varchar(50),
    "serverStopTime" bigint,
    "futureStart" bigint,
    "life" bigint
);

create table if not exists "task_instance_history"(
  "task_id" varchar(255),
  "size" int,
  "index" int,
  "key" varchar(255),
  "param" varchar(1000),
  "start_time" varchar(50),
  "end_time" varchar(50),
  "alias" varchar(50),
  "result" varchar(50)
);