/*
 * (C) Copyright 2015-2017 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

package io.github.mzmine.modules.io.rawdataimport.fileformats.mzml_msdk.msdk.data;

import com.google.common.collect.Range;
import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.ActivationInfo;
import io.github.msdk.datamodel.ActivationType;
import io.github.msdk.datamodel.Chromatogram;
import io.github.msdk.datamodel.ChromatogramType;
import io.github.msdk.datamodel.IonAnnotation;
import io.github.msdk.datamodel.IsolationInfo;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.datamodel.SeparationType;
import io.github.msdk.datamodel.SimpleActivationInfo;
import io.github.msdk.datamodel.SimpleIsolationInfo;
import io.github.mzmine.modules.io.rawdataimport.fileformats.mzml_msdk.msdk.MzMLFileImportMethod;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MzMLChromatogram implements Chromatogram {

  private final @Nonnull
  MzMLRawDataFile dataFile;
  private @Nonnull InputStream inputStream;
  private final @Nonnull String chromatogramId;
  private final @Nonnull Integer chromatogramNumber;
  private final @Nonnull Integer numOfDataPoints;

  private MzMLCVGroup cvParams;
  private MzMLPrecursorElement precursor;
  private MzMLProduct product;
  private MzMLBinaryDataInfo rtBinaryDataInfo;
  private MzMLBinaryDataInfo intensityBinaryDataInfo;
  private ChromatogramType chromatogramType;
  private Double mz;
  private SeparationType separationType;
  private Range<Float> rtRange;
  private float[] rtValues;
  private float[] intensityValues;

  private Logger logger = LoggerFactory.getLogger(MzMLFileImportMethod.class);

  /**
   * <p>
   * Constructor for {@link MzMLChromatogram MzMLChromatogram}
   * </p>
   * 
   * @param dataFile a {@link MzMLRawDataFile MzMLRawDataFile} object
   *        the parser stores the data in
   * @param is an {@link InputStream InputStream} of the MzML format data
   * @param chromatogramId the Chromatogram ID
   * @param chromatogramNumber the Chromatogram number
   * @param numOfDataPoints the number of data points in the retention time and intensity arrays
   */
  MzMLChromatogram(@Nonnull MzMLRawDataFile dataFile, InputStream is, String chromatogramId,
      Integer chromatogramNumber, Integer numOfDataPoints) {
    this.cvParams = new MzMLCVGroup();
    this.dataFile = dataFile;
    this.inputStream = is;
    this.chromatogramId = chromatogramId;
    this.chromatogramNumber = chromatogramNumber;
    this.numOfDataPoints = numOfDataPoints;
    this.separationType = SeparationType.UNKNOWN;
    this.rtBinaryDataInfo = null;
    this.intensityBinaryDataInfo = null;
    this.chromatogramType = ChromatogramType.UNKNOWN;
    this.mz = null;
    this.rtRange = null;
    this.rtValues = null;
    this.intensityValues = null;

  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public RawDataFile getRawDataFile() {
    return dataFile;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Integer getChromatogramNumber() {
    return chromatogramNumber;
  }

  /**
   * <p>
   * getCVParams.
   * </p>
   *
   * @return a {@link ArrayList} object.
   */
  public MzMLCVGroup getCVParams() {
    return cvParams;
  }

  /**
   * <p>
   * Getter for the field <code>mzBinaryDataInfo</code>.
   * </p>
   *
   * @return a {@link MzMLBinaryDataInfo} object.
   */
  public MzMLBinaryDataInfo getRtBinaryDataInfo() {
    return rtBinaryDataInfo;
  }

  /**
   * <p>
   * Setter for the field <code>mzBinaryDataInfo</code>.
   * </p>
   *
   * @param rtBinaryDataInfo a {@link MzMLBinaryDataInfo} object.
   */
  public void setRtBinaryDataInfo(MzMLBinaryDataInfo rtBinaryDataInfo) {
    this.rtBinaryDataInfo = rtBinaryDataInfo;
  }

  /**
   * <p>
   * Getter for the field <code>intensityBinaryDataInfo</code>.
   * </p>
   *
   * @return a {@link MzMLBinaryDataInfo} object.
   */
  public MzMLBinaryDataInfo getIntensityBinaryDataInfo() {
    return intensityBinaryDataInfo;
  }

  /**
   * <p>
   * Setter for the field <code>intensityBinaryDataInfo</code>.
   * </p>
   *
   * @param intensityBinaryDataInfo a {@link MzMLBinaryDataInfo} object.
   */
  public void setIntensityBinaryDataInfo(MzMLBinaryDataInfo intensityBinaryDataInfo) {
    this.intensityBinaryDataInfo = intensityBinaryDataInfo;
  }

  /**
   * <p>
   * getInputStream.
   * </p>
   *
   * @return a {@link io.github.msdk.io.mzml2.util.io.ByteBufferInputStream} object.
   */
  public InputStream getInputStream() {
    return inputStream;
  }

  /**
   * <p>
   * setInputStream.
   * </p>
   *
   * @param inputStream a {@link InputStream} object.
   */
  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * <p>
   * getPrecursor.
   * </p>
   *
   * @return a {@link MzMLPrecursorElement} object.
   */
  public MzMLPrecursorElement getPrecursor() {
    return precursor;
  }

  /**
   * <p>
   * Setter for the field <code>precursor</code>.
   * </p>
   *
   * @param precursor a {@link MzMLPrecursorElement} object.
   */
  public void setPrecursor(MzMLPrecursorElement precursor) {
    this.precursor = precursor;
  }

  /**
   * <p>
   * getProduct.
   * </p>
   *
   * @return a {@link MzMLProduct} object.
   */
  public MzMLProduct getProduct() {
    return product;
  }

  /**
   * <p>
   * Setter for the field <code>precursor</code>.
   * </p>
   *
   * @param product a {@link MzMLProduct} object.
   */
  public void setProdcut(MzMLProduct product) {
    this.product = product;
  }

  /**
   * <p>
   * Getter for the field <code>chromatogramId</code>.
   * </p>
   *
   * @return a {@link String} object.
   */
  public String getId() {
    return chromatogramId;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public ChromatogramType getChromatogramType() {
    if (chromatogramType != ChromatogramType.UNKNOWN)
      return chromatogramType;

    int count = 0;

    if (getCVValue(MzMLCV.cvChromatogramTIC).isPresent()) {
      chromatogramType = ChromatogramType.TIC;
      count++;
    }
    if (getCVValue(MzMLCV.cvChromatogramMRM_SRM).isPresent()) {
      chromatogramType = ChromatogramType.MRM_SRM;
      count++;
    }
    if (getCVValue(MzMLCV.cvChromatogramSIC).isPresent()) {
      chromatogramType = ChromatogramType.SIC;
      count++;
    }
    if (getCVValue(MzMLCV.cvChromatogramBPC).isPresent()) {
      chromatogramType = ChromatogramType.BPC;
      count++;
    }

    if (count > 1) {
      logger.error("More than one chromatogram type defined by CV terms not allowed");
      chromatogramType = ChromatogramType.UNKNOWN;
    }

    return chromatogramType;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Double getMz() {
    // Set mz value only if it hasn't been fetched before
    if (mz == null) {
      // Try getting the mz value from the product isolation window
      // set mz value to the respective cv value only if the value is present
      if (product != null) {
        if (product.getIsolationWindow().isPresent()) {
          Optional<String> cvval =
              getCVValue(product.getIsolationWindow().get(), MzMLCV.cvIsolationWindowTarget);
          if (cvval.isPresent())
            mz = Double.valueOf(cvval.get());
        }
      }
    }
    return mz;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public List<IsolationInfo> getIsolations() {
    if (getChromatogramType() == ChromatogramType.MRM_SRM) {

      Optional<MzMLIsolationWindow> precursorIsolationWindow = getPrecursor().getIsolationWindow();
      Optional<MzMLIsolationWindow> productIsolationWindow = getProduct().getIsolationWindow();

      if (!precursorIsolationWindow.isPresent()) {
        logger.error("Couldn't find precursor isolation window for chromotgram (#"
            + getChromatogramNumber() + ")");
        return Collections.emptyList();
      }

      if (!productIsolationWindow.isPresent()) {
        logger.error("Couldn't find product isolation window for chromotgram (#"
            + getChromatogramNumber() + ")");
        return Collections.emptyList();
      }

      Optional<String> precursorIsolationMz =
          getCVValue(precursorIsolationWindow.get(), MzMLCV.cvIsolationWindowTarget);
      Optional<String> precursorActivationEnergy =
          getCVValue(getPrecursor().getActivation(), MzMLCV.cvActivationEnergy);
      Optional<Integer> precursorScanNumber = getScanNumber(precursor.getSpectrumRef().orElse(""));
      Optional<String> productIsolationMz =
          getCVValue(productIsolationWindow.get(), MzMLCV.cvIsolationWindowTarget);
      ActivationType precursorActivation = ActivationType.UNKNOWN;
      ActivationInfo activationInfo = null;

      if (getCVValue(getPrecursor().getActivation(), MzMLCV.cvActivationCID).isPresent())
        precursorActivation = ActivationType.CID;

      if (precursorActivationEnergy != null) {
        activationInfo = new SimpleActivationInfo(Double.valueOf(precursorActivationEnergy.get()),
            precursorActivation);
      }

      List<IsolationInfo> isolations = new ArrayList<>();
      IsolationInfo isolationInfo = null;

      Integer precursorScanNumberInt =
          precursorScanNumber.isPresent() ? Integer.valueOf(precursorScanNumber.get()) : null;

      if (precursorIsolationMz.isPresent()) {
        isolationInfo =
            new SimpleIsolationInfo(Range.singleton(Double.valueOf(precursorIsolationMz.get())),
                null, Double.valueOf(precursorIsolationMz.get()), null, activationInfo,
                precursorScanNumberInt);
        isolations.add(isolationInfo);
      }

      if (productIsolationMz.isPresent()) {
        isolationInfo =
            new SimpleIsolationInfo(Range.singleton(Double.valueOf(productIsolationMz.get())), null,
                Double.valueOf(productIsolationMz.get()), null, null, null);
        isolations.add(isolationInfo);
      }

      return Collections.unmodifiableList(isolations);
    }

    return Collections.emptyList();
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public SeparationType getSeparationType() {
    return separationType;
  }

  /** {@inheritDoc} */
  @Override
  public IonAnnotation getIonAnnotation() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Integer getNumberOfDataPoints() {
    return numOfDataPoints;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public float[] getRetentionTimes(@Nullable float array[]) {
    if (rtValues == null) {
      if (getRtBinaryDataInfo().getArrayLength() != numOfDataPoints) {
        logger.warn(
            "Retention time binary data array contains a different array length from the default array length of the scan (#"
                + getChromatogramNumber() + ")");
      }

      try {
        rtValues = MzMLPeaksDecoder.decodeToFloat(inputStream, getRtBinaryDataInfo(), array);
      } catch (Exception e) {
        throw (new MSDKRuntimeException(e));
      }
    }

    if (array == null || array.length < numOfDataPoints) {
      array = new float[numOfDataPoints];

      System.arraycopy(rtValues, 0, array, 0, numOfDataPoints);
    }

    return array;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public float[] getIntensityValues(@Nullable float[] array) {
    if (intensityValues == null) {
      if (getIntensityBinaryDataInfo().getArrayLength() != numOfDataPoints) {
        logger.warn(
            "Intensity binary data array contains a different array length from the default array length of the chromatogram (#"
                + getChromatogramNumber() + ")");
      }

      try {
        intensityValues =
            MzMLPeaksDecoder.decodeToFloat(inputStream, getIntensityBinaryDataInfo(), array);
      } catch (Exception e) {
        throw (new MSDKRuntimeException(e));
      }
    }

    if (array == null || array.length < numOfDataPoints) {
      array = new float[numOfDataPoints];

      System.arraycopy(intensityValues, 0, array, 0, numOfDataPoints);
    }

    return array;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public double[] getMzValues(@Nullable double array[]) {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Range<Float> getRtRange() {
    if (rtRange == null) {
      float[] rtValues = getRetentionTimes();
      rtRange = Range.closed(rtValues[0], rtValues[numOfDataPoints - 1]);
    }
    return rtRange;
  }

  /**
   * <p>
   * Search for the CV Parameter value for the given accession in the
   * {@link Chromatogram Chromatogram}'s CV Parameters
   * </p>
   *
   * @param accession the CV Parameter accession as {@link String String}
   * @return an {@link Optional Optional<String>} containing the CV Parameter value for
   *         the given accession, if present <br>
   *         An empty {@link Optional Optional<String>} otherwise
   */
  public Optional<String> getCVValue(String accession) {
    return getCVValue(cvParams, accession);
  }

  /**
   * <p>
   * Search for the CV Parameter value for the given accession in the given
   * {@link MzMLCVGroup MzMLCVGroup}
   * </p>
   *
   * @param group the {@link MzMLCVGroup MzMLCVGroup} to search through
   * @param accession the CV Parameter accession as {@link String String}
   * @return an {@link Optional Optional<String>} containing the CV Parameter value for
   *         the given accession, if present <br>
   *         An empty {@link Optional Optional<String>} otherwise
   */
  public Optional<String> getCVValue(MzMLCVGroup group, String accession) {
    Optional<String> value;
    for (MzMLCVParam cvParam : group.getCVParamsList()) {
      if (cvParam.getAccession().equals(accession)) {
        value = cvParam.getValue();
        if (!value.isPresent())
          value = Optional.of("");
        return value;
      }
    }
    return Optional.empty();
  }

  /**
   * <p>
   * getScanNumber.
   * </p>
   *
   * @param spectrumId a {@link String} object.
   * @return a {@link Integer} object.
   */
  public Optional<Integer> getScanNumber(String spectrumId) {
    final Pattern pattern = Pattern.compile("scan=([0-9]+)");
    final Matcher matcher = pattern.matcher(spectrumId);
    boolean scanNumberFound = matcher.find();

    // Some vendors include scan=XX in the ID, some don't, such as
    // mzML converted from WIFF files. See the definition of nativeID in
    // http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo
    // So, get the value of the index tag if the scanNumber is not present in the ID
    if (scanNumberFound) {
      Integer scanNumber = Integer.parseInt(matcher.group(1));
      return Optional.ofNullable(scanNumber);
    }

    return Optional.ofNullable(null);
  }

}
