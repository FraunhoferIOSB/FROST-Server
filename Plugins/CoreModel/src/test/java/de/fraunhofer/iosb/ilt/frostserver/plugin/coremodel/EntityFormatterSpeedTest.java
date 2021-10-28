package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

///*
// * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
// * Karlsruhe, Germany.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package de.fraunhofer.iosb.ilt.frostserver.serialize;
//
//import de.fraunhofer.iosb.ilt.frostserver.json.serialize.JsonWriter;
//import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
//import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
//import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
//import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
//import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
//import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
//import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
//import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
//import de.fraunhofer.iosb.ilt.frostserver.path.Version;
//import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
//import de.fraunhofer.iosb.ilt.frostserver.query.Query;
//import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
//import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
//import de.fraunhofer.iosb.ilt.frostserver.util.TestHelper;
//import java.io.IOException;
//import java.math.BigDecimal;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.slf4j.LoggerFactory;
//
///**
// *
// * @author scf
// */
//public class EntityFormatterSpeedTest {
//
//    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EntityFormatterSpeedTest.class);
//
//    private static CoreSettings coreSettings;
//    private static QueryDefaults queryDefaults;
//
//    @BeforeClass
//    public static void initClass() {
//        if (queryDefaults == null) {
//            coreSettings = new CoreSettings();
//            queryDefaults = coreSettings.getQueryDefaults();
//            queryDefaults.setUseAbsoluteNavigationLinks(true);
//        }
//    }
//
//    @Test
//    public void writeObservationSpeedTest() throws IOException {
//
//        ResourcePath path = PathParser.parsePath("http://example.org", Version.V_1_0, "/Observations(1)");
//        Query query = QueryParser.parseQuery("", coreSettings, path)
//                .validate();
//
//        int count = 1000;
//        EntitySet thingsOld = createObservations(query, count);
//        EntitySet<DefaultEntity> thingsNew = createObservationsNew(query, count);
//
//        for (int o = 0; o < 20; o++) {
//            long timeOld = runTest(thingsOld);
//            long timeNew = runTest(thingsNew);
//            long diff = 100 * timeNew / timeOld;
//            LOGGER.info("Old: {}ms, New: {}ms --> {}%", timeOld, timeNew, diff);
//        }
//    }
//
//    private EntitySet createObservations(Query query, int count) {
//        EntitySet things = new EntitySetImpl<>(EntityType.OBSERVATION);
//        for (int i = 0; i < count; i++) {
//            DefaultEntity entity = new DefaultEntity(EntityType.OBSERVATION)
//                    .setQuery(query)
//                    .setId(new IdLong(i))
//                    .setProperty(EntityPropertyMain.PHENOMENONTIME,TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59))
//                    .setProperty(EntityPropertyMain.RESULTTIME,TestHelper.createTimeInstantUTC(2014, 12, 31, 19, 59, 59))
//                    .setProperty(EntityPropertyMain.RESULT,new BigDecimal("70.40"));
//            things.add(entity);
//        }
//        return things;
//    }
//
//    private EntitySet<DefaultEntity> createObservationsNew(Query query, int count) {
//        EntitySet<DefaultEntity> things = new EntitySetImpl<>(EntityType.OBSERVATION);
//        for (int i = 0; i < count; i++) {
//            DefaultEntity entity = new DefaultEntity(EntityType.OBSERVATION)
//                    .setQuery(query)
//                    .setProperty(EntityPropertyMain.ID, new IdLong(i))
//                    .setProperty(EntityPropertyMain.PHENOMENONTIME, TestHelper.createTimeInstantUTC(2014, 12, 31, 11, 59, 59))
//                    .setProperty(EntityPropertyMain.RESULTTIME, TestHelper.createTimeInstantUTC(2014, 12, 31, 19, 59, 59))
//                    .setProperty(EntityPropertyMain.RESULT, new BigDecimal("70.40"));
//            things.add(entity);
//        }
//        return things;
//    }
//
//    private long runTest(EntitySet things) throws IOException {
//        long startTime = System.currentTimeMillis();
//        JsonWriter.writeEntityCollection(things);
//        long endTime = System.currentTimeMillis();
//        long time = endTime - startTime;
//        return time;
//    }
//}
