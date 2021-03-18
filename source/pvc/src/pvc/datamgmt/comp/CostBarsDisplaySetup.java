package pvc.datamgmt.comp;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import pvc.calc.TCOCalculator;
import pvc.datamgmt.*;

public class CostBarsDisplaySetup {
	public int legendFontSize, axesLineWidth, gridLineWidth, gridColorRed, gridColorGreen, gridColorBlue;
	public boolean legendFontBold, showGrid;
	
	private CBarUnitData[] cbuData;
	private CBarNode rootNode;
	private int[] vehIDsByPlotID;
	private boolean[] vehShownByPlotID;
	private AnalysisVehModelsSetup avms;
	
	public boolean isVehShown(int plotID) {return vehShownByPlotID[plotID];}
	public void setVehShown(int plotID, boolean flag) {vehShownByPlotID[plotID] = flag;}
	public int numVehicles() {return vehIDsByPlotID.length;}
	public int numVehiclesShown() {
		int num = 0;
		for (int i=0; i<vehShownByPlotID.length; i++) {
			if (vehShownByPlotID[i]) num++;
		}
		return num;
	}
	
	public String vehModelShortName(int plotID) {
		return avms.vehModelsSetup()[vehIDsByPlotID[plotID]].shortName;
	}
	public int getVehID(int plotID) {return vehIDsByPlotID[plotID];}
	public void vehPlotID_moveUp(int plotID) {
		if (plotID < 1) return;
		if (plotID >= vehIDsByPlotID.length) return;
		
		int tmp = vehIDsByPlotID[plotID-1];
		vehIDsByPlotID[plotID-1] = vehIDsByPlotID[plotID];
		vehIDsByPlotID[plotID] = tmp;
		
		boolean btmp = vehShownByPlotID[plotID-1];
		vehShownByPlotID[plotID-1] = vehShownByPlotID[plotID];
		vehShownByPlotID[plotID] = btmp;
	}
	public void vehPlotID_moveDown(int plotID) {
		if (plotID < 0) return;
		if (plotID >= vehIDsByPlotID.length-1) return;
		
		int tmp = vehIDsByPlotID[plotID+1];
		vehIDsByPlotID[plotID+1] = vehIDsByPlotID[plotID];
		vehIDsByPlotID[plotID] = tmp;
		
		boolean btmp = vehShownByPlotID[plotID+1];
		vehShownByPlotID[plotID+1] = vehShownByPlotID[plotID];
		vehShownByPlotID[plotID] = btmp;
	}
	
	public int numCheckBoxes() {return cbuData.length;}
	public int maxNumBarsOnPlot() {return cbuData.length - 4;}
	public CBarUnitData getChkData(int chkID) {return cbuData[chkID];}
	public CBarUnitData getChkData(CostBarType type) {
		CBarNode nd = rootNode.findCorrespondingNodeAmongSubTree(type);
		if (nd == null) return null;
		return cbuData[nd.daID];
	}
	
