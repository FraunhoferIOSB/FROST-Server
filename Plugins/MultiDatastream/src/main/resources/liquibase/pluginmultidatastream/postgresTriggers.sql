-- Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
-- Karlsruhe, Germany.
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Lesser General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Lesser General Public License for more details.
--
-- You should have received a copy of the GNU Lesser General Public License
-- along with this program.  If not, see <http://www.gnu.org/licenses/>.


-- ---------------------------------------
-- Function: multidatastreams_update_insert()
--
-- Updated fields are:
-- PHENOMENON_TIME_START,PHENOMENON_TIME_END,RESULT_TIME_START,RESULT_TIME_END and OBSERVED_AREA
-- ---------------------------------------
create or replace function multidatastreams_update_insert()
    returns trigger as
$BODY$
declare
    "MDS_ROW" RECORD;
    queryset TEXT := '';
    delimitr char(1) := ' ';
begin

if (NEW."MULTI_DATASTREAM_ID" is not null)
then
    select "ID","PHENOMENON_TIME_START","PHENOMENON_TIME_END","RESULT_TIME_START","RESULT_TIME_END","OBSERVED_AREA","LAST_FOI_ID"
        into "MDS_ROW" from "MULTI_DATASTREAMS" where "MULTI_DATASTREAMS"."ID"=NEW."MULTI_DATASTREAM_ID";
    if (NEW."PHENOMENON_TIME_START"<"MDS_ROW"."PHENOMENON_TIME_START" or "MDS_ROW"."PHENOMENON_TIME_START" is null) then
        queryset := queryset || delimitr || '"PHENOMENON_TIME_START" = $1."PHENOMENON_TIME_START"';
        delimitr := ',';
    end if;
    if (coalesce(NEW."PHENOMENON_TIME_END", NEW."PHENOMENON_TIME_START") > "MDS_ROW"."PHENOMENON_TIME_END" or "MDS_ROW"."PHENOMENON_TIME_END" is null) then
        queryset := queryset || delimitr || '"PHENOMENON_TIME_END" = coalesce($1."PHENOMENON_TIME_END", $1."PHENOMENON_TIME_START")';
        delimitr := ',';
    end if;

    if (NEW."RESULT_TIME" is not null) then
        if (NEW."RESULT_TIME"<"MDS_ROW"."RESULT_TIME_START" or "MDS_ROW"."RESULT_TIME_START" is null) then
            queryset := queryset || delimitr || '"RESULT_TIME_START" = $1."RESULT_TIME"';
            delimitr := ',';
        end if;
        if (NEW."RESULT_TIME" > "MDS_ROW"."RESULT_TIME_END" or "MDS_ROW"."RESULT_TIME_END" is null) then
            queryset := queryset || delimitr || '"RESULT_TIME_END" = $1."RESULT_TIME"';
            delimitr := ',';
        end if;
    end if;

    if ("MDS_ROW"."LAST_FOI_ID" is null or "MDS_ROW"."LAST_FOI_ID" != NEW."FEATURE_ID") then
        queryset := queryset || delimitr || '"LAST_FOI_ID" = $1."FEATURE_ID"';
        queryset := queryset || ',"OBSERVED_AREA" = ST_ConvexHull(ST_Collect("OBSERVED_AREA", (select "GEOM" from "FEATURES" where "ID"=$1."FEATURE_ID")))';
        delimitr := ',';
    end if;
    if (delimitr = ',') then
        EXECUTE 'update "MULTI_DATASTREAMS" SET ' || queryset ||  ' where "MULTI_DATASTREAMS"."ID"=$1."MULTI_DATASTREAM_ID"' using NEW;
    end if;
    return new;
end if;

return new;
END
$BODY$
    language plpgsql volatile
    cost 100;



do $$ begin
    create trigger multidatastreams_actualization_insert
        after insert
        on "OBSERVATIONS"
        for each row
        execute procedure multidatastreams_update_insert();
exception
    when others then null;
end $$;




-- ---------------------------------------
-- Function: multidatastreams_update_update()
--
-- This function also updates multidatastreams. Updated fields are:
-- PHENOMENON_TIME_START,PHENOMENON_TIME_END,RESULT_TIME_START,RESULT_TIME_END.
-- Warning: OBSERVED_AREA not taken into account.
-- ---------------------------------------
create or replace function multidatastreams_update_update()
    returns trigger as
$BODY$
declare
    "MDS_ROW" "MULTI_DATASTREAMS"%rowtype;
    queryset TEXT := '';
    delimitr char(1) := ' ';
begin

