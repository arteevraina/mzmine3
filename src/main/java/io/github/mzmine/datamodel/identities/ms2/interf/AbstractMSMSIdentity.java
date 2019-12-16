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
package io.github.mzmine.datamodel.identities.ms2.interf;

import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;

public abstract class AbstractMSMSIdentity {

  // the mz tolerance that was used to find identity
  protected MZTolerance mzTolerance;

  public AbstractMSMSIdentity(MZTolerance mzTolerance) {
    this.mzTolerance = mzTolerance;
  }

  public abstract String getName();

  /**
   * the mz tolerance that was used to find identity
   * 
   * @return
   */
  public MZTolerance getMzTolerance() {
    return mzTolerance;
  }

  /**
   * the mz tolerance that was used to find identity
   * 
   * @param mzTolerance
   */
  public void setMzTolerance(MZTolerance mzTolerance) {
    this.mzTolerance = mzTolerance;
  }
}