	public void setSelected(CostBarType type, boolean flag) {
		CBarNode nd = rootNode.findCorrespondingNodeAmongSubTree(type);
		if (nd == null) return;
		
		if (flag) nd.setFullySelected();
		else nd.setFullyUnSelected();
	}
	public void setCombined(CostBarType type, boolean flag) {
		CBarNode nd = rootNode.findCorrespondingNodeAmongSubTree(type);
		if (nd == null) return;
		
		boolean myNodeSelection = cbuData[nd.daID].isFullyShown;
		
		if (flag) nd.setCombined();
		else nd.setUnCombined();
		
		setSelected(type, myNodeSelection);
	}
	
	
	public CostBarsDisplaySetup(CostBarsDisplaySetup other) {
		legendFontSize = other.legendFontSize;
		legendFontBold = other.legendFontBold;
		
		axesLineWidth = other.axesLineWidth;
		gridLineWidth = other.gridLineWidth;
		gridColorRed = other.gridColorRed;
		gridColorGreen = other.gridColorGreen;
		gridColorBlue = other.gridColorBlue;
		showGrid = other.showGrid;
		
		avms = other.avms;
		
		vehIDsByPlotID = new int[other.vehIDsByPlotID.length];
		vehShownByPlotID = new boolean[other.vehIDsByPlotID.length];
		
		for (int i=0; i<vehIDsByPlotID.length; i++) {
			vehIDsByPlotID[i] = other.vehIDsByPlotID[i];
			vehShownByPlotID[i] = other.vehShownByPlotID[i];
		}
		
		cbuData = new CBarUnitData[other.cbuData.length];
		for (int i=0; i<cbuData.length; i++) cbuData[i] = new CBarUnitData(other.cbuData[i]);

		rootNode = new CBarNode(other.rootNode);
	}
	public CostBarsDisplaySetup(AnalysisVehModelsSetup avmSetup, WIITModel wiitMod, String fname) {
		legendFontSize = 11;
		legendFontBold = true;
		
		axesLineWidth = 1;
		gridLineWidth = 1;
		gridColorRed = 180;
		gridColorGreen = 180;
		gridColorBlue = 180;
		showGrid = true;
		
		avms = avmSetup;
		initialize(avms.vehModelsSetup(), wiitMod);
		
		try {
			readDataFromFile(fname);
		} catch (Exception e) {}
	}
	
