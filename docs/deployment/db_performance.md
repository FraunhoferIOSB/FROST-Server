---
layout: default
title: DB Performance
category: deployment
order: 14
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


## Limiting problem queries

It is trivial to write a query that will not give a result in a reasonable time.
Therefore FROST has the option to limit query duration to a resonable time.
If a database query takes more time, it is stopped and an error is returned.
The option controlling how long this timeout is, is:

* **persistence.queryTimeout:**  
  The maximum duration, in seconds, that a query is allowed to take. Default 0 (no timeout). If
  your FROST instance is behind a reverse proxy that will abort the connection after a certain time, set this to the
  same duration.


## Adding Indices


### Indexing timestamps

By default, only primary and foreign keys have indices on them. A very common index
is for _Datastreams(x)/observations?$orderby=phenomenonTime asc_:

```sql
create index "OBS-DS_ID-PHTIME_SE-O_ID"
  on "OBSERVATIONS"
  using btree
  ("DATASTREAM_ID", "PHENOMENON_TIME_START" asc, "PHENOMENON_TIME_END" asc);
```


### Spatial indices

You can also add indices to geometry columns using the PostGIS `GIST(column)` function.
See [Spatial Indexing](https://postgis.net/workshops/postgis-intro/indexing.html)
in the PostGIS manual.

For the Locations table:
```sql
create index "LOCATIONS_GEOM"
  on "LOCATION"
  using gist ("GEOM");
```

For the FeaturesOfInterest table:
```sql
create index "FEATURES_GEOM"
  on "FEATURES"
  using gist ("GEOM");
```


### Indexing JSON fields

Indices can be added to fields within `jsonb`-type columns to speed up queries
like _Observations?$filter=parameters/secondary_id eq 123_:

```sql
create index "IDX_OBS_PARAM_SECONDARYID"
  on "OBSERVATIONS"
  using btree (("PARAMETERS" #> '{ secondary_id }') asc);
```


## Regenerating generated properties

If you ever need to re-generate the phenomenonTime properties of Datastreams,
you can use the SQL query:

```sql
update "DATASTREAMS" d
  SET "PHENOMENON_TIME_START" = 
        (select min("PHENOMENON_TIME_START")
          from "OBSERVATIONS" o
          where o."DATASTREAM_ID" = d."ID"
          group by o."DATASTREAM_ID"),
      "PHENOMENON_TIME_END" = 
        (select max("PHENOMENON_TIME_END")
          from "OBSERVATIONS" o
          where o."DATASTREAM_ID" = d."ID"
          group by o."DATASTREAM_ID")
;
```

If you ever need to re-generate the ObservedArea properties of Datastreams,
you can use the SQL query:

```sql
update "DATASTREAMS" d
  SET "OBSERVED_AREA" = 
      (select ST_ConvexHull(ST_Collect("GEOM"))
      from "FEATURES" f
      left join "OBSERVATIONS" o on o."FEATURE_ID" = f."ID"
      where o."DATASTREAM_ID" = d."ID"
      group by o."DATASTREAM_ID")
;
```
