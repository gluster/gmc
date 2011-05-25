create table cluster_info (
	id bigint generated by default as identity, 
	name varchar(255), 
	primary key (id));

create unique index ix_cluster_name on cluster_info (name);
	
create table server_info (
	id bigint generated by default as identity, 
	name varchar(255),
	cluster_id bigint,
	primary key (id));

create unique index ix_cluster_server on server_info (name, cluster_id);
	
alter table server_info add constraint FK_CLUSTER_ID foreign key (cluster_id) references cluster_info(id);