	private void initialize(AnalysisVehModelsSetup.AVehModelSetup[] vms, WIITModel wiitMod) {
		//Initial order same as vehicles in model list with all vehicles shown
		vehIDsByPlotID = new int[vms.length];
		vehShownByPlotID = new boolean[vehIDsByPlotID.length];
		
		for (int i=0; i<vehIDsByPlotID.length; i++) {
			vehIDsByPlotID[i] = i;
			vehShownByPlotID[i] = true;
		}
		
		//Identify What's in the model
		boolean hasBEVwRep = false;
		boolean hasBEVCommercial = false;
		if (wiitMod.hasBEVs()) {
			if (wiitMod.bevRepModel.bevRepCommercial == null) hasBEVwRep = true;
			else hasBEVCommercial = true;
		}	
		boolean hasPHEVs = wiitMod.hasPHEVs();
		boolean hasHomeCharger = hasBEVwRep || hasPHEVs;
		boolean hasElectricty = hasHomeCharger || hasBEVCommercial;
		boolean hasElectrified = hasElectricty || wiitMod.hasHEVs();
		
		boolean hasHydrogen = wiitMod.hasHydrogen();
		boolean hasGasoline = wiitMod.hasGasoline();
		boolean hasDiesel = wiitMod.hasDiesel();
		boolean hasCNG = wiitMod.hasCNG();
		boolean hasICE = hasGasoline || hasDiesel || hasCNG || hasBEVwRep;
		boolean hasFuel = hasHydrogen || hasICE;		
		
		//Create Tree Structure
		ArrayList<CBarUnitData> lstCBData = new ArrayList<CBarUnitData>();
		
		lstCBData.add(new CBarUnitData(CostBarType.tco, 0));
		rootNode = new CBarNode(lstCBData.size()-1, null);
		
		ArrayList<CBarNode> lstRootNodeChildren = new ArrayList<CBarNode>();
		ArrayList<CBarNode> lstNetAcquisitonNodeChildren = new ArrayList<CBarNode>();
		ArrayList<CBarNode> lstPurchaseCostNodeChildren = new ArrayList<CBarNode>();
		ArrayList<CBarNode> lstRunningCostNodeChildren = new ArrayList<CBarNode>();

		lstCBData.add(new CBarUnitData(CostBarType.netAquisitionCost, 1));
		CBarNode netAcquistionNode = new CBarNode(lstCBData.size()-1, rootNode);
		lstRootNodeChildren.add(netAcquistionNode);
		
		lstCBData.add(new CBarUnitData(CostBarType.purcahseCost, 2));
		CBarNode purchaseCostNode = new CBarNode(lstCBData.size()-1, netAcquistionNode);
		lstNetAcquisitonNodeChildren.add(purchaseCostNode);
		
		lstCBData.add(new CBarUnitData(CostBarType.allElseInVeh, 3));
		lstPurchaseCostNodeChildren.add(new CBarNode(lstCBData.size()-1, purchaseCostNode));
		
		if (hasICE) {
			lstCBData.add(new CBarUnitData(CostBarType.ice, 3));
			lstPurchaseCostNodeChildren.add(new CBarNode(lstCBData.size()-1, purchaseCostNode));
		}
		if (hasHydrogen) {
			lstCBData.add(new CBarUnitData(CostBarType.fuelCell, 3));
			lstPurchaseCostNodeChildren.add(new CBarNode(lstCBData.size()-1, purchaseCostNode));
			lstCBData.add(new CBarUnitData(CostBarType.h2Tank, 3));
			lstPurchaseCostNodeChildren.add(new CBarNode(lstCBData.size()-1, purchaseCostNode));
		}
		if (hasElectrified) {
			lstCBData.add(new CBarUnitData(CostBarType.motor, 3));
			lstPurchaseCostNodeChildren.add(new CBarNode(lstCBData.size()-1, purchaseCostNode));
			lstCBData.add(new CBarUnitData(CostBarType.battery, 3));
			lstPurchaseCostNodeChildren.add(new CBarNode(lstCBData.size()-1, purchaseCostNode));
		}

		if (hasHomeCharger) {
			lstCBData.add(new CBarUnitData(CostBarType.homeChargerCost, 2));
			lstNetAcquisitonNodeChildren.add(new CBarNode(lstCBData.size()-1, netAcquistionNode));
		}
		lstCBData.add(new CBarUnitData(CostBarType.pvResaleValue, 2));
		lstNetAcquisitonNodeChildren.add(new CBarNode(lstCBData.size()-1, netAcquistionNode));
		lstCBData.add(new CBarUnitData(CostBarType.firstYearEqIncentives, 2));
		lstNetAcquisitonNodeChildren.add(new CBarNode(lstCBData.size()-1, netAcquistionNode));

		if (hasBEVCommercial) {
			lstCBData.add(new CBarUnitData(CostBarType.addVehInFleet, 1));
			lstRootNodeChildren.add(new CBarNode(lstCBData.size()-1, rootNode));
		}

		lstCBData.add(new CBarUnitData(CostBarType.totalRunningCost, 1));
		CBarNode runningCostNode = new CBarNode(lstCBData.size()-1, rootNode);
		lstRootNodeChildren.add(runningCostNode);

		lstCBData.add(new CBarUnitData(CostBarType.licCost, 2));
		lstRunningCostNodeChildren.add(new CBarNode(lstCBData.size()-1, runningCostNode));
		lstCBData.add(new CBarUnitData(CostBarType.insCost, 2));
		lstRunningCostNodeChildren.add(new CBarNode(lstCBData.size()-1, runningCostNode));
		lstCBData.add(new CBarUnitData(CostBarType.mntCost, 2));
		lstRunningCostNodeChildren.add(new CBarNode(lstCBData.size()-1, runningCostNode));
		
		if (hasFuel) {
			lstCBData.add(new CBarUnitData(CostBarType.fuelCost, 2));
			lstRunningCostNodeChildren.add(new CBarNode(lstCBData.size()-1, runningCostNode));
		}
		if (hasElectricty) {
			lstCBData.add(new CBarUnitData(CostBarType.electCost, 2));
			lstRunningCostNodeChildren.add(new CBarNode(lstCBData.size()-1, runningCostNode));
		}
		if (hasBEVwRep) {
			lstCBData.add(new CBarUnitData(CostBarType.bevRepCost, 2));
			lstRunningCostNodeChildren.add(new CBarNode(lstCBData.size()-1, runningCostNode));
		}

		rootNode.childNodes = new CBarNode[lstRootNodeChildren.size()];
		for (int i=0; i<rootNode.childNodes.length; i++) rootNode.childNodes[i] = lstRootNodeChildren.get(i);
		
		netAcquistionNode.childNodes = new CBarNode[lstNetAcquisitonNodeChildren.size()];
		for (int i=0; i<netAcquistionNode.childNodes.length; i++) netAcquistionNode.childNodes[i] = lstNetAcquisitonNodeChildren.get(i);
		
		purchaseCostNode.childNodes = new CBarNode[lstPurchaseCostNodeChildren.size()];
		for (int i=0; i<purchaseCostNode.childNodes.length; i++) purchaseCostNode.childNodes[i] = lstPurchaseCostNodeChildren.get(i);
		
		runningCostNode.childNodes = new CBarNode[lstRunningCostNodeChildren.size()];
		for (int i=0; i<runningCostNode.childNodes.length; i++) runningCostNode.childNodes[i] = lstRunningCostNodeChildren.get(i);

		cbuData = new CBarUnitData[lstCBData.size()];
		for (int i=0; i<cbuData.length; i++) cbuData[i] = lstCBData.get(i);
	}
	private void readDataFromFile(String fname) throws Exception {
		BufferedReader fin = new BufferedReader(new FileReader(fname));
		
		for (int i=0; i<cbuData.length; i++) {
			String readLine = fin.readLine();
			cbuData[i].setData(readLine);
		}
		for (int i=0; i<vehIDsByPlotID.length; i++) {
			String readLine = fin.readLine();
			String[] sp = readLine.split(",");
			
			int vehID = Integer.parseInt(sp[0]);
			boolean isShown = Boolean.parseBoolean(sp[1]);
			
			vehIDsByPlotID[i] = vehID;
			vehShownByPlotID[i] = isShown;
		}
		
		String readLine = fin.readLine();
		String[] sp = readLine.split(",");

		legendFontSize = Integer.parseInt(sp[0]);
		legendFontBold = Boolean.parseBoolean(sp[1]);
		
		axesLineWidth = Integer.parseInt(sp[2]);
		gridLineWidth = Integer.parseInt(sp[3]);
		gridColorRed = Integer.parseInt(sp[4]);
		gridColorGreen = Integer.parseInt(sp[5]);
		gridColorBlue = Integer.parseInt(sp[6]);
		showGrid = Boolean.parseBoolean(sp[7]);
		
		fin.close();
	}
	public void saveDataToFile(String fname) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			for (int i=0; i<cbuData.length; i++) {
				String st = cbuData[i].type.toString()+","+cbuData[i].cRed + "," + cbuData[i].cGreen + "," + cbuData[i].cBlue +
						"," + cbuData[i].isFullyShown + "," + cbuData[i].isCombined + "," + cbuData[i].parentHasCombined;
				fout.append(st+lsep);
			}
			for (int i=0; i<vehIDsByPlotID.length; i++) {
				fout.append(""+vehIDsByPlotID[i]+","+vehShownByPlotID[i]+lsep);
			}
			
