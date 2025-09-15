drop database if exists phone_surv;

create database phone_surv;
use phone_surv;

create table telecamera (
    id_cam      int             auto_increment  primary key,
    nome        varchar(256)    not null,

    unique(nome)
);

create table intervallo (
    id_int      int             auto_increment  primary key,
    inizio      datetime        not null,
    fine        datetime        default null,
    id_cam      int             not null,

    check(fine>=inizio),

    constraint fk_int_cam foreign key (id_cam) references telecamera(id_cam) on update cascade on delete cascade
);

create table foto (
    id_foto     int             auto_increment  primary key,
    nomefile_f  varchar(512)    not null,
    id_int      int             not null,

    constraint fk_foto_int foreign key (id_int) references intervallo(id_int)  on update cascade on delete cascade
);

create table video (
    id_video    int             auto_increment primary key,
    nomefile_v  varchar(512)    not null,
    id_int      int             not null,

    constraint fk_vid_int foreign key (id_int) references intervallo(id_int)  on update cascade on delete cascade
);
