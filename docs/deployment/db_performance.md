---
layout: default
title: DB Performance
category: deployment
order: 13
---

# Performance tips for PostgreSQL and PostGIS

If your database grows large, you will probably notice that things start to become
slower. This page has some general hints on how to make your database faster.

## Finding problem queries

The start of making thing faster is figuring out exactly what is slow. To help with
that, FROST has the option to generate a log of all queries that take too much time
to execute:

* **persistence.slowQueryThreshold:**  
  The duration threshold in ms after which queries are considered slow and are logged. Default 200, set to 0 to disable.

Any database query that takes more than the set amount of time will be written to
the logs. Take one such a query, add `explain analyze` in front of it, and execute
it on your database.

A tutorial on how to read `explain analyze` output can be found on
[postgresqltutorial.com](https://www.postgresqltutorial.com/postgresql-explain/)

## Adding Indices

By default, only primary and foreign keys have indices on them. A very common index
is for Datastreams(x)/observations?$orderby=phenomenonTime asc:

```sql
create index "OBS-DS_ID-PHTIME_SE-O_ID"
  on "OBSERVATIONS"
  using btree
  ("DATASTREAM_ID", "PHENOMENON_TIME_START" asc, "PHENOMENON_TIME_END" asc);
```

You can also add indices to geometry columns using the PostGIS `GIST(column)` function.
See [Spatial Indexing](https://postgis.net/workshops/postgis-intro/indexing.html)
in the PostGIS manual.

```sql
create index "LOCATIONS_GEOM"
  on "LOCATION"
  using gist("GEOM");
```



