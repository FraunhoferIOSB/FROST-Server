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
DECLARE v_num_value NUMERIC DEFAULT NULL;
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
DECLARE v_bool_value BOOLEAN DEFAULT NULL;
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
"DS_ROW" "DATASTREAMS"%rowtype;
"MDS_ROW" "MULTI_DATASTREAMS"%rowtype;
begin

if (NEW."DATASTREAM_ID" is not null) 
then 
	select * into "DS_ROW" from "DATASTREAMS" where "DATASTREAMS"."ID"=NEW."DATASTREAM_ID";
	if (NEW."PHENOMENON_TIME_START"<"DS_ROW"."PHENOMENON_TIME_START" or "DS_ROW"."PHENOMENON_TIME_START" is null) then
		update "DATASTREAMS" set "PHENOMENON_TIME_START" = NEW."PHENOMENON_TIME_START" where "DATASTREAMS"."ID" = "DS_ROW"."ID";
	end if;
	if (coalesce(NEW."PHENOMENON_TIME_END", NEW."PHENOMENON_TIME_START") > "DS_ROW"."PHENOMENON_TIME_END" or "DS_ROW"."PHENOMENON_TIME_END" is null) then
		update "DATASTREAMS" set "PHENOMENON_TIME_END" = coalesce(NEW."PHENOMENON_TIME_END", NEW."PHENOMENON_TIME_START") where "DATASTREAMS"."ID" = "DS_ROW"."ID";
	end if;

	if (NEW."RESULT_TIME"<"DS_ROW"."RESULT_TIME_START" or "DS_ROW"."RESULT_TIME_START" is null) then
		update "DATASTREAMS" set "RESULT_TIME_START" = NEW."RESULT_TIME" where "DATASTREAMS"."ID" = "DS_ROW"."ID";
	end if;
	if (NEW."RESULT_TIME" > "DS_ROW"."RESULT_TIME_END" or "DS_ROW"."RESULT_TIME_END" is null) then
		update "DATASTREAMS" set "RESULT_TIME_END" = NEW."RESULT_TIME" where "DATASTREAMS"."ID" = "DS_ROW"."ID";
	end if;

	update "DATASTREAMS" SET "OBSERVED_AREA" = ST_ConvexHull(ST_Collect("OBSERVED_AREA", (select "GEOM" from "FEATURES" where "ID"=NEW."FEATURE_ID"))) where "DATASTREAMS"."ID"=NEW."DATASTREAM_ID";
end if;

if (NEW."MULTI_DATASTREAM_ID" is not null) 
then 
	select * into "MDS_ROW" from "MULTI_DATASTREAMS" where "MULTI_DATASTREAMS"."ID"=NEW."MULTI_DATASTREAM_ID";
	if (NEW."PHENOMENON_TIME_START"<"MDS_ROW"."PHENOMENON_TIME_START" or "MDS_ROW"."PHENOMENON_TIME_START" is null) then
		update "MULTI_DATASTREAMS" set "PHENOMENON_TIME_START" = NEW."PHENOMENON_TIME_START" where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
	end if;
	if (coalesce(NEW."PHENOMENON_TIME_END", NEW."PHENOMENON_TIME_START") > "MDS_ROW"."PHENOMENON_TIME_END" or "MDS_ROW"."PHENOMENON_TIME_END" is null) then
		update "MULTI_DATASTREAMS" set "PHENOMENON_TIME_END" = coalesce(NEW."PHENOMENON_TIME_END", NEW."PHENOMENON_TIME_START") where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
	end if;

	if (NEW."RESULT_TIME"<"MDS_ROW"."RESULT_TIME_START" or "MDS_ROW"."RESULT_TIME_START" is null) then
		update "MULTI_DATASTREAMS" set "RESULT_TIME_START" = NEW."RESULT_TIME" where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
	end if;
	if (NEW."RESULT_TIME" > "MDS_ROW"."RESULT_TIME_END" or "MDS_ROW"."RESULT_TIME_END" is null) then
		update "MULTI_DATASTREAMS" set "RESULT_TIME_END" = NEW."RESULT_TIME" where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
	end if;

	update "MULTI_DATASTREAMS" SET "OBSERVED_AREA" = ST_ConvexHull(ST_Collect("OBSERVED_AREA", (select "GEOM" from "FEATURES" where "ID"=NEW."FEATURE_ID"))) where "MULTI_DATASTREAMS"."ID"=NEW."MULTI_DATASTREAM_ID";
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
begin

