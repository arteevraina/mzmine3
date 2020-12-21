/*
 * Copyright 2006-2020 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.gui.chartbasics.gui.javafx.template;

import io.github.mzmine.gui.chartbasics.gui.javafx.template.providers.ColorProvider;
import io.github.mzmine.gui.chartbasics.gui.javafx.template.providers.DomainValueProvider;
import io.github.mzmine.gui.chartbasics.gui.javafx.template.providers.PlotDatasetProvider;
import io.github.mzmine.gui.chartbasics.gui.javafx.template.providers.RangeValueProvider;
import io.github.mzmine.gui.chartbasics.gui.javafx.template.providers.SeriesKeyProvider;

public class ColoredXYZDataset extends ColoredXYDataset {

  public ColoredXYZDataset(DomainValueProvider domainValueProvider,
      RangeValueProvider rangeValueProvider,
      SeriesKeyProvider<Comparable<?>> seriesKeyProvider,
      ColorProvider colorProvider) {
    super(domainValueProvider, rangeValueProvider, seriesKeyProvider, colorProvider);
  }

  public ColoredXYZDataset(PlotDatasetProvider datasetProvider) {
    super(datasetProvider);
  }


}