			String st = "" + legendFontSize + "," + legendFontBold + "," + axesLineWidth + "," + gridLineWidth + "," + 
					gridColorRed + "," + gridColorGreen + "," + gridColorBlue + "," + showGrid;
			fout.append(st+lsep);

			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	
	private class CBarNode {
		private int daID;
		private CBarNode parentNode;
		private CBarNode[] childNodes;
		
		private CBarNode(int idInArray, CBarNode pNode) {daID = idInArray; parentNode = pNode;}
		private CBarNode(CBarNode other) {
			daID = other.daID;
			
			if (other.childNodes != null) {
				childNodes = new CBarNode[other.childNodes.length];
				for (int i=0; i<childNodes.length; i++) {
					childNodes[i] = new CBarNode(other.childNodes[i]);
					childNodes[i].parentNode = this;
				}
			}
		}
		
		private CBarNode findCorrespondingNodeAmongSubTree(CostBarType type) {
			if (cbuData[daID].type == type) return this;
			
			if (childNodes != null) {
				for (int i=0; i<childNodes.length; i++) {
					CBarNode stNode = childNodes[i].findCorrespondingNodeAmongSubTree(type);
					if (stNode != null) return stNode;
				}
			}
			
			return null;
		}
		
		private void setFullySelected() {
			cbuData[daID].isFullyShown = true;
			if (childNodes != null) {
				for (int i=0; i<childNodes.length; i++) childNodes[i].setFullySelected();
			}
			if (parentNode != null) parentNode.childSelected();
		}
		private void childSelected() {
			boolean allChildrenSelected = true;
			for (int i=0; i<childNodes.length; i++) allChildrenSelected = allChildrenSelected && cbuData[childNodes[i].daID].isFullyShown;
			
			if (allChildrenSelected) {
				cbuData[daID].isFullyShown = true;
				if (parentNode != null) parentNode.childSelected();
			}
		}
		private void setFullyUnSelected() {
			cbuData[daID].isFullyShown = false;
			if (childNodes != null) {
				for (int i=0; i<childNodes.length; i++) childNodes[i].setFullyUnSelected();
			}
			if (parentNode != null) parentNode.setUnSelected();
		}
		private void setUnSelected() {
			cbuData[daID].isFullyShown = false;
			if (parentNode != null) parentNode.setUnSelected();
		}
		private void setCombined() {
			cbuData[daID].isCombined = true;
			if (childNodes != null) {
				for (int i=0; i<childNodes.length; i++) childNodes[i].setCombined();
				for (int i=0; i<childNodes.length; i++) childNodes[i].setParentCombined();
			}
		}
		private void setParentCombined() {
			cbuData[daID].parentHasCombined = true;
			if (childNodes != null) {
				for (int i=0; i<childNodes.length; i++) childNodes[i].setParentCombined();
			}
		}
		private void setUnCombined() {
			cbuData[daID].isCombined = false;
			if (childNodes != null) {
				for (int i=0; i<childNodes.length; i++) childNodes[i].setParentUnCombined();
			}
		}
		private void setParentUnCombined() {
			cbuData[daID].parentHasCombined = false;
			if (!cbuData[daID].isCombined) setUnCombined();
		}
	
