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
