create user 'phone_surv_queryusr'@'localhost' identified by 'Cornbread0-Phony2-Mobilize3-Crunching6';
grant select, insert, update, delete on phone_surv.* to 'phone_surv_queryusr'@'localhost';
flush privileges;
