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


package io.github.mzmine.modules.dataprocessing.id_cliquems.cliquemsimplementation;

import io.github.mzmine.modules.dataprocessing.id_cliquems.CliqueMSTask;
import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import org.apache.commons.lang3.mutable.MutableDouble;

/**
 * Main class for computing isotopes provided anClique object whose cliques have been already
 * computed, if not the algorithm considers each node as 1 clique and compute isotopes.
 *
 * See https://github.com/osenan/cliqueMS/blob/master/R/findIsotopes.R for R code corresponding to
 * this class
 */
public class ComputeIsotopesModule {

  private final Logger logger = Logger.getLogger(getClass().getName());

  private final CliqueMSTask driverTask;
  private final AnClique anClique;
  private MutableDouble progress;

  public ComputeIsotopesModule(AnClique anClique, CliqueMSTask task, MutableDouble progress){
    this.anClique = anClique;
    this.driverTask = task;
    this.progress = progress;
  }



  private List<Integer> findBadFeatures(List<Integer> feature, List<Double> weights){
    // Drop one parental mass when one isotope has two parental masses candidates
    // This code corresponds to the filterInlinks or filterOutLinks function in the R code
    HashMap <Integer,Integer> IFeatureHash = new HashMap<>();
    List<Integer> duplicateIFindex = new ArrayList<>();//this contains indices to be deleted from all features
    for(int i = 0 ; i<feature.size() ; i++){
      if(IFeatureHash.containsKey(feature.get(i))){
        Integer duplicateFeature = feature.get(i);
        //The indices of the one with smaller weight is added to duplicateIFindex to be deleted
        if(weights.get(IFeatureHash.get(duplicateFeature)) < weights.get(i)){
          duplicateIFindex.add( IFeatureHash.get(duplicateFeature));
          IFeatureHash.put(duplicateFeature,i);  // drop the parental feature with less weight
        }
        else{
          duplicateIFindex.add(i);  // drop the parental feature with less weight
        }
      }
      else{
        IFeatureHash.put(feature.get(i),i);
      }
    }
    return duplicateIFindex;
  }

  /**
   * Function to filter isotopes feature vectors, and create network of isotope TODO check this
   *
   * @param pFeature
   * @param iFeature
   * @param pCharge
   * @param icharge
   */
  private void filterIso(List<Integer> pFeature, List<Integer> iFeature, List<Integer> pCharge, List<Integer> icharge){
    //Find node corresponding pFeature and iFeature from network
    NetworkCliqueMS network = this.anClique.getNetwork();
    List<Double> weights = new ArrayList<>();
    for(int i = 0 ; i< pFeature.size() ; i++){
      Integer first = pFeature.get(i), second = iFeature.get(i);
      //if first>=second, revert as edge list in network only contains only edges with nodes such that first < second
      if(first >= second){
        Integer temp = first;
        first = second;
        second = temp;
      }
      weights.add(network.getEdges().get(new Pair(first,second)));
    }

    // First filter isotopes pointing to two different parents
    List<Integer> deletepos = findBadFeatures(iFeature, weights);
    // Second filter parents pointed by two different isotopes
    deletepos.addAll(findBadFeatures(pFeature,weights));
    Collections.sort(deletepos, Collections.reverseOrder());
    //removing the indices
    for(Integer index : deletepos){
      pFeature.remove((int)index);
      iFeature.remove((int)index);
      pCharge.remove((int)index);
      icharge.remove((int)index);
      weights.remove((int)index);
    }

    // Third filter inconsistency in charge
    deletepos.clear();
    for(int i=0; i<pFeature.size() ; i++){
      for(int j=0; j<iFeature.size() ; j++){
        if(pFeature.get(i).equals(iFeature.get(j))){
          //If same node in iFeature and pFeature, they must have same charge
          if(pCharge.get(i).equals(icharge.get(j))){
            deletepos.add(i);
            deletepos.add(j);
          }
          break;
        }
      }
    }
  }

