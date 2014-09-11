/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.nioneo.store;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.mockfs.EphemeralFileSystemAbstraction;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.kernel.DefaultIdGeneratorFactory;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.pagecache.StandalonePageCacheFactory;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.kernel.lifecycle.LifeSupport;
import org.neo4j.kernel.monitoring.Monitors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import static org.neo4j.helpers.collection.MapUtil.stringMap;
import static org.neo4j.kernel.impl.nioneo.store.CommonAbstractStore.Configuration;

public class StoreFactoryTest
{
    private LifeSupport life;
    private StoreFactory storeFactory;

    @Before
    public void setup()
    {
        FileSystemAbstraction fs = new EphemeralFileSystemAbstraction();
        Map<String, String> configParams = stringMap(
                Configuration.read_only.name(), "false",
                GraphDatabaseSettings.neo_store.name(), "graph.db/neostore" );
        life = new LifeSupport();
        life.start();
        PageCache pageCache = StandalonePageCacheFactory.createPageCache( fs, getClass().getName(), life );

        storeFactory = new StoreFactory( new Config( configParams ), new DefaultIdGeneratorFactory(),
                pageCache, fs, StringLogger.DEV_NULL, new Monitors() );
    }

    @After
    public void teardown()
    {
        life.shutdown();
    }

    @Test
    public void shouldHaveSameCreationTimeAndUpgradeTimeOnStartup() throws Exception
    {
        // When
        NeoStore neostore = storeFactory.createNeoStore();

        // Then
        assertThat( neostore.getUpgradeId(), equalTo( neostore.getRandomNumber() ) );
        assertThat( neostore.getUpgradeTime(), equalTo( neostore.getCreationTime() ) );
    }

}
