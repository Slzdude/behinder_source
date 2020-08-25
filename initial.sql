create table catagory
(
    id      INTEGER
        primary key autoincrement,
    name    varchar(255),
    comment varchar(255)
);

create table hosts
(
    id      INTEGER
        primary key autoincrement,
    shellid int,
    ip      varchar(255),
    os      varchar(255),
    status  varchar(255),
    comment varchar(255)
);

create table plugins
(
    id         INTEGER
        primary key autoincrement,
    name       varchar(255),
    version    varchar(255),
    entryFile  varchar(255),
    scriptType varchar(20),
    type       varchar(255),
    isGetShell int,
    icon       varchar(255),
    author     varchar(255),
    link       varchar(255),
    qrcode     varchar(255),
    status     int,
    comment    varchar(255)
);

create table proxys
(
    id       INTEGER
        primary key autoincrement,
    name     varchar(255),
    type     varchar(20),
    ip       varchar(255),
    port     int,
    username varchar(255) default '',
    password varchar(255) default '',
    status   int
);

create table services
(
    id      INTEGER
        primary key autoincrement,
    hostid  int,
    port    varchar(6),
    name    varchar(255),
    banner  text default '',
    status  varchar(255),
    comment varchar(255)
);

create table shells
(
    id         INTEGER
        primary key autoincrement,
    url        text not null,
    ip         varchar(15),
    password   varchar(255),
    type       varchar(20),
    os         varchar(255) default '',
    comment    text         default '',
    memo       text         default '',
    addtime    TIMESTAMP,
    updatetime TIMESTAMP,
    accesstime TIMESTAMP,
    headers    text,
    catagory   varchar(255)
);