  /**
   * Function to grade and isotope, starting from 0 to the parental isotope, 1 the first isotope and
   * further.
   * The algorithm was implemented in R through igraph, this is the equivalent algorithm. The igraph
   * was directed, unweighted and iLinks and outLinks correspond to that graph network.
   * @param inLinks inLink to the graph
   * @param outLinks outLinks to the graph
   * @return int[][] nx2 dimension matrix, 1st dimension carries nodeID and 2nd carries grade of the node
   */
  private int[][] isoGrade(List<Integer> inLinks, List<Integer> outLinks){
    int [][]gradeData = new int[2*inLinks.size()][2]; // 1st dimension carries node ID, 2nd carries grade
    int pos = 0;
    for(int i=0;i<inLinks.size();i++){
      gradeData[pos++][0] = inLinks.get(i);
      gradeData[pos++][0] = outLinks.get(i);
    }

    for(int i=0;i<gradeData.length ; i++){
      int res = 0;
      if(inLinks.contains(gradeData[i][0])) {
        int nei = outLinks.get(inLinks.indexOf(gradeData[i][0]));
        while (true) {
          res++;
          if (!inLinks.contains(nei)) {
            break;
          }
          nei = outLinks.get(inLinks.indexOf(nei));
        }
      }
      gradeData[i][1] = res;
    }
    return gradeData;
  }

  /**
   * function to set the node attributes for each isotope: grade, charge, and community (or cluster)
   * @param pFeature
   * @param iFeature
   * @param pCharge
   * @param iCharge
   * @param maxGrade
   */
  private IsoTable isonetAttributes(List<Integer> pFeature, List<Integer> iFeature, List<Integer> pCharge, List<Integer> iCharge, int maxGrade){
    // First assign grade (for info look isoGrade function)
    int [][] grade = isoGrade(iFeature,pFeature);

    List<Integer> nodes = new ArrayList<>(); //get all nodes IDs from the grade matrix
    HashSet<Integer> nodesSet = new HashSet<>();
    Hashtable<Integer,Integer> gradeHash = new Hashtable<>();
    for(int i=0;i<grade.length;i++){
      nodesSet.add(grade[i][0]);
      gradeHash.put(grade[i][0],grade[i][1]);
    }
    nodes.addAll(nodesSet);

    List<Integer> grades = new ArrayList<>(); // get all grades corresponding to the nodeID
    for(int i = 0;i < nodes.size() ; i++){
      grades.add(gradeHash.get(nodes.get(i)));
    }


    List<Integer> charge = new ArrayList<>();
    for(int i=0;i<nodes.size() ; i++){
      int res;
      if(pFeature.contains(nodes.get(i))){
        res = pCharge.get(pFeature.indexOf(nodes.get(i)));
      }
      else{
        res = iCharge.get(iFeature.indexOf(nodes.get(i)));
      }
      charge.add(res);
    }

    // label features that belong to the same isotope cluster
    List<Integer> cluster = new ArrayList<>();
    for (int i=0; i < nodes.size(); i++) {
      cluster.add(0);
    }
    int clusterID = 1;
    HashSet<Integer> clusteredNodes = new HashSet<>();
    for(Integer x : iFeature){
      if(clusteredNodes.contains(x))
        continue;
      cluster.set(nodes.indexOf(x),clusterID);
      clusteredNodes.add(x);
      Integer nei = pFeature.get(iFeature.indexOf(x));
      cluster.set(nodes.indexOf(nei),clusterID);
      clusteredNodes.add(nei);
      while(iFeature.contains(nei)){
        nei = pFeature.get(iFeature.indexOf(nei));
        cluster.set(nodes.indexOf(nei),clusterID);
        clusteredNodes.add(nei);
      }
      clusterID++;
    }

    //correct the grade if grade is greater than maxGrade
    while(Collections.max(grades) > maxGrade) {
      // The following code corresponds to correctGrade in R code
      int maxCluster = Collections.max(cluster);
      HashSet<Integer> uniqueCluster = new HashSet<>(cluster); // unique cluster
      for (Integer clus : uniqueCluster) {
        List<Integer> clusterIndices = new ArrayList<>();
        for(int i = 0; i < cluster.size() ; i++) {
          if(clus.equals(cluster.get(i)))
            clusterIndices.add(i);
        }
        List<Integer> badGradeIndex = new ArrayList<>();
        for (int i : clusterIndices) {
          if (grades.get(i) > maxGrade) {
            badGradeIndex.add(i);
          }
        }
        if (badGradeIndex.size() > 1) {
          int gradeVal = 0;
          for (int index : badGradeIndex) {
            cluster.set(index, maxCluster + badGradeIndex.size() + 1);
            grades.set(index, gradeVal++);
          }
        }
      }
    }
    IsoTable isoTable = new IsoTable();
    isoTable.charge = charge;
    isoTable.cluster = cluster;
    isoTable.feature = nodes;
    isoTable.grade = grades;
    return isoTable;
  }