		private void extractTypes(ArrayList<CostBarType> lst) {
			if (cbuData[daID].isCombined) {
				if (cbuData[daID].isFullyShown) lst.add(cbuData[daID].type);
				return;
			}
			
			if (childNodes != null) {
				for (int i=0; i<childNodes.length; i++) childNodes[i].extractTypes(lst);
			} else {
				if (cbuData[daID].isFullyShown) lst.add(cbuData[daID].type);
			}
		}
	}
	
	public class CBarUnitData {
		private CostBarType type;
		private boolean isFullyShown, isCombined, hasSubLinks, parentHasCombined;
		private int cRed, cGreen, cBlue, layerLevel;
		
		public CostBarType type() {return type;}
		public int layerLevel() {return layerLevel;}
		public boolean isFullyShown() {return isFullyShown;}
		public boolean isCombined() {return isCombined;}
		public boolean hasSubLinks() {return hasSubLinks;}
		public boolean parentHasCombined() {return parentHasCombined;}
		public int colorRed() {return cRed;}
		public int colorGreen() {return cGreen;}
		public int colorBlue() {return cBlue;}
		
		
		public void setColor(int r, int g, int b) {cRed = r; cGreen = g; cBlue = b;}
		public Color getColor() {return new Color(cRed, cGreen, cBlue);}
		