if (NEW."DATASTREAM_ID" is not null) 
then 
	if (NEW."PHENOMENON_TIME_START" != OLD."PHENOMENON_TIME_START" or NEW."PHENOMENON_TIME_END" != OLD."PHENOMENON_TIME_END") then
		for "DS_ROW" in select * from "DATASTREAMS" where "ID"=NEW."DATASTREAM_ID"
		loop
			if (NEW."PHENOMENON_TIME_START"<"DS_ROW"."PHENOMENON_TIME_START") then
				update "DATASTREAMS" set "PHENOMENON_TIME_START" = NEW."PHENOMENON_TIME_START" where "DATASTREAMS"."ID" = "DS_ROW"."ID";
			end if;
			if (coalesce(NEW."PHENOMENON_TIME_END", NEW."PHENOMENON_TIME_START") > "DS_ROW"."PHENOMENON_TIME_END") then
				update "DATASTREAMS" set "PHENOMENON_TIME_END" = coalesce(NEW."PHENOMENON_TIME_END", NEW."PHENOMENON_TIME_START") where "DATASTREAMS"."ID" = "DS_ROW"."ID";
			end if;

			if (OLD."PHENOMENON_TIME_START" = "DS_ROW"."PHENOMENON_TIME_START"
				or coalesce(OLD."PHENOMENON_TIME_END", OLD."PHENOMENON_TIME_START") = "DS_ROW"."PHENOMENON_TIME_END")
			then
				update "DATASTREAMS"
					set "PHENOMENON_TIME_START" = (select min("PHENOMENON_TIME_START") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = "DS_ROW"."ID")
					where "DATASTREAMS"."ID" = "DS_ROW"."ID";
				update "DATASTREAMS"
					set "PHENOMENON_TIME_END" = (select max(coalesce("PHENOMENON_TIME_END", "PHENOMENON_TIME_START")) from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = "DS_ROW"."ID")
					where "DATASTREAMS"."ID" = "DS_ROW"."ID";
			end if;
		end loop;
		return NEW;
	end if;


	if (NEW."RESULT_TIME" != OLD."RESULT_TIME") then
		for "DS_ROW" in select * from "DATASTREAMS" where "ID"=NEW."DATASTREAM_ID"
		loop
			if (NEW."RESULT_TIME" < "DS_ROW"."RESULT_TIME_START") then
				update "DATASTREAMS" set "RESULT_TIME_START" = NEW."RESULT_TIME" where "ID" = "DS_ROW"."ID";
			end if;
			if (NEW."RESULT_TIME" > "DS_ROW"."RESULT_TIME_END") then
				update "DATASTREAMS" set "RESULT_TIME_END" = NEW."RESULT_TIME" where "ID" = "DS_ROW"."ID";
			end if;

			if (OLD."RESULT_TIME" = "DS_ROW"."RESULT_TIME_START")
			then
				update "DATASTREAMS"
					set "RESULT_TIME_START" = (select min("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = "DS_ROW"."ID")
					where "DATASTREAMS"."ID" = "DS_ROW"."ID";
			end if;
			if (OLD."RESULT_TIME" = "DS_ROW"."RESULT_TIME_END")
			then
				update "DATASTREAMS"
					set "RESULT_TIME_END" = (select max("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = "DS_ROW"."ID")
					where "DATASTREAMS"."ID" = "DS_ROW"."ID";
			end if;
		end loop;
		return NEW;
	end if;
end if;

if (NEW."MULTI_DATASTREAM_ID" is not null) 
then 
	if (NEW."PHENOMENON_TIME_START" != OLD."PHENOMENON_TIME_START" or NEW."PHENOMENON_TIME_END" != OLD."PHENOMENON_TIME_END") then
		select * into "MDS_ROW" from "MULTI_DATASTREAMS" where "MULTI_DATASTREAMS"."ID"=NEW."MULTI_DATASTREAM_ID";
		
		if (NEW."PHENOMENON_TIME_START"<"MDS_ROW"."PHENOMENON_TIME_START") then
			update "MULTI_DATASTREAMS" set "PHENOMENON_TIME_START" = NEW."PHENOMENON_TIME_START" where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
		end if;
		if (coalesce(NEW."PHENOMENON_TIME_END", NEW."PHENOMENON_TIME_START") > "MDS_ROW"."PHENOMENON_TIME_END") then
			update "MULTI_DATASTREAMS" set "PHENOMENON_TIME_END" = coalesce(NEW."PHENOMENON_TIME_END", NEW."PHENOMENON_TIME_START") where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
		end if;

		if (OLD."PHENOMENON_TIME_START" = "MDS_ROW"."PHENOMENON_TIME_START"
			or coalesce(OLD."PHENOMENON_TIME_END", OLD."PHENOMENON_TIME_START") = "MDS_ROW"."PHENOMENON_TIME_END")
		then
			update "MULTI_DATASTREAMS"
				set "PHENOMENON_TIME_START" = (select min("PHENOMENON_TIME_START") from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = "MDS_ROW"."ID")
				where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
			update "MULTI_DATASTREAMS"
				set "PHENOMENON_TIME_END" = (select max(coalesce("PHENOMENON_TIME_END", "PHENOMENON_TIME_START")) from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = "MDS_ROW"."ID")
				where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
		end if;
		
		return NEW;
	end if;


	if (NEW."RESULT_TIME" != OLD."RESULT_TIME") then
		select * into "MDS_ROW" from "MULTI_DATASTREAMS" where "MULTI_DATASTREAMS"."ID"=NEW."MULTI_DATASTREAM_ID";
		
		if (NEW."RESULT_TIME" < "MDS_ROW"."RESULT_TIME_START") then
			update "MULTI_DATASTREAMS" set "RESULT_TIME_START" = NEW."RESULT_TIME" where "ID" = "MDS_ROW"."ID";
		end if;
		if (NEW."RESULT_TIME" > "MDS_ROW"."RESULT_TIME_END") then
			update "MULTI_DATASTREAMS" set "RESULT_TIME_END" = NEW."RESULT_TIME" where "ID" = "MDS_ROW"."ID";
		end if;

		if (OLD."RESULT_TIME" = "MDS_ROW"."RESULT_TIME_START")
		then
			update "MULTI_DATASTREAMS"
				set "RESULT_TIME_START" = (select min("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = "MDS_ROW"."ID")
				where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
		end if;
		if (OLD."RESULT_TIME" = "MDS_ROW"."RESULT_TIME_END")
		then
			update "MULTI_DATASTREAMS"
				set "RESULT_TIME_END" = (select max("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = "MDS_ROW"."ID")
				where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
		end if;

		return NEW;
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
begin

if (OLD."DATASTREAM_ID" is not null) 
then
	select * into "DS_ROW" from "DATASTREAMS" where "DATASTREAMS"."ID"=OLD."DATASTREAM_ID";

	if (OLD."PHENOMENON_TIME_START" = "DS_ROW"."PHENOMENON_TIME_START"
		or coalesce(OLD."PHENOMENON_TIME_END", OLD."PHENOMENON_TIME_START") = "DS_ROW"."PHENOMENON_TIME_END")
	then
		update "DATASTREAMS"
			set "PHENOMENON_TIME_START" = (select min("PHENOMENON_TIME_START") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = "DS_ROW"."ID")
			where "DATASTREAMS"."ID" = "DS_ROW"."ID";
		update "DATASTREAMS"
			set "PHENOMENON_TIME_END" = (select max(coalesce("PHENOMENON_TIME_END", "PHENOMENON_TIME_START")) from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = "DS_ROW"."ID")
			where "DATASTREAMS"."ID" = "DS_ROW"."ID";
	end if;

	if (OLD."RESULT_TIME" = "DS_ROW"."RESULT_TIME_START")
	then
		update "DATASTREAMS"
			set "RESULT_TIME_START" = (select min("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = "DS_ROW"."ID")
			where "DATASTREAMS"."ID" = "DS_ROW"."ID";
	end if;
	if (OLD."RESULT_TIME" = "DS_ROW"."RESULT_TIME_END")
	then
		update "DATASTREAMS"
			set "RESULT_TIME_END" = (select max("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."DATASTREAM_ID" = "DS_ROW"."ID")
			where "DATASTREAMS"."ID" = "DS_ROW"."ID";
	end if;

end if;	

if (OLD."MULTI_DATASTREAM_ID" is not null) 
then
	select * into "MDS_ROW" from "MULTI_DATASTREAMS" where "MULTI_DATASTREAMS"."ID"=OLD."MULTI_DATASTREAM_ID";

	if (OLD."PHENOMENON_TIME_START" = "DS_ROW"."PHENOMENON_TIME_START"
		or coalesce(OLD."PHENOMENON_TIME_END", OLD."PHENOMENON_TIME_START") = "MDS_ROW"."PHENOMENON_TIME_END")
	then
		update "MULTI_DATASTREAMS"
			set "PHENOMENON_TIME_START" = (select min("PHENOMENON_TIME_START") from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = "MDS_ROW"."ID")
			where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
		update "MULTI_DATASTREAMS"
			set "PHENOMENON_TIME_END" = (select max(coalesce("PHENOMENON_TIME_END", "PHENOMENON_TIME_START")) from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = "MDS_ROW"."ID")
			where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
	end if;

	if (OLD."RESULT_TIME" = "MDS_ROW"."RESULT_TIME_START")
	then
		update "MULTI_DATASTREAMS"
			set "RESULT_TIME_START" = (select min("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = "MDS_ROW"."ID")
			where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
	end if;
	if (OLD."RESULT_TIME" = "MDS_ROW"."RESULT_TIME_END")
	then
		update "MULTI_DATASTREAMS"
			set "RESULT_TIME_END" = (select max("RESULT_TIME") from "OBSERVATIONS" where "OBSERVATIONS"."MULTI_DATASTREAM_ID" = "MDS_ROW"."ID")
			where "MULTI_DATASTREAMS"."ID" = "MDS_ROW"."ID";
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
