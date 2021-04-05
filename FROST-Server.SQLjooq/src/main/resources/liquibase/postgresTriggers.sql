-- Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
-- Safe cast function from jsonb to numeric.
-- Returns NULL for inputs that are not json numbers.
-- ---------------------------------------
CREATE OR REPLACE FUNCTION safe_cast_to_numeric(v_input jsonb)
    RETURNS NUMERIC AS $$
DECLARE
    v_num_value NUMERIC DEFAULT NULL;
BEGIN
    IF jsonb_typeof(v_input) = 'number' THEN
        RETURN (v_input#>>'{}')::numeric;
    ELSE
        RETURN NULL;
    END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


-- ---------------------------------------
-- Safe cast function from jsonb to boolean.
-- Returns NULL for inputs that are not json booleans.
-- ---------------------------------------
CREATE OR REPLACE FUNCTION safe_cast_to_boolean(v_input jsonb)
    RETURNS BOOLEAN AS $$
DECLARE
    v_bool_value BOOLEAN DEFAULT NULL;
BEGIN
    IF jsonb_typeof(v_input) = 'boolean' THEN
        RETURN (v_input#>>'{}')::boolean;
    ELSE
        RETURN NULL;
    END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


-- ---------------------------------------
-- Trigger: datastreams_actualization_insert on OBSERVATIONS
-- ---------------------------------------
drop trigger if exists datastreams_actualization_insert ON "OBSERVATIONS";

-- ---------------------------------------
-- Function: datastreams_update_insert()
--
-- This function also updates multidatastreams. Updated fields are:
-- PHENOMENON_TIME_START,PHENOMENON_TIME_END,RESULT_TIME_START,RESULT_TIME_END and OBSERVED_AREA
-- ---------------------------------------
create or replace function datastreams_update_insert()
    returns trigger as
$BODY$
declare
    "DS_ROW" RECORD;
    "MDS_ROW" RECORD;
    queryset TEXT := '';
    delimitr char(1) := ' ';
begin

if (NEW."DATASTREAM_ID" is not null) 
then 
    select "ID","PHENOMENON_TIME_START","PHENOMENON_TIME_END","RESULT_TIME_START","RESULT_TIME_END","OBSERVED_AREA","LAST_FOI_ID"
        into "DS_ROW" from "DATASTREAMS" where "DATASTREAMS"."ID"=NEW."DATASTREAM_ID";
    if (NEW."PHENOMENON_TIME_START"<"DS_ROW"."PHENOMENON_TIME_START" or "DS_ROW"."PHENOMENON_TIME_START" is null) then
        queryset := queryset || delimitr || '"PHENOMENON_TIME_START" = $1."PHENOMENON_TIME_START"';
        delimitr := ',';
    end if;
    if (coalesce(NEW."PHENOMENON_TIME_END", NEW."PHENOMENON_TIME_START") > "DS_ROW"."PHENOMENON_TIME_END" or "DS_ROW"."PHENOMENON_TIME_END" is null) then
        queryset := queryset || delimitr || '"PHENOMENON_TIME_END" = coalesce($1."PHENOMENON_TIME_END", $1."PHENOMENON_TIME_START")';
        delimitr := ',';
    end if;

    if (NEW."RESULT_TIME" is not null) then
        if (NEW."RESULT_TIME"<"DS_ROW"."RESULT_TIME_START" or "DS_ROW"."RESULT_TIME_START" is null) then
            queryset := queryset || delimitr || '"RESULT_TIME_START" = $1."RESULT_TIME"';
            delimitr := ',';
        end if;
        if (NEW."RESULT_TIME" > "DS_ROW"."RESULT_TIME_END" or "DS_ROW"."RESULT_TIME_END" is null) then
            queryset := queryset || delimitr || '"RESULT_TIME_END" = $1."RESULT_TIME"';
            delimitr := ',';
        end if;
    end if;

    if ("DS_ROW"."LAST_FOI_ID" is null or "DS_ROW"."LAST_FOI_ID" != NEW."FEATURE_ID") then
        queryset := queryset || delimitr || '"LAST_FOI_ID" = $1."FEATURE_ID"';
        queryset := queryset || ',"OBSERVED_AREA" = ST_ConvexHull(ST_Collect("OBSERVED_AREA", (select "GEOM" from "FEATURES" where "ID"=$1."FEATURE_ID")))';
        delimitr := ',';
    end if;
    if (delimitr = ',') then
        EXECUTE 'update "DATASTREAMS" SET ' || queryset ||  ' where "DATASTREAMS"."ID"=$1."DATASTREAM_ID"' using NEW;
    end if;
    return new;
end if;

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
end if;

return new;
END
$BODY$
    language plpgsql volatile
    cost 100;



create trigger datastreams_actualization_insert
    after insert
    on "OBSERVATIONS"
    for each row
    execute procedure datastreams_update_insert();




-- ---------------------------------------
-- Trigger: datastreams_actualization_update on OBSERVATIONS
-- ---------------------------------------
drop trigger if exists datastreams_actualization_update ON "OBSERVATIONS";

-- ---------------------------------------
-- Function: datastreams_update_update()
--
-- This function also updates multidatastreams. Updated fields are:
-- PHENOMENON_TIME_START,PHENOMENON_TIME_END,RESULT_TIME_START,RESULT_TIME_END.
-- Warning: OBSERVED_AREA not taken into account. 
-- ---------------------------------------
create or replace function datastreams_update_update()
    returns trigger as
$BODY$
declare
    "DS_ROW" "DATASTREAMS"%rowtype;
    "MDS_ROW" "MULTI_DATASTREAMS"%rowtype;
    queryset TEXT := '';
    delimitr char(1) := ' ';
begin

if (NEW."DATASTREAM_ID" is not null) 
then 
    if (NEW."PHENOMENON_TIME_START" != OLD."PHENOMENON_TIME_START" or NEW."PHENOMENON_TIME_END" != OLD."PHENOMENON_TIME_END") then
        for "DS_ROW" in select * from "DATASTREAMS" where "ID"=NEW."DATASTREAM_ID"
        loop
            if (NEW."PHENOMENON_TIME_START"<"DS_ROW"."PHENOMENON_TIME_START") then
                queryset := queryset || delimitr || '"PHENOMENON_TIME_START" = $1."PHENOMENON_TIME_START"';
                delimitr := ',';
            elseif (OLD."PHENOMENON_TIME_START" = "DS_ROW"."PHENOMENON_TIME_START") then
                queryset := queryset || delimitr || '"PHENOMENON_TIME_START" = (select min("PHENOMENON_TIME_START") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = $1."DATASTREAM_ID")';
                delimitr := ',';
            end if;
            if (coalesce(NEW."PHENOMENON_TIME_END", NEW."PHENOMENON_TIME_START") > "DS_ROW"."PHENOMENON_TIME_END") then
                queryset := queryset || delimitr || '"PHENOMENON_TIME_END" = coalesce($1."PHENOMENON_TIME_END", $1."PHENOMENON_TIME_START")';
                delimitr := ',';
            elseif (coalesce(OLD."PHENOMENON_TIME_END", OLD."PHENOMENON_TIME_START") = "DS_ROW"."PHENOMENON_TIME_END") then
                queryset := queryset || delimitr || '"PHENOMENON_TIME_END" = (select max(coalesce("PHENOMENON_TIME_END", "PHENOMENON_TIME_START")) from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = $1."DATASTREAM_ID")';
                delimitr := ',';
            end if;
        end loop;
    end if;


    if (NEW."RESULT_TIME" != OLD."RESULT_TIME") then
        for "DS_ROW" in select * from "DATASTREAMS" where "ID"=NEW."DATASTREAM_ID"
        loop
            if (NEW."RESULT_TIME" < "DS_ROW"."RESULT_TIME_START") then
                queryset := queryset || delimitr || '"RESULT_TIME_START" = $1."RESULT_TIME"';
                delimitr := ',';
            elseif (OLD."RESULT_TIME" = "DS_ROW"."RESULT_TIME_START") then
                queryset := queryset || delimitr || '"RESULT_TIME_START" = (select min("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = $1."DATASTREAM_ID")';
                delimitr := ',';
            end if;
            if (NEW."RESULT_TIME" > "DS_ROW"."RESULT_TIME_END") then
                queryset := queryset || delimitr || '"RESULT_TIME_END" = $1."RESULT_TIME"';
                delimitr := ',';
            elseif (OLD."RESULT_TIME" = "DS_ROW"."RESULT_TIME_END") then
                queryset := queryset || delimitr || '"RESULT_TIME_END" = (select max("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = $1."DATASTREAM_ID")';
                delimitr := ',';
            end if;
        end loop;
    end if;
    if (delimitr = ',') then
        EXECUTE 'update "DATASTREAMS" SET ' || queryset ||  ' where "DATASTREAMS"."ID"=$1."DATASTREAM_ID"' using NEW;
    end if;
end if;

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


create trigger datastreams_actualization_update
    after update
    on "OBSERVATIONS"
    for each row
    execute procedure datastreams_update_update();



-- ---------------------------------------
-- Trigger: datastreams_actualization_delete on OBSERVATIONS
-- ---------------------------------------
drop trigger if exists datastreams_actualization_delete ON "OBSERVATIONS";

-- ---------------------------------------
-- Function: datastreams_update_delete()
--
-- This function also updates multidatastreams. Updated fields are:
-- PHENOMENON_TIME_START,PHENOMENON_TIME_END,RESULT_TIME_START,RESULT_TIME_END.
-- Warning: OBSERVED_AREA not taken into account. 
-- ---------------------------------------
create or replace function datastreams_update_delete()
    returns trigger as
$BODY$
declare
    "DS_ROW" "DATASTREAMS"%rowtype;
    "MDS_ROW" "MULTI_DATASTREAMS"%rowtype;
    queryset TEXT := '';
    delimitr char(1) := ' ';
begin

if (OLD."DATASTREAM_ID" is not null) 
then
    select * into "DS_ROW" from "DATASTREAMS" where "DATASTREAMS"."ID"=OLD."DATASTREAM_ID";

    if (OLD."PHENOMENON_TIME_START" = "DS_ROW"."PHENOMENON_TIME_START"
        or coalesce(OLD."PHENOMENON_TIME_END", OLD."PHENOMENON_TIME_START") = "DS_ROW"."PHENOMENON_TIME_END")
    then
        queryset := queryset || delimitr || '"PHENOMENON_TIME_START" = (select min("PHENOMENON_TIME_START") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = $1."DATASTREAM_ID")';
        delimitr := ',';
        queryset := queryset || delimitr || '"PHENOMENON_TIME_END" = (select max(coalesce("PHENOMENON_TIME_END", "PHENOMENON_TIME_START")) from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = $1."DATASTREAM_ID")';
    end if;

    if (OLD."RESULT_TIME" = "DS_ROW"."RESULT_TIME_START")
    then
        queryset := queryset || delimitr || '"RESULT_TIME_START" = (select min("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = $1."DATASTREAM_ID")';
        delimitr := ',';
    end if;
    if (OLD."RESULT_TIME" = "DS_ROW"."RESULT_TIME_END")
    then
        queryset := queryset || delimitr || '"RESULT_TIME_END" = (select max("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = $1."DATASTREAM_ID")';
        delimitr := ',';
    end if;
    if (delimitr = ',') then
        EXECUTE 'update "DATASTREAMS" SET ' || queryset ||  ' where "DATASTREAMS"."ID"=$1."DATASTREAM_ID"' using NEW;
    end if;
end if;    

if (OLD."MULTI_DATASTREAM_ID" is not null) 
then
    select * into "MDS_ROW" from "MULTI_DATASTREAMS" where "MULTI_DATASTREAMS"."ID"=OLD."MULTI_DATASTREAM_ID";

    if (OLD."PHENOMENON_TIME_START" = "DS_ROW"."PHENOMENON_TIME_START"
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
        EXECUTE 'update "MULTI_DATASTREAMS" SET ' || queryset ||  ' where "MULTI_DATASTREAMS"."ID"=$1."MULTI_DATASTREAM_ID"' using NEW;
    end if;
end if;    


return NULL;
end
$BODY$
    language plpgsql volatile
    cost 100;


create trigger datastreams_actualization_delete
    after delete
    on "OBSERVATIONS"
    for each row
    execute procedure datastreams_update_delete();