		private void setData(String readLine) throws Exception {
			String[] sp = readLine.split(",");
			int r = Integer.parseInt(sp[1]);
			int g = Integer.parseInt(sp[2]);
			int b = Integer.parseInt(sp[3]);
			boolean full = Boolean.parseBoolean(sp[4]);
			boolean combine = Boolean.parseBoolean(sp[5]);
			boolean pCombined = Boolean.parseBoolean(sp[6]);
			
			isFullyShown = full;
			isCombined = combine;
			parentHasCombined = pCombined;
			setColor(r,g,b);
		}		
		private CBarUnitData(CBarUnitData other) {
			type = other.type;
			
			isFullyShown = other.isFullyShown;
			isCombined = other.isCombined;
			hasSubLinks = other.hasSubLinks;
			parentHasCombined = other.parentHasCombined;
			
			cRed = other.cRed;
			cGreen = other.cGreen;
			cBlue = other.cBlue;
			layerLevel = other.layerLevel;
		}		
		private CBarUnitData(CostBarType t, int llevel) {
			type = t;
			layerLevel = llevel;
			
			isFullyShown = true;
			isCombined = false;
			parentHasCombined = false;
			
			cRed = type.vGrabID.dRed;
			cGreen = type.vGrabID.dGreen;
			cBlue = type.vGrabID.dBlue;
			
			switch (type) {
			case tco:
			case netAquisitionCost:
			case totalRunningCost:
			case purcahseCost:
				hasSubLinks = true;
				break;
			default:
				hasSubLinks = false;
				break;
			}
		}
	}	
	
	public enum CostBarType {
		//Root Layer
		tco("Total Cost of Ownership", TCOCalculator.APTCOvsGHGSummaryOutputs.tco, false),
		
		//Layer #1
		netAquisitionCost("Net Aquisition Cost", TCOCalculator.APTCOvsGHGSummaryOutputs.netAcquistionCost, false),
		addVehInFleet("Additional Vehicles in Fleet Cost", TCOCalculator.APTCOvsGHGSummaryOutputs.addtionalVehiclesInFleetCost, false),	//Not always in model
		totalRunningCost("Total Running Cost", TCOCalculator.APTCOvsGHGSummaryOutputs.totAnnual_runningCost, false),
		
		//Layer #2, under netAquisitionCost
		purcahseCost("Total Purchase Cost", TCOCalculator.APTCOvsGHGSummaryOutputs.totPucahseCost, false),
		homeChargerCost("Appropreated Cost of Home Charger", TCOCalculator.APTCOvsGHGSummaryOutputs.cost_homeCharger, false),	//Not always in model
		pvResaleValue("First-Year Equivalent Re-Sale Value", TCOCalculator.APTCOvsGHGSummaryOutputs.credit_pvResale, true),
		firstYearEqIncentives("First-Year Equivalent Incentives", TCOCalculator.APTCOvsGHGSummaryOutputs.credit_Incentives, true),

		//Layer #2, under totalRunningCost
		licCost("Total Licensing Cost", TCOCalculator.APTCOvsGHGSummaryOutputs.annual_cost_lic, false),
		insCost("Total Insurance Cost", TCOCalculator.APTCOvsGHGSummaryOutputs.annual_cost_ins, false),
		mntCost("Total Maintenance Cost", TCOCalculator.APTCOvsGHGSummaryOutputs.annual_cost_mnt, false),
		fuelCost("Total Fuel Cost", TCOCalculator.APTCOvsGHGSummaryOutputs.annual_cost_fuel, false),						//Usually in model
		electCost("Total Electricity Cost", TCOCalculator.APTCOvsGHGSummaryOutputs.annual_cost_elect, false),				//Usually in model
		bevRepCost("Total Replacement Vehicle(s) Cost", TCOCalculator.APTCOvsGHGSummaryOutputs.annual_cost_bevRep, false),	//Not always in model

