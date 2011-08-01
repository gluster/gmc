-- Create users
insert into users(username, password, enabled) values ('gluster','syst3m',1);

-- Assign authorities to users (to be removed after implementing user group functionality)
insert into authorities(username,authority) values ('gluster','ROLE_USER');
insert into authorities(username,authority) values ('gluster','ROLE_ADMIN');

-- Create user groups
insert into groups(group_name) values ('Users');
insert into groups(group_name) values ('Administrators');

-- Add authorities to groups (functionality not yet implemented in code)
insert into group_authorities(group_id, authority) select id,'ROLE_USER' from groups where group_name='Users';
insert into group_authorities(group_id, authority) select id,'ROLE_USER' from groups where group_name='Administrators'; 
insert into group_authorities(group_id, authority) select id,'ROLE_ADMIN' from groups where group_name='Administrators';

-- Assign group members
insert into group_members(group_id, username) select id,'gluster' from groups where group_name='Administrators';