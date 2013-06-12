/* temporarily connect to some existing database so that we can drop
and recreate aggbook
*/
\connect sportsdb
drop database if exists aggbook;
create database aggbook;
\connect aggbook

/* create your tables, indexes, etc. here */
create table USERS(
uID int not null,
uName varchar(50) not null,
email varchar(50) not null,
pWord varchar(50) not null,
about varchar(255) null,
fcount int not null);

create table RELATION(
friend1 int not null,
friend2 int not null
);

create table PENDING(
sender int not null,
recieve int not null
);

create table BOARD(
profile int not null,
poster int not null,
pname varchar(50) not null,
message varchar(255) not null,
datetime varchar(50) not null
);

/*insert into USERS values(1,'Test','test@test.com', '123','Do I work?'); 
insert into USERS values(2,'New Guy','new@new.com','zxc','Im new here');
insert into USERS values(3,'Dude', 'thedude@gmail.com', '123','Thats just like your opinion man');

insert into RELATION values(1,3);

insert into FCOUNT values(1,1);
insert into FCOUNT values(2,0);
insert into FCOUNT values(3,1);
*/