		//Layer #3, under purcahseCost
		allElseInVeh("Equivalent Retail Cost for Everything Else in Vehicle", TCOCalculator.APTCOvsGHGSummaryOutputs.cost_allElseInVeh, false),
		ice("Equivalent Retail Cost for Engine System", TCOCalculator.APTCOvsGHGSummaryOutputs.cost_ICE, false),	//Not always in model
		fuelCell("Equivalent Retail Cost for Fuel Cell System", TCOCalculator.APTCOvsGHGSummaryOutputs.cost_fuelCell, false),	//Not always in model
		h2Tank("Equivalent Retail Cost for Hydrogen Tank System", TCOCalculator.APTCOvsGHGSummaryOutputs.cost_h2Tank, false),	//Not always in model
		motor("Equivalent Retail Cost for Motor System", TCOCalculator.APTCOvsGHGSummaryOutputs.cost_motor, false),	//Not always in model
		battery("Equivalent Retail Cost for Battery System", TCOCalculator.APTCOvsGHGSummaryOutputs.cost_battery, false),	//Not always in model

		;
		private String captionWOUnits;
		private TCOCalculator.APTCOvsGHGSummaryOutputs vGrabID;
		private boolean isNegative;
		
		public String getCaptionWithoutUnits() {
			return captionWOUnits;
		}
		public String getCaptionWUnits() {
			return captionWOUnits + " (" + DUnits.getShortName(DUnits.UnitType.LargeMoneyUnit) + ")";
		}
		public float getValueInOutputUnits(int vehID, TCOCalculator.TCOvsGHGSummaryOutputStructure tcoSummary, float numYears) {
			switch (this) {
			case licCost:
			case insCost:
			case mntCost:
			case fuelCost:
			case electCost:
			case bevRepCost:
			case totalRunningCost:
				return 0.001f * tcoSummary.getValue(vehID, vGrabID) * numYears / DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			default:
				return tcoSummary.getValue(vehID, vGrabID) / DUnits.convConstMPtoBCalc(DUnits.UnitType.LargeMoneyUnit);
			}			
		}
		public boolean isNegative() {return isNegative;}
		public int getDefaultColorRed() {return vGrabID.dRed;}
		public int getDefaultColorGreen() {return vGrabID.dGreen;}
		public int getDefaultColorBlue() {return vGrabID.dBlue;}
		
		private CostBarType(String s, TCOCalculator.APTCOvsGHGSummaryOutputs v, boolean neg) {
			captionWOUnits = s;
			vGrabID = v;
			isNegative = neg;
		}

		public static CostBarType decode(String s) {
			CostBarType[] v = values();
			for (int i=0; i<v.length; i++) {
				if (s.equalsIgnoreCase(v[i].toString())) return v[i];
			}
			return null;
		}
	}
	
	public CostBarsPlotOutput genCostBarsData(TCOCalculator.TCOvsGHGSummaryOutputStructure tcoRes, float numYears) {
		CostBarsPlotOutput res = new CostBarsPlotOutput();
		
		for (int i=0; i<vehIDsByPlotID.length; i++) {
			int vehID = vehIDsByPlotID[i];
			
			for (int j=0; j<res.cbIncluded.length; j++) {
				res.valuesInOutputLargeMoneyUnits[i][j] = res.cbIncluded[j].getValueInOutputUnits(vehID, tcoRes, numYears);
			}
		}
		
		return res;
	}
	
	public class CostBarsPlotOutput {
		private CostBarType[] cbIncluded;
		private float[][] valuesInOutputLargeMoneyUnits;	//First Index on number of vehicle models (ordered by plotID), second index on output types
		
		public CostBarType getCBarType(int barID) {return cbIncluded[barID];}
		
		public int numCBars() {
			return cbIncluded.length;
		}
		public int numVehModels() {
			return vehIDsByPlotID.length;
		}
		public String[] costBarTitles() {
			String[] st = new String[cbIncluded.length];
			for (int i=0; i<st.length; i++) st[i] = new String(cbIncluded[i].getCaptionWithoutUnits());
			return st;
		}
		public String[] vehTitlesByPlotOrder() {
			String[] st = new String[vehIDsByPlotID.length];
			for (int i=0; i<st.length; i++) st[i] = new String(avms.vehModelsSetup()[vehIDsByPlotID[i]].shortName);
			return st;
		}
		public float getValueInOutputLargeMoneyUnits(int plotID, int cbID) {
			return valuesInOutputLargeMoneyUnits[plotID][cbID];
		}
		
