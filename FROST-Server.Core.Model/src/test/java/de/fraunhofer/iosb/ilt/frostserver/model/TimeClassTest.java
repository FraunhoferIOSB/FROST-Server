/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package de.fraunhofer.iosb.ilt.frostserver.model;

import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DurationConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntervalConstant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author hylke
 */
class TimeClassTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "2016-01-02T00:00:00Z",
        "2016-01-02T01:00:00Z",
        "2016-01-02T01:01:00Z",
        "2016-01-02T01:01:01Z",
        "2016-01-02T01:01:01.1Z",
        "2016-01-02T01:01:01.12Z",
        "2016-01-02T01:01:01.123Z",
        "2016-01-02T01:01:01.1234Z",
        "2016-01-02T01:01:01.12345Z",
        "2016-01-02T01:01:01.123456Z",
        "2016-01-02T01:01:01.1234567Z",
        "2016-01-02T01:01:01.12345678Z",
        "2016-01-02T01:01:01.123456789Z",
        "2016-01-03T23:59:59.999Z"
    })
    void TestTimeInstant(String value) {
        TimeInstant instant = TimeInstant.parse(value);
        Assertions.assertEquals(value, instant.asISO8601());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "2016-01-02T01:01:01.000000000Z|2016-01-02T01:01:01Z",
        "2016-01-02T01:01:01.100000000Z|2016-01-02T01:01:01.1Z",
        "2016-01-02T01:01:01.123456780Z|2016-01-02T01:01:01.12345678Z"
    })
    void TestTimeInstant(String value, String expected) {
        TimeInstant instant = TimeInstant.parse(value);
        Assertions.assertEquals(expected, instant.asISO8601());
    }

    @Test
    void TestTimeInterval() {
        String value = "2016-01-02T01:01:01Z/2016-01-03T23:59:59.999Z";
        TimeInterval interval = TimeInterval.parse(value);
        Assertions.assertEquals(value, interval.asISO8601());
    }

    @Test
    void testIntervalConstant() {
        String value = "2016-01-02T01:01:01Z/2016-01-03T23:59:59.999Z";
        IntervalConstant interval = IntervalConstant.parse(value);
        Assertions.assertEquals(value, interval.toUrl());
    }

    @Test
    void testDurationConstant() {
        String value = "P1DT1H";
        String expected = "duration'" + value + "'";
        DurationConstant duration = DurationConstant.parse(value);
        Assertions.assertEquals(expected, duration.toUrl());
    }

}