  private List<IsoTable> computelistofIsoTable(int maxCharge, int maxGrade, MZTolerance isoMZTolerance, double isom){
    List<PeakData> pdList = anClique.getPeakList();
    HashMap<Integer,PeakData> pdHash = new HashMap<>();
    List<IsoTable> listofIsoTable =new ArrayList<>();
    for(PeakData pd : pdList){
      pdHash.put(pd.getNodeID(),pd);
    }
    int done = 0 ;
    for(Integer cliqueID : this.anClique.cliques.keySet()){
      done++ ;
      this.progress.setValue(driverTask.EIC_PROGRESS + driverTask.MATRIX_PROGRESS +
          driverTask.NET_PROGRESS + driverTask.ISO_PROGRESS *((double) done /(double) this.anClique.cliques.size()));

      if(driverTask.isCanceled()){
        return listofIsoTable;
      }
      List<Pair<Double, Pair<Double,Integer>>> inData = new ArrayList<>(); // contains following data -> intensity, mz value, nodeID
      for(Integer cliquenodeID : this.anClique.cliques.get(cliqueID)){
        PeakData pd = pdHash.get(cliquenodeID);
        Pair<Double,Integer> p = new Pair(pd.getMz(),pd.getNodeID());
        Pair<Double,Pair<Double,Integer>> isoInput = new Pair(pd.getIntensity(),p);
        inData.add(isoInput);
      }

      inData.sort((o1, o2) -> Double.compare(o2.getKey(), o1.getKey()));

      IsotopeAnCliqueMS an = new IsotopeAnCliqueMS(inData);
      an.getIsotopes(maxCharge,isoMZTolerance,isom);
      IsoTable iTable = null;
      if(an.getPfeature().size() > 0){
        // filter the isotope list by charge and other inconsistencies
        filterIso(an.getPfeature(),an.getIfeature(),an.getPcharge(),an.getIcharge());
        if(an.getPfeature().size() > 0){
          iTable = isonetAttributes(an.getPfeature(),an.getIfeature(),an.getPcharge(),an.getIcharge(),maxGrade);
        }
      }
      if(iTable != null)
        listofIsoTable.add(iTable);
    }
    return listofIsoTable;
  }

  /**
   *
   * @param maxCharge
   * @param maxGrade
   * @param isoMZTolerance
   * @param isom
   */
  public void getIsotopes(int maxCharge, int maxGrade, MZTolerance isoMZTolerance, double isom){
    if(!anClique.cliquesFound){
      logger.log(Level.WARNING,"Cliques have not been computed for this object. This could lead"
          + " to long computing times for isotope annotation.");
    }
    if(anClique.isoFound){
      logger.log(Level.WARNING,"Isotopes have been already computed for this object");
    }

    logger.log(Level.INFO,"Computing Isotopes");
    List<IsoTable> isoTableList = computelistofIsoTable(maxCharge, maxGrade, isoMZTolerance, isom);
    if(driverTask.isCanceled()){
      return;
    }
    List<String> isoLabel = new ArrayList<>();
    //The cluster label is inconsistent between all isotopes found let's correct for avoiding confusions
    int maxC = Collections.max(isoTableList.get(0).cluster);
    for(int i = 1;i<isoTableList.size() ; i++){
      for(int j = 0; j < isoTableList.get(i).cluster.size() ; j++ ){
        isoTableList.get(i).cluster.set(j,isoTableList.get(i).cluster.get(j)+maxC + 1);
      }
      maxC = Collections.max(isoTableList.get(i).cluster);
    }
    // give isotope labels
    Hashtable<Integer,Integer> nodeIDtoindexHash = new Hashtable<>();
    for(int i = 0 ; i < this.anClique.getPeakList().size() ; i++){
      PeakData pd = this.anClique.getPeakList().get(i);
      isoLabel.add("M0"); // give MO label to every peak
      nodeIDtoindexHash.put(pd.getNodeID(),i);
    }

    for(IsoTable isoTable : isoTableList){
      for(int i = 0 ; i < isoTable.feature.size() ; i++){
        String label = "M"+isoTable.grade.get(i)+"["+isoTable.cluster.get(i)+"]";
        isoLabel.set(nodeIDtoindexHash.get(isoTable.feature.get(i)), label);
      }
    }

    for(int i = 0; i < isoLabel.size() ; i++){
      PeakData pd = this.anClique.getPeakList().get(i);
      pd.setIsotopeAnnotation(isoLabel.get(i));
    }
    this.anClique.isoFound = true;
  }
  public void getIsotopes(){
    getIsotopes(3,2,new MZTolerance(0,10),1.003355);
  }


  class IsoTable{
    List<Integer> feature;
    List<Integer> charge;
    List<Integer> grade;
    List<Integer> cluster;
  }
}
