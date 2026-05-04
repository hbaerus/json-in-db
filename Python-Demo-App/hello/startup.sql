-- dropping appdb user
drop user appdb cascade;

-- creating appdb...

-- creating tablespace
CREATE TABLESPACE APPDB DATAFILE 'appdb.dbf' SIZE 1G reuse AUTOEXTEND ON nologging;

-- creating user
CREATE USER appdb IDENTIFIED BY "&APPDB_PASSWORD"
	DEFAULT TABLESPACE APPDB
	QUOTA UNLIMITED ON APPDB;

-- assiging privileges
grant CREATE SESSION to appdb;
grant CREATE PROCEDURE to appdb;
grant CREATE SEQUENCE to appdb;
grant CREATE TABLE to appdb;

-- Creates the user table with three columns; user id number, username and password
CREATE TABLE "APPDB"."user" 
   (	"USER_ID" NUMBER(*,0) NOT NULL ENABLE, 
	"USERNAME" VARCHAR2(50 CHAR) NOT NULL ENABLE, 
	"PASSWORD" VARCHAR2(255 CHAR) NOT NULL ENABLE,
	 PRIMARY KEY ("USER_ID")
   );

-- Creates the USER_PROFILE table with two columns; comment id number and their profile stored as JSON
CREATE TABLE "APPDB"."USER_PROFILE" 
   (	"COMMENT_ID" NUMBER(*,0) NOT NULL ENABLE, 
	"DOC" CLOB, 
	 CONSTRAINT "ENSURE_JSON" CHECK (DOC IS JSON) ENABLE, 
	 PRIMARY KEY ("COMMENT_ID")
   );

-- Creates a JSON Search Index
CREATE INDEX po_search_idx ON user_profile (doc)
INDEXTYPE IS CTXSYS.CONTEXT
PARAMETERS ('section group CTXSYS.JSON_SECTION_GROUP SYNC (ON COMMIT)');

-- How to add in users
truncate table "user";
insert into "user" (user_id, username, password) values (1, 'john', 'pbkdf2_sha256$200000$anNvbi1pbi1kYi1qb2hu$aRrEApT0WiH4tXHzY2yCwIM4p5u2E3x0x5r/bzBsQ/E=');
insert into "user" (user_id, username, password) values (2, 'jane', 'pbkdf2_sha256$200000$anNvbi1pbi1kYi1qYW5l$C4xAyhBs2I181EFv5U9c2XtKGhXTRReqOmjORWKrT1s=');
insert into "user" (user_id, username, password) values (3, 'jason', 'pbkdf2_sha256$200000$anNvbi1pbi1kYi1qYXNvbg==$ZXUL9JJjwf/KhPY4SaHEzuSyrntOF1hrzSaNlNMysFM=');
insert into "user" (user_id, username, password) values (4, 'jill', 'pbkdf2_sha256$200000$anNvbi1pbi1kYi1qaWxs$V2CauZ2/c3XEXU1RxYZm9VH0dNbhpGLyYCcnci4r6NE=');
insert into "user" (user_id, username, password) values (5, 'tiffany', 'pbkdf2_sha256$200000$anNvbi1pbi1kYi10aWZmYW55$gdjcHfycZ9uTAy99hvGDsNM/HRFfl/5orSyBG94MypA=');
insert into "user" (user_id, username, password) values (6, 'thomas', 'pbkdf2_sha256$200000$anNvbi1pbi1kYi10aG9tYXM=$R1rklrdPFJzHES17NFG/pBbw+f88QrjW3pLjRau2ZGY=');
insert into "user" (user_id, username, password) values (7, 'monique', 'pbkdf2_sha256$200000$anNvbi1pbi1kYi1tb25pcXVl$EmjM345UJhDiHdAF67TpbH6k2LKtMTVVLSQUBC7fUw4=');
insert into "user" (user_id, username, password) values (8, 'megan', 'pbkdf2_sha256$200000$anNvbi1pbi1kYi1tZWdhbg==$OVqEvfyHoJLqb+KJgNwHkd9EvG5pYGwngDBSCPBfI90=');
insert into "user" (user_id, username, password) values (9, 'patrick', 'pbkdf2_sha256$200000$anNvbi1pbi1kYi1wYXRyaWNr$zWTnZN1Rck+fl3vjHj0zlZGfN7huk4eFOReJVFOAg0Q=');
insert into "user" (user_id, username, password) values (10, 'jordan', 'pbkdf2_sha256$200000$anNvbi1pbi1kYi1qb3JkYW4=$/qVzmIggGT+grdtSH5d3Dss9AY7HDvpd8XWiMNlQgc8=');
commit;

