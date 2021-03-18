package pvc.datamgmt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import fastsimjava.FSJOneFileVehModel;
import pvc.calc.FEcoSimsGenerator;
import pvc.datamgmt.comp.FDefaults;

public class ChargingEffManager {
	private VehChargingEfficiencies[] vehChEffs;
	public VehChargingEfficiencies vehChEff(int vehID) {
		return vehChEffs[vehID];
	}
	
	public static ChargingEffManager readFromFile(AnalysisVehModelsSetup.AVehModelSetup[] vms, String fname) {
		try {
			return new ChargingEffManager(vms, fname);
		} catch (Exception e) {
			return null;
		}
	}
	
	private ChargingEffManager(AnalysisVehModelsSetup.AVehModelSetup[] vms, String fname) throws Exception {
		read(vms, fname);
	}
	public ChargingEffManager(ChargingEffManager other) {
		vehChEffs = new VehChargingEfficiencies[other.vehChEffs.length];
		for (int i=0; i<vehChEffs.length; i++) {
			if (other.vehChEffs[i] != null) {
				vehChEffs[i] = new VehChargingEfficiencies(other.vehChEffs[i]);
			} else {
				vehChEffs[i] = null;
			}
		}
	}
	public ChargingEffManager(FEcoSimsGenerator fsG) {
		AnalysisVehModelsSetup.AVehModelSetup[] vms = fsG.avms().vehModelsSetup();
		FSJOneFileVehModel[] fsofModels = fsG.fsofModels();
		
		vehChEffs = new VehChargingEfficiencies[vms.length];
		
		for (int i=0; i<fsofModels.length; i++) {
			switch (fsofModels[i].vehModelParam.general.vehPtType) {
			case bev:
			case phev:
				vehChEffs[i] = new VehChargingEfficiencies(vms[i].shortName, FDefaults.chargerDefaultEfficiency, 
						FDefaults.chargerDefaultEfficiency, FDefaults.chargerDefaultEfficiency);
				break;
			default:
				break;
			}
		}
	}
	private void read(AnalysisVehModelsSetup.AVehModelSetup[] vms, String fname) throws Exception {
		vehChEffs = new VehChargingEfficiencies[vms.length];
		
		BufferedReader fin = new BufferedReader(new FileReader(fname));
		String readLine = fin.readLine();
		
		while ((readLine = fin.readLine())!=null) {
			try {
				VehChargingEfficiencies vehChEff = new VehChargingEfficiencies(readLine);
				
				int vehID  = -1;
				for (int i=0; i<vms.length; i++) {
					if (vehChEff.vehShortName.equalsIgnoreCase(vms[i].shortName)) {
						vehID = i;
						break;
					}
				}
				
				vehChEffs[vehID] = vehChEff;
				
			} catch (Exception e) {
				fin.close();
				throw new Exception();
			}
		}

		fin.close();
	}
	public void save(String fname) {
		try {
			FileWriter fout = new FileWriter(fname);
			String lsep = System.getProperty("line.separator");
			
			fout.append("vehShortName,chEff_L1, chEff_L2,chEff_DCFast"+lsep);
			
			for (int i=0; i<vehChEffs.length; i++) {
				if (vehChEffs[i] != null) {
					fout.append(vehChEffs[i].toString());
					fout.append(lsep);
				}
			}
			
			fout.flush();
			fout.close();
		} catch (Exception e) {}
	}
	
	public static class VehChargingEfficiencies {
		private String vehShortName;
		public String vehShortName() {return vehShortName;}
		
		public float chEffL1, chEffL2, chEffDCFast; 
		
		private VehChargingEfficiencies(VehChargingEfficiencies other) {
			vehShortName = new String(other.vehShortName);
			chEffL1 = other.chEffL1;
			chEffL2 = other.chEffL2;
			chEffDCFast = other.chEffDCFast;
		}
		
		private VehChargingEfficiencies(String name, float chgEffL1, float chgEffL2, float chgEffDCFast) {
			vehShortName = new String(name);
			chEffL1 = chgEffL1;
			chEffL2 = chgEffL2;
			chEffDCFast = chgEffDCFast;
		}
		private VehChargingEfficiencies(String readLine) throws Exception {
			String[] sp = readLine.split(",");
			
			vehShortName = new String(sp[0]);
			chEffL1 = Float.parseFloat(sp[1]);
			chEffL2 = Float.parseFloat(sp[2]);
			chEffDCFast = Float.parseFloat(sp[3]);
		}
		@Override public String toString() {return vehShortName + "," + chEffL1 + "," + chEffL2 + "," + chEffDCFast;}
	}
}
