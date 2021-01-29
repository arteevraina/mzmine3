/*
 *  Copyright 2006-2020 The MZmine Development Team
 *
 *  This file is part of MZmine.
 *
 *  MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 *  General Public License as published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version.
 *
 *  MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 *  Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with MZmine; if not,
 *  write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 *  USA
 */

package tdfimportest;

import com.google.common.collect.Range;
import fxinitializer.InitJavaFX;
import io.github.mzmine.datamodel.Frame;
import io.github.mzmine.datamodel.IMSRawDataFile;
import io.github.mzmine.datamodel.MZmineProject;
import io.github.mzmine.datamodel.MobilityType;
import io.github.mzmine.modules.io.import_bruker_tdf.TDFImportTask;
import io.github.mzmine.project.impl.IMSRawDataFileImpl;
import io.github.mzmine.project.impl.MZmineProjectImpl;
import io.github.mzmine.taskcontrol.AbstractTask;
import io.github.mzmine.taskcontrol.TaskStatus;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class BrukerTdfTest {

  private static Logger logger = Logger.getLogger(BrukerTdfTest.class.getName());

  public static IMSRawDataFile importTestFile() throws IOException, InterruptedException {
    InitJavaFX.init();

    MZmineProject project = new MZmineProjectImpl();
    String str = BrukerTdfTest.class.getClassLoader()
        .getResource("rawdatafiles/200ngHeLaPASEF_2min_compressed.d").getFile();
    File file = new File(str);
    IMSRawDataFile rawDataFile = new IMSRawDataFileImpl(file.getName(), Color.BLACK);

    AtomicReference<TaskStatus> status = new AtomicReference<>(TaskStatus.WAITING);

    AbstractTask importTask = new TDFImportTask(project, file, rawDataFile);
    importTask.addTaskStatusListener((task, newStatus, oldStatus) -> {
      status.set(newStatus);
    });

    Thread thread = new Thread(importTask);
    thread.start();

    Date start = new Date();
    logger.info("Waiting for file import.");
    while (status.get() != TaskStatus.FINISHED) {
      TimeUnit.SECONDS.sleep(1);
      if (status.get() == TaskStatus.ERROR || status.get() == TaskStatus.CANCELED) {
        Assert.fail();
      }
    }
    Date end = new Date();
    logger.info("TDF import took " + ((end.getTime() - start.getTime()) / 1000) + " seconds");
    logger.info("Compare to 19 seconds on NVME SSD.");

    return rawDataFile;
  }

  @Test
  public void testFile() {
    IMSRawDataFile file = null;
    try {
      file = importTestFile();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    } catch (InterruptedException e) {
      e.printStackTrace();
      Assert.fail();
    }

    Assert.assertEquals(1430, file.getNumberOfFrames());
    Assert.assertEquals(MobilityType.TIMS,
        file.getFrame(0).getMobilityScans().get(0).getMobilityType());
    Assert.assertEquals(MobilityType.TIMS,
        file.getFrame(0).getMobilityScans().get(0).getMobilityType());
    Assert.assertEquals(671, file.getFrame(507).getMobilityScans().size());

    Frame frame18 = file.getFrame(17);
    Assert.assertEquals(21616, frame18.getNumberOfDataPoints());
    Assert.assertEquals(599.3259, frame18.getBasePeakMz(), 0.001d);
    Assert.assertEquals(555437, frame18.getBasePeakIntensity(), 1d);
    Assert.assertEquals((double) 3.0823007E7, frame18.getTIC(), 2d);
    Assert.assertEquals(40.044052, frame18.getRetentionTime(), 0.00001f);
    Assert.assertEquals(Range.closed(100d, 1700d), frame18.getScanningMZRange());
  }
}