-- How to add a profile for a user
truncate table user_profile;
insert into user_profile (comment_id, doc) values (1, '{"Profname":"John", "username":"john", "age":29, "picture":"http://i58.tinypic.com/25kmzwz.png", "about":"I enjoy fishing and hanging out with my dog", "email":"john@email.com", "phone":"123-456-7890", "loc":"San Francisco", "groupd":"Database Security", "empid":"123456", "school":"MIT", "gradYear":"2008", "involv":"Rugby, baseball, Kappa Sigma"}'); 
insert into user_profile (comment_id, doc) values (2, '{"Profname":"Jane", "username":"jane", "age":28, "picture": "http://i61.tinypic.com/2e0v9dy.png","about":"I enjoy video games and eating ramen", "email":"jane@email.com", "phone":"123-456-7890", "loc":"San Francisco", "groupd":"Database Security", "empid":"123456", "school":"UC Santa Cruz", "gradYear":"1997", "involv":"Captain of the chess team"}'); 
insert into user_profile (comment_id, doc) values (3, '{"Profname":"Jason", "username":"jason", "age":22, "picture":"http://i58.tinypic.com/25kmzwz.png", "about":"Wanna code with me?", "email":"jason@email.com", "phone":"123-456-7890", "loc":"San Francisco", "groupd":"Database Security", "empid":"123456", "school":"Stanford", "gradYear":"2008", "involv":"Computer science TA"}'); 
insert into user_profile (comment_id, doc) values (4, '{"Profname":"Jill", "username":"jill", "age":27, "picture": "http://i61.tinypic.com/2e0v9dy.png","about":"Nordstrom is where you will find me.", "email":"jill@email.com", "phone":"123-456-7890", "loc":"San Francisco", "groupd":"Database Security", "empid":"123456", "school":"Yale", "gradYear":"2003", "involv":"Double major in computer science and fashion design"}'); 
insert into user_profile (comment_id, doc) values (5, '{"Profname":"Tiffany", "username":"tiffany", "age":23, "picture": "http://i61.tinypic.com/2e0v9dy.png","about":"Major foodie and avid vegan", "email":"tiffany@email.com", "phone":"123-456-7890", "loc":"San Francisco", "groupd":"Database Security", "empid":"123456", "school":"UCSF", "gradYear":"2010", "involv":"Resident chef"}'); 
insert into user_profile (comment_id, doc) values (6, '{"Profname":"Thomas", "username":"thomas", "age":22, "picture":"http://i58.tinypic.com/25kmzwz.png", "about":"I like music festivals and mixing beats", "email":"thomas@email.com", "phone":"123-456-7890", "loc":"San Francisco", "groupd":"Database Security", "empid":"123456", "school":"Stanford", "gradYear":"2014", "involv":"Stanford Concert Network"}'); 
insert into user_profile (comment_id, doc) values (7, '{"Profname":"Monique", "username":"monique", "age":28, "picture": "http://i61.tinypic.com/2e0v9dy.png","about":"PhD in Computer Science, Masters in Mechanical Engineering", "email":"monique@email.com", "phone":"123-456-7890", "loc":"San Francisco", "groupd":"Database Security", "empid":"123456", "school":"MIT", "gradYear":"2013", "involv":"Founded my own start-up in 10th grade which sold to Google for $20M"}'); 
insert into user_profile (comment_id, doc) values (8, '{"Profname":"Megan", "username":"megan", "age":25, "picture": "http://i61.tinypic.com/2e0v9dy.png","about":"I really just like to sleep", "email":"megan@email.com", "phone":"123-456-7890", "loc":"San Francisco", "groupd":"Database Security", "empid":"123456", "school":"UCSB", "gradYear":"2011", "involv":"Local barista"}'); 
insert into user_profile (comment_id, doc) values (9, '{"Profname":"Patrick", "username":"patrick", "age":22, "picture":"http://i58.tinypic.com/25kmzwz.png", "about":"Cat person all the way", "email":"patrick@email.com", "phone":"123-456-7890", "loc":"San Francisco", "groupd":"Database Security", "empid":"123456", "school":"UPenn", "gradYear":"2013", "involv":"Alpha delt literary society"}'); 
insert into user_profile (comment_id, doc) values (10, '{"Profname":"Jordan", "username":"jordan", "age":26, "picture":"http://i58.tinypic.com/25kmzwz.png", "about":"I spend my weekends sailing on my yacht", "email":"john@email.com", "phone":"123-456-7890", "loc":"San Francisco", "groupd":"Database Security", "empid":"123456", "school":"Harvard", "gradYear":"2008", "involv":"Student Body President"}');
commit;

