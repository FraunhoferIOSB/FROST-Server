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
-- MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Lesser General Public License for more details.
--
-- You should have received a copy of the GNU Lesser General Public License
-- along with this program.  If not, see <http://www.gnu.org/licenses/>.


-- ---------------------------------------
-- Function: datastreams_update_insert()
--
-- Updated fields are:
-- phenomenon_time_start,phenomenon_time_end,result_time_start,result_time_end and observed_area
-- ---------------------------------------
create OR replace function datastreams_update_insert()
    returns trigger as
$BODY$
declare
    "DS_ROW" RECORD;
    queryset TEXT := '';
    delimitr char(1) := ' ';
begin

if (NEW."datastream_id" is not null)
then
    select "id","phenomenon_time_start","phenomenon_time_end","result_time_start","result_time_end","observed_area","last_foi_id"
        into "DS_ROW" from "datastreams" where "datastreams"."id"=NEW."datastream_id";
    if (NEW."phenomenon_time_start"<"DS_ROW"."phenomenon_time_start" OR "DS_ROW"."phenomenon_time_start" IS NULL) then
        queryset := queryset || delimitr || '"phenomenon_time_start" = $1."phenomenon_time_start"';
        delimitr := ',';
    end if;
    if (coalesce(NEW."phenomenon_time_end", NEW."phenomenon_time_start") > "DS_ROW"."phenomenon_time_end" OR "DS_ROW"."phenomenon_time_end" IS NULL) then
        queryset := queryset || delimitr || '"phenomenon_time_end" = coalesce($1."phenomenon_time_end", $1."phenomenon_time_start")';
        delimitr := ',';
    end if;

    if (NEW."result_time" is not null) then
        if (NEW."result_time"<"DS_ROW"."result_time_start" OR "DS_ROW"."result_time_start" IS NULL) then
            queryset := queryset || delimitr || '"result_time_start" = $1."result_time"';
            delimitr := ',';
        end if;
        if (NEW."result_time" > "DS_ROW"."result_time_end" OR "DS_ROW"."result_time_end" IS NULL) then
            queryset := queryset || delimitr || '"result_time_end" = $1."result_time"';
            delimitr := ',';
        end if;
    end if;

    if ("DS_ROW"."last_foi_id" IS NULL OR "DS_ROW"."last_foi_id" != NEW."feature_id") then
        queryset := queryset || delimitr || '"last_foi_id" = $1."feature_id"';
        queryset := queryset || ',"observed_area" = ST_ConvexHull(ST_Collect("observed_area", (select "geom" from "features" where "id"=$1."feature_id")))';
        delimitr := ',';
    end if;
    if (delimitr = ',') then
        EXECUTE 'update "datastreams" SET ' || queryset ||  ' where "datastreams"."id"=$1."datastream_id"' using NEW;
    end if;
    return new;
end if;

return new;
END
$BODY$
    language plpgsql volatile
    cost 100;



do $$ begin
    create trigger datastreams_actualization_insert
        after insert
        on "observations"
        for each row
        execute procedure datastreams_update_insert();
exception
    when others then null;
end $$;




-- ---------------------------------------
-- Function: datastreams_update_update()
--
-- Updated fields are:
-- phenomenon_time_start,phenomenon_time_end,result_time_start,result_time_end.
-- Warning: observed_area not taken into account.
-- ---------------------------------------
create OR replace function datastreams_update_update()
    returns trigger as
$BODY$
declare
    "DS_ROW" "datastreams"%rowtype;
    queryset TEXT := '';
    delimitr char(1) := ' ';
begin