		private CostBarsPlotOutput(CostBarsPlotOutput allCBars, boolean extractNegative) {
			int numNeg = allCBars.numNegQunatities();
			
			if (extractNegative) {
				if (numNeg < 1) {
					cbIncluded = new CostBarType[0];
					valuesInOutputLargeMoneyUnits = new float[vehIDsByPlotID.length][0];
					return;
				}
				
				cbIncluded = new CostBarType[numNeg];
				valuesInOutputLargeMoneyUnits = new float[vehIDsByPlotID.length][numNeg];
				
				int cPos = allCBars.lastIndexOfNegQunatities();
				int ccPos = 0;
				
				while (cPos >= 0) {
					if (allCBars.cbIncluded[cPos].isNegative) {
						cbIncluded[ccPos] = allCBars.cbIncluded[cPos];
						for (int i=0; i<vehIDsByPlotID.length; i++) valuesInOutputLargeMoneyUnits[i][ccPos] = allCBars.valuesInOutputLargeMoneyUnits[i][cPos];
						ccPos++;
					}
					
					cPos--;
				}
				return;
			}
			
			int numPos = allCBars.cbIncluded.length - numNeg;
			if (numPos < 1) {
				cbIncluded = new CostBarType[0];
				valuesInOutputLargeMoneyUnits = new float[vehIDsByPlotID.length][0];
				return;
			}

			cbIncluded = new CostBarType[numPos];
			valuesInOutputLargeMoneyUnits = new float[vehIDsByPlotID.length][numPos];
			
			int cPos = 0;
			int ccPos = 0;

			while (cPos < allCBars.cbIncluded.length) {
				if (!allCBars.cbIncluded[cPos].isNegative) {
					cbIncluded[ccPos] = allCBars.cbIncluded[cPos];
					for (int i=0; i<vehIDsByPlotID.length; i++) valuesInOutputLargeMoneyUnits[i][ccPos] = allCBars.valuesInOutputLargeMoneyUnits[i][cPos];
					ccPos++;
				}
				
				cPos++;
			}
		}
		private int lastIndexOfNegQunatities() {
			int id = -1;
			for (int i=0; i<cbIncluded.length; i++) {
				if (cbIncluded[i].isNegative) id = i;
			}
			return id;
		}
		private int numNegQunatities() {
			int num = 0;
			for (int i=0; i<cbIncluded.length; i++) {
				if (cbIncluded[i].isNegative) num++;
			}
			return num;
		}
		private CostBarsPlotOutput() {
			ArrayList<CostBarType> lst = new ArrayList<CostBarType>();
			rootNode.extractTypes(lst);
			
			cbIncluded = new CostBarType[lst.size()];
			for (int i=0; i<cbIncluded.length; i++) cbIncluded[i] = lst.get(i);
			
			valuesInOutputLargeMoneyUnits = new float[vehIDsByPlotID.length][cbIncluded.length];
		}		
	
		public CostBarsPlotOutput getNegativeBars() {return new CostBarsPlotOutput(this, true);}
		public CostBarsPlotOutput getPositiveBars() {return new CostBarsPlotOutput(this, false);}
	
		@Override public String toString() {
			String lsep = System.getProperty("line.separator");
			
			String st = "vehicleModel";
			for (int j=0; j<cbIncluded.length; j++) st = st + "," + cbIncluded[j].getCaptionWUnits();
			st = st + lsep;
			
			AnalysisVehModelsSetup.AVehModelSetup[] vms = avms.vehModelsSetup();
			
			for (int i=0; i<vehIDsByPlotID.length; i++) {
				st = st + vms[vehIDsByPlotID[i]].shortName;
				for (int j=0; j<cbIncluded.length; j++) st = st + "," + valuesInOutputLargeMoneyUnits[i][j];
				st = st + lsep;
			}		
			
			return st;
		}
	}
}
