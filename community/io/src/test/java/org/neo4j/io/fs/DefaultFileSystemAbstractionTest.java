/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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
package org.neo4j.io.fs;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.neo4j.graphdb.mockfs.CloseTrackingFileSystem;
import org.neo4j.io.fs.watcher.FileWatcher;
import org.neo4j.test.rule.TestDirectory;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DefaultFileSystemAbstractionTest
{
    @Rule
    public TestDirectory testDirectory = TestDirectory.testDirectory();

    private DefaultFileSystemAbstraction defaultFileSystemAbstraction;
    private File path;

    @Before
    public void before() throws Exception
    {
        path = testDirectory.file( "testFile" );
        defaultFileSystemAbstraction = new DefaultFileSystemAbstraction();
    }

    @After
    public void tearDown() throws IOException
    {
        defaultFileSystemAbstraction.close();
    }

    @Test
    public void fileWatcherCreation() throws IOException
    {
        try (FileWatcher fileWatcher = defaultFileSystemAbstraction.fileWatcher())
        {
            assertNotNull( fileWatcher.watch( testDirectory.directory( "testDirectory" ) ) );
        }
    }

    @Test
    public void shouldCreatePath() throws Exception
    {
        defaultFileSystemAbstraction.mkdirs( path );

        assertThat( path.exists(), is( true ) );
    }

    @Test
    public void shouldCreateDeepPath() throws Exception
    {
        path = new File( path, UUID.randomUUID() + "/" + UUID.randomUUID() );

        defaultFileSystemAbstraction.mkdirs( path );

        assertThat( path.exists(), is( true ) );
    }

    @Test
    public void shouldCreatePathThatAlreadyExists() throws Exception
    {
        assertTrue( path.mkdir() );

        defaultFileSystemAbstraction.mkdirs( path );

        assertThat( path.exists(), is( true ) );
    }

    @Test
    public void shouldCreatePathThatPointsToFile() throws Exception
    {
        assertTrue( path.mkdir() );
        path = new File( path, "some_file" );
        assertTrue( path.createNewFile() );

        defaultFileSystemAbstraction.mkdirs( path );

        assertThat( path.exists(), is( true ) );
    }

    @Test
    public void shouldFailGracefullyWhenPathCannotBeCreated() throws Exception
    {
        path = new File( testDirectory.directory(), String.valueOf( UUID.randomUUID() ) )
        {
            @Override
            public boolean mkdirs()
            {
                return false;
            }
        };

        try
        {
            defaultFileSystemAbstraction.mkdirs( path );

            fail();
        }
        catch ( IOException e )
        {
            assertThat( path.exists(), is( false ) );
            assertThat( e.getMessage(), is( String.format( DefaultFileSystemAbstraction
                    .UNABLE_TO_CREATE_DIRECTORY_FORMAT, path ) ) );
        }
    }

    @Test
    public void closeThirdPartyFileSystemsOnClose() throws IOException
    {
        CloseTrackingFileSystem closeTrackingFileSystem = new CloseTrackingFileSystem();

        CloseTrackingFileSystem fileSystem = defaultFileSystemAbstraction
                .getOrCreateThirdPartyFileSystem( CloseTrackingFileSystem.class,
                        thirdPartyFileSystemClass -> closeTrackingFileSystem );

        assertSame( closeTrackingFileSystem, fileSystem );
        assertFalse( closeTrackingFileSystem.isClosed() );

        defaultFileSystemAbstraction.close();

        assertTrue( closeTrackingFileSystem.isClosed() );
    }
}
