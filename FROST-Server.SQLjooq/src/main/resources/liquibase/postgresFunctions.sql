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
-- Safe cast function from jsonb to numeric.
-- Returns NULL for inputs that are not json numbers.
-- ---------------------------------------
CREATE OR REPLACE FUNCTION safe_cast_to_numeric(v_input jsonb)
RETURNS NUMERIC AS $$
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
BEGIN
    IF jsonb_typeof(v_input) = 'boolean' THEN
        RETURN (v_input#>>'{}')::boolean;
    ELSE
        RETURN NULL;
    END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


-- ---------------------------------------
-- Calls the timezone function, after checking of the zoneid is an offset.
-- If the zoneid is an offset, the offset is reversed.
-- ---------------------------------------
CREATE OR REPLACE FUNCTION timezone_with_iso_offsets(zoneid text, tvalue timestamptz)
RETURNS TIMESTAMP AS $$
BEGIN
    IF zoneid ~ '^([+-])?[0-9]{1,2}(:[0-9][0-9](:[0-9][0-9])?)?$' THEN
        IF starts_with(zoneid, '-') THEN
            RETURN timezone(substring(zoneid from 2), tvalue);
        ELSIF starts_with(zoneid, '+') THEN
            RETURN timezone('-' || substring(zoneid from 2), tvalue);
        ELSE
            RETURN timezone('-' || zoneid, tvalue);
        END IF;
    ELSE
        RETURN timezone(zoneid, tvalue);
    END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


-- ---------------------------------------
-- Estimates the count for the given query using the query optimiser.
-- Returns the estimated count.
-- ---------------------------------------
CREATE OR REPLACE FUNCTION count_estimate(query text)
  RETURNS integer
  LANGUAGE plpgsql AS
$func$
DECLARE
    rec   record;
    rows  integer;
BEGIN
    FOR rec IN EXECUTE 'EXPLAIN ' || query LOOP
        rows := substring(rec."QUERY PLAN" FROM ' rows=([[:digit:]]+)');
        EXIT WHEN rows IS NOT NULL;
    END LOOP;

    RETURN rows;
END
$func$;
