/*
 * Copyright 2006-2020 The MZmine Development Team
 * 
 * This file is part of MZmine.
 * 
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.modules.dataprocessing.filter_scanfilters.resample;

import java.awt.Window;

import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.dataprocessing.filter_scanfilters.ScanFilterSetupDialog;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.DoubleParameter;
import io.github.mzmine.util.ExitCode;

public class ResampleFilterParameters extends SimpleParameterSet {

  public static final DoubleParameter binSize = new DoubleParameter("m/z bin length",
      "The length of m/z bin", MZmineCore.getConfiguration().getMZFormat());

  public ResampleFilterParameters() {
    super(new Parameter[] {binSize});
  }

  public ExitCode showSetupDialog(Window parent, boolean valueCheckRequired) {
    ScanFilterSetupDialog dialog =
        new ScanFilterSetupDialog(parent, valueCheckRequired, this, ResampleFilter.class);
    dialog.setVisible(true);
    return dialog.getExitCode();
  }
}
