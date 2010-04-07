package com.healthmarketscience.jackcess;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author Dan Rollo
 *         Date: Mar 5, 2010
 *         Time: 12:44:21 PM
 */
public final class JetFormatTest extends TestCase {

    private static final File DIR_TEST_DATA = new File("test/data");

    /**
     * Defines known valid db test file base names.
     */
    public static enum Basename {

        BIG_INDEX("bigIndexTest"),
        COMP_INDEX("compIndexTest"),
        DEL_COL("delColTest"),
        DEL("delTest"),
        FIXED_NUMERIC("fixedNumericTest"),
        FIXED_TEXT("fixedTextTest"),
        INDEX_CURSOR("indexCursorTest"),
        INDEX("indexTest"),
        OVERFLOW("overflowTest"),
        QUERY("queryTest"),
        TEST("test"),
        TEST2("test2"),
        INDEX_CODES("testIndexCodes"),
        INDEX_PROPERTIES("testIndexProperties"),
        PROMOTION("testPromotion"),
        ;

        private final String _basename;

        Basename(String fileBasename) {
          _basename = fileBasename;
        }

        @Override
        public String toString() { return _basename; }
    }

    /** Defines currently supported db file formats. */
    final static Database.FileFormat[] SUPPORTED_FILEFORMATS = 
      new Database.FileFormat[] {
            Database.FileFormat.V2000,
            Database.FileFormat.V2003,
            Database.FileFormat.V2007,
    };

    /**
     * Defines known valid test database files, and their jet format version.
     */
    public static final class TestDB {

        private final File dbFile;
        private final Database.FileFormat expectedFileFormat;

        private TestDB(File databaseFile, 
                       Database.FileFormat expectedDBFileFormat) {

            dbFile = databaseFile;
            expectedFileFormat = expectedDBFileFormat;
        }

        public final File getFile() { return dbFile; }

        public final Database.FileFormat  getExpectedFileFormat() { 
          return expectedFileFormat; 
        }

        public final JetFormat getExpectedFormat() { 
          return expectedFileFormat.getJetFormat(); 
        }

        @Override
        public final String toString() {
          return "dbFile: " + dbFile.getAbsolutePath()
            + "; expectedFileFormat: " + expectedFileFormat;
        }

        public static List<TestDB> getSupportedForBasename(Basename basename) {

          List<TestDB> supportedTestDBs = new ArrayList<TestDB>();
          for (Database.FileFormat fileFormat : SUPPORTED_FILEFORMATS) {
            supportedTestDBs.add(new TestDB(
                                     getFileForBasename(basename, fileFormat),
                                     fileFormat));
          }
          return supportedTestDBs;
        }

        private static File getFileForBasename(
            Basename basename, Database.FileFormat fileFormat) {

            return new File(DIR_TEST_DATA, 
                            fileFormat.name() + File.separator +
                            basename + fileFormat.name() + 
                            fileFormat.getFileExtension());
        }
    }

    private static final File UNSUPPORTED_TEST_V1997 = 
      new File(DIR_TEST_DATA, "V1997" + File.separator +
               Basename.TEST + "V1997.mdb");

    static final List<TestDB> SUPPORTED_DBS_TEST = 
      TestDB.getSupportedForBasename(Basename.TEST);


    public void testGetFormat() throws Exception {
        try {
            JetFormat.getFormat(null);
            fail("npe");
        } catch (NullPointerException e) {
          // success
        }

        checkUnsupportedJetFormat(UNSUPPORTED_TEST_V1997);

        for (final TestDB testDB : SUPPORTED_DBS_TEST) {
          checkJetFormat(testDB);
        }
    }

    private static void checkJetFormat(final TestDB testDB)
            throws IOException {

        final FileChannel channel = Database.openChannel(testDB.dbFile, false);
        try {

            JetFormat fmtActual = JetFormat.getFormat(channel);
            assertEquals("Unexpected JetFormat for dbFile: " + 
                         testDB.dbFile.getAbsolutePath(),
                         testDB.expectedFileFormat.getJetFormat(), fmtActual);

        } finally {
            channel.close();
        }
    }

    private static void checkUnsupportedJetFormat(File testDB)
            throws IOException {

        final FileChannel channel = Database.openChannel(testDB, false);
        try {
            JetFormat.getFormat(channel);
            fail("Unexpected JetFormat for dbFile: " + 
                 testDB.getAbsolutePath());
        } catch(IOException ignored) {
          // success
        } finally {
            channel.close();
        }
    }

}