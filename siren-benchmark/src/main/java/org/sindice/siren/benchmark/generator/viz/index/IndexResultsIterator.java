package org.sindice.siren.benchmark.generator.viz.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;
import org.sindice.siren.benchmark.generator.viz.ResultsIterator;
import org.sindice.siren.benchmark.generator.viz.VizException;

public class IndexResultsIterator
extends ResultsIterator {

  private final static String TIME_LOGS_DIR  = "time-logs";
  private final static String INDEX_DIR      = "index";

  private boolean             isNewDirectory;
  private File[]              timeLogsFiles;
  private File[]              indexFiles;

  private final static String COMMIT_OUT     = "commit.out";
  private final static String OPTIMISE_OUT   = "optimise.out";

  final static String         DOC_EXTENSION  = ".doc";
  final static String         SKIP_EXTENSION = ".skp";
  final static String         NOD_EXTENSION  = ".nod";
  final static String         POS_EXTENSION  = ".pos";

  @Override
  public void init(File directory) {
    super.init(directory);
    isNewDirectory = true;
  }

  @Override
  public boolean hasNext() {
    if (isNewDirectory) {
      final File timeLogsDir = new File(directory, TIME_LOGS_DIR);
      final File indexDir = new File(directory, INDEX_DIR);
      // Check that both results folder exists
      checkDirectoriesExists(indexDir, timeLogsDir);
      // Check that TIME_LOGS_DIR contains the expected files
      try {
        timeLogsFiles = timeLogsDir.getCanonicalFile().listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.equals(COMMIT_OUT) || name.equals(OPTIMISE_OUT);
          }
        });
        if (timeLogsFiles == null || timeLogsFiles.length != 2) {
          logger.error("Missing either commit.out or optimise.out, or both: {}", timeLogsDir);
        }
        checkFilesExists(timeLogsFiles);
        // Check that INDEX_DIR contains the expected files
        indexFiles = indexDir.getCanonicalFile().listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith(DOC_EXTENSION) || name.endsWith(SKIP_EXTENSION) ||
                   name.endsWith(NOD_EXTENSION) || name.endsWith(POS_EXTENSION);
          }
        });
        if (indexFiles == null || indexFiles.length != 4) {
          logger.error("Missing index files: {}", indexDir);
        }
        checkFilesExists(indexFiles);
      } catch (IOException e) {
        logger.error("", e);
        throw new VizException(e);
      }
      return true;
    }
    return false;
  }

  @Override
  public BenchmarkResults next() {
    final IndexBenchmarkResults res = new IndexBenchmarkResults();

    res.setDirectoryName(directory.getName());
    isNewDirectory = false;
    try {
      getIndexTimes(res);
      getIndexFilesSize(res);
    } catch (IOException e) {
      logger.error("", e);
      throw new VizException(e);
    }
    return res;
  }

  private void getIndexTimes(IndexBenchmarkResults res)
  throws IOException {
    for (File time: timeLogsFiles) {
      logger.info("Processing file: {}", time);
      final BufferedReader r = new BufferedReader(new FileReader(time));
      if (time.getName().equals(COMMIT_OUT)) {
        double commitTime = 0;
        String line;
        while ((line = r.readLine()) != null) {
          commitTime += Double.valueOf(line);
        }
        res.setCommitTime(commitTime);
      } else if (time.getName().equals(OPTIMISE_OUT)) {
        res.setOptimiseTime(Double.valueOf(r.readLine()));
      } else {
        r.close();
        // Should not happen
        throw new VizException();
      }
      r.close();
    }
  }

  private void getIndexFilesSize(IndexBenchmarkResults res)
  throws IOException {
    for (File iFile: indexFiles) {
      logger.info("Processing file: {}", iFile);
      if (iFile.getName().endsWith(DOC_EXTENSION)) {
        res.setDocSizeInBytes(iFile.length());
      } else if (iFile.getName().endsWith(NOD_EXTENSION)) {
        res.setNodSizeInBytes(iFile.length());
      } else if (iFile.getName().endsWith(SKIP_EXTENSION)) {
        res.setSkpSizeInBytes(iFile.length());
      } else if (iFile.getName().endsWith(POS_EXTENSION)) {
        res.setPosSizeInBytes(iFile.length());
      } else {
        // Should not happen
        throw new VizException();
      }
    }
  }

}