if (NEW."datastream_id" is not null)
then
    if (NEW."phenomenon_time_start" != OLD."phenomenon_time_start" OR NEW."phenomenon_time_end" != OLD."phenomenon_time_end") then
        for "DS_ROW" in select * from "datastreams" where "id"=NEW."datastream_id"
        loop
            if (NEW."phenomenon_time_start"<"DS_ROW"."phenomenon_time_start") then
                queryset := queryset || delimitr || '"phenomenon_time_start" = $1."phenomenon_time_start"';
                delimitr := ',';
            elseif (OLD."phenomenon_time_start" = "DS_ROW"."phenomenon_time_start") then
                queryset := queryset || delimitr || '"phenomenon_time_start" = (select min("phenomenon_time_start") from "observations" where "observations"."datastream_id" = $1."datastream_id")';
                delimitr := ',';
            end if;
            if (coalesce(NEW."phenomenon_time_end", NEW."phenomenon_time_start") > "DS_ROW"."phenomenon_time_end") then
                queryset := queryset || delimitr || '"phenomenon_time_end" = coalesce($1."phenomenon_time_end", $1."phenomenon_time_start")';
                delimitr := ',';
            elseif (coalesce(OLD."phenomenon_time_end", OLD."phenomenon_time_start") = "DS_ROW"."phenomenon_time_end") then
                queryset := queryset || delimitr || '"phenomenon_time_end" = (select max(coalesce("phenomenon_time_end", "phenomenon_time_start")) from "observations" where "observations"."datastream_id" = $1."datastream_id")';
                delimitr := ',';
            end if;
        end loop;
    end if;


    if (NEW."result_time" != OLD."result_time") then
        for "DS_ROW" in select * from "datastreams" where "id"=NEW."datastream_id"
        loop
            if (NEW."result_time" < "DS_ROW"."result_time_start") then
                queryset := queryset || delimitr || '"result_time_start" = $1."result_time"';
                delimitr := ',';
            elseif (OLD."result_time" = "DS_ROW"."result_time_start") then
                queryset := queryset || delimitr || '"result_time_start" = (select min("result_time") from "observations" where "observations"."datastream_id" = $1."datastream_id")';
                delimitr := ',';
            end if;
            if (NEW."result_time" > "DS_ROW"."result_time_end") then
                queryset := queryset || delimitr || '"result_time_end" = $1."result_time"';
                delimitr := ',';
            elseif (OLD."result_time" = "DS_ROW"."result_time_end") then
                queryset := queryset || delimitr || '"result_time_end" = (select max("result_time") from "observations" where "observations"."datastream_id" = $1."datastream_id")';
                delimitr := ',';
            end if;
        end loop;
    end if;
    if (delimitr = ',') then
        EXECUTE 'update "datastreams" SET ' || queryset ||  ' where "datastreams"."id"=$1."datastream_id"' using NEW;
    end if;
end if;


return new;
END
$BODY$
    language plpgsql volatile
    cost 100;


do $$ begin
    create trigger datastreams_actualization_update
        after update
        on "observations"
        for each row
        execute procedure datastreams_update_update();
exception
    when others then null;
end $$;



-- ---------------------------------------
-- Function: datastreams_update_delete()
--
-- Updated fields are:
-- phenomenon_time_start,phenomenon_time_end,result_time_start,result_time_end.
-- Warning: observed_area not taken into account.
-- ---------------------------------------
create OR replace function datastreams_update_delete()
    returns trigger as
$BODY$
declare
    "DS_ROW" "datastreams"%rowtype;
    queryset TEXT := '';
    delimitr char(1) := ' ';
begin

if (OLD."datastream_id" is not null)
then
    select * into "DS_ROW" from "datastreams" where "datastreams"."id"=OLD."datastream_id";

    if (OLD."phenomenon_time_start" = "DS_ROW"."phenomenon_time_start"
        OR coalesce(OLD."phenomenon_time_end", OLD."phenomenon_time_start") = "DS_ROW"."phenomenon_time_end")
    then
        queryset := queryset || delimitr || '"phenomenon_time_start" = (select min("phenomenon_time_start") from "observations" where "observations"."datastream_id" = $1."datastream_id")';
        delimitr := ',';
        queryset := queryset || delimitr || '"phenomenon_time_end" = (select max(coalesce("phenomenon_time_end", "phenomenon_time_start")) from "observations" where "observations"."datastream_id" = $1."datastream_id")';
    end if;

    if (OLD."result_time" = "DS_ROW"."result_time_start")
    then
        queryset := queryset || delimitr || '"result_time_start" = (select min("result_time") from "observations" where "observations"."datastream_id" = $1."datastream_id")';
        delimitr := ',';
    end if;
    if (OLD."result_time" = "DS_ROW"."result_time_end")
    then
        queryset := queryset || delimitr || '"result_time_end" = (select max("result_time") from "observations" where "observations"."datastream_id" = $1."datastream_id")';
        delimitr := ',';
    end if;
    if (delimitr = ',') then
        EXECUTE 'update "datastreams" SET ' || queryset ||  ' where "datastreams"."id"=$1."datastream_id"' using OLD;
    end if;
end if;    

return NULL;
end
$BODY$
    language plpgsql volatile
    cost 100;


do $$ begin
    create trigger datastreams_actualization_delete
        after delete
        on "observations"
        for each row
        execute procedure datastreams_update_delete();
exception
    when others then null;
end $$;
