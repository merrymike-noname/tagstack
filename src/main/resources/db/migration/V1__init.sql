-- initial schema

-- bookmarks table
create table bookmarks (
    id int primary key generated by default as identity,
    user_id int not null,
    url TEXT not null,
    title TEXT,
    description TEXT,
    created_at timestamp default NOW(),
    updated_at timestamp default NOW()
);

-- tags table
create table tags (
    id int primary key generated by default as identity,
    user_id int not null,
    name varchar not null,
    parent_id int references tags(id) on delete cascade
);

-- bookmark_tags join table
create table bookmark_tags (
    bookmark_id int references bookmarks(id) on delete cascade,
    tag_id      int references tags(id) on delete cascade,
    primary key (bookmark_id, tag_id)
);