if (NEW."MULTI_DATASTREAM_ID" is not null)
then
    if (NEW."PHENOMENON_TIME_START" != OLD."PHENOMENON_TIME_START" or NEW."PHENOMENON_TIME_END" != OLD."PHENOMENON_TIME_END") then
        select * into "MDS_ROW" from "MULTI_DATASTREAMS" where "MULTI_DATASTREAMS"."ID"=NEW."MULTI_DATASTREAM_ID";
        
        if (NEW."PHENOMENON_TIME_START"<"MDS_ROW"."PHENOMENON_TIME_START") then
            queryset := queryset || delimitr || '"PHENOMENON_TIME_START" = $1."PHENOMENON_TIME_START"';
            delimitr := ',';
        elseif (OLD."PHENOMENON_TIME_START" = "MDS_ROW"."PHENOMENON_TIME_START") then
            queryset := queryset || delimitr || '"PHENOMENON_TIME_START" = (select min("PHENOMENON_TIME_START") from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = $1."MULTI_DATASTREAM_ID")';
            delimitr := ',';
        end if;
        if (coalesce(NEW."PHENOMENON_TIME_END", NEW."PHENOMENON_TIME_START") > "MDS_ROW"."PHENOMENON_TIME_END") then
            queryset := queryset || delimitr || '"PHENOMENON_TIME_END" = coalesce($1."PHENOMENON_TIME_END", $1."PHENOMENON_TIME_START")';
            delimitr := ',';
        elseif (coalesce(OLD."PHENOMENON_TIME_END", OLD."PHENOMENON_TIME_START") = "MDS_ROW"."PHENOMENON_TIME_END") then
            queryset := queryset || delimitr || '"PHENOMENON_TIME_END" = (select max(coalesce("PHENOMENON_TIME_END", "PHENOMENON_TIME_START")) from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = $1."MULTI_DATASTREAM_ID")';
            delimitr := ',';
        end if;
    end if;


    if (NEW."RESULT_TIME" != OLD."RESULT_TIME") then
        select * into "MDS_ROW" from "MULTI_DATASTREAMS" where "MULTI_DATASTREAMS"."ID"=NEW."MULTI_DATASTREAM_ID";
        
        if (NEW."RESULT_TIME" < "MDS_ROW"."RESULT_TIME_START") then
            queryset := queryset || delimitr || '"RESULT_TIME_START" = $1."RESULT_TIME"';
            delimitr := ',';
        elseif (OLD."RESULT_TIME" = "MDS_ROW"."RESULT_TIME_START") then
            queryset := queryset || delimitr || '"RESULT_TIME_START" = (select min("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = $1."MULTI_DATASTREAM_ID")';
            delimitr := ',';
        end if;
        if (NEW."RESULT_TIME" > "MDS_ROW"."RESULT_TIME_END") then
            queryset := queryset || delimitr || '"RESULT_TIME_END" = $1."RESULT_TIME"';
            delimitr := ',';
        elseif (OLD."RESULT_TIME" = "MDS_ROW"."RESULT_TIME_END") then
            queryset := queryset || delimitr || '"RESULT_TIME_END" = (select max("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = $1."MULTI_DATASTREAM_ID")';
            delimitr := ',';
        end if;
    end if;
    if (delimitr = ',') then
        EXECUTE 'update "MULTI_DATASTREAMS" SET ' || queryset ||  ' where "MULTI_DATASTREAMS"."ID"=$1."MULTI_DATASTREAM_ID"' using NEW;
    end if;
end if;


return new;
END
$BODY$
    language plpgsql volatile
    cost 100;


do $$ begin
    create trigger multidatastreams_actualization_update
        after update
        on "OBSERVATIONS"
        for each row
        execute procedure multidatastreams_update_update();
exception
    when others then null;
end $$;



-- ---------------------------------------
-- Function: multidatastreams_update_delete()
--
-- Updated fields are:
-- PHENOMENON_TIME_START,PHENOMENON_TIME_END,RESULT_TIME_START,RESULT_TIME_END.
-- Warning: OBSERVED_AREA not taken into account.
-- ---------------------------------------
create or replace function multidatastreams_update_delete()
    returns trigger as
$BODY$
declare
    "MDS_ROW" "MULTI_DATASTREAMS"%rowtype;
    queryset TEXT := '';
    delimitr char(1) := ' ';
begin

if (OLD."MULTI_DATASTREAM_ID" is not null)
then
    select * into "MDS_ROW" from "MULTI_DATASTREAMS" where "MULTI_DATASTREAMS"."ID"=OLD."MULTI_DATASTREAM_ID";

    if (OLD."PHENOMENON_TIME_START" = "MDS_ROW"."PHENOMENON_TIME_START"
        or coalesce(OLD."PHENOMENON_TIME_END", OLD."PHENOMENON_TIME_START") = "MDS_ROW"."PHENOMENON_TIME_END")
    then
        queryset := queryset || delimitr || '"PHENOMENON_TIME_START" = (select min("PHENOMENON_TIME_START") from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = $1."MULTI_DATASTREAM_ID")';
        delimitr := ',';
        queryset := queryset || delimitr || '"PHENOMENON_TIME_END" = (select max(coalesce("PHENOMENON_TIME_END", "PHENOMENON_TIME_START")) from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = $1."MULTI_DATASTREAM_ID")';
    end if;

    if (OLD."RESULT_TIME" = "MDS_ROW"."RESULT_TIME_START")
    then
        queryset := queryset || delimitr || '"RESULT_TIME_START" = (select min("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = $1."MULTI_DATASTREAM_ID")';
        delimitr := ',';
    end if;
    if (OLD."RESULT_TIME" = "MDS_ROW"."RESULT_TIME_END")
    then
        queryset := queryset || delimitr || '"RESULT_TIME_END" = (select max("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = $1."MULTI_DATASTREAM_ID")';
        delimitr := ',';
    end if;
    if (delimitr = ',') then
        EXECUTE 'update "MULTI_DATASTREAMS" SET ' || queryset ||  ' where "MULTI_DATASTREAMS"."ID"=$1."MULTI_DATASTREAM_ID"' using OLD;
    end if;
end if;    

return NULL;
end
$BODY$
    language plpgsql volatile
    cost 100;


do $$ begin
    create trigger multidatastreams_actualization_delete
        after delete
        on "OBSERVATIONS"
        for each row
        execute procedure multidatastreams_update_delete();
exception
    when others then null;
end $$;
