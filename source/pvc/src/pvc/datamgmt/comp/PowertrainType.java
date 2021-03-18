package pvc.datamgmt.comp;

import fastsimjava.FSJVehModelParam;

public enum PowertrainType {
	cv_gas("Gasoline Conventional ICE",						255,  0,  0),
	cv_diesel("Diesel Conventional ICE",					  0,  0,  0),
	cv_cng("CNG Conventional ICE",							205, 25,119),

	hev_gas("Gasoline Hybrid Electric Vehicle",				238,125, 49),
	hev_diesel("Diesel Hybrid Electric Vehicle",			132, 60, 12),
	hev_cng("CNG Hybrid Electric Vehicle",					255,  0,255),
	hev_fc("Hydrogen Fuel Cell Vehicle",					112, 48,160),

	phev_gas("Plug-in Gasoline Hybrid Electric Vehicle",	  0,176,240),
	phev_diesel("Plug-in Diesel Hybrid Electric Vehicle",	  0, 54,162),
	phev_cng("Plug-in CNG Hybrid Electric Vehicle",			255, 83,181),
	phev_fc("Plug-in Hydrogen Fuel Cell Vehicle",			106, 65,207),

	bev("Battery (only) Electric Vehicle",					  0,176, 80)
	;
	public String description;
	public int defaultColorRed, defaultColorGreen, defaultColorBlue;
	
	private PowertrainType(String s, int r, int g, int b) {
		description = s;
		defaultColorRed = r;
		defaultColorGreen = g;
		defaultColorBlue = b;
	}
	
	public static int toInt(PowertrainType ptType) {return ptType.ordinal();}
	public static PowertrainType decode(String intString) {
		try {
			int id = Integer.parseInt(intString);
			return PowertrainType.values()[id];
		} catch (Exception e) {
			return null;
		}
	}
	public static PowertrainType decode(FSJVehModelParam vehModel) {
		switch (vehModel.general.vehPtType) {
		case bev:
			return bev;
		case cv:
			switch (vehModel.fuelConv.fcEffType) {
			case cng:
				return cv_cng;
			case sparkIgnition:
			case atkins:
				return cv_gas;
			case diesel:
			case hybridDiesel:
				return cv_diesel;
			default:
				return null;			
			}
		case hev:
			switch (vehModel.fuelConv.fcEffType) {
			case cng:
				return hev_cng;
			case sparkIgnition:
			case atkins:
				return hev_gas;
			case diesel:
			case hybridDiesel:
				return hev_diesel;
			case fuelCell:
				return hev_fc;
			default:
				return null;			
			}
		case phev:
			switch (vehModel.fuelConv.fcEffType) {
			case cng:
				return phev_cng;
			case sparkIgnition:
			case atkins:
				return phev_gas;
			case diesel:
			case hybridDiesel:
				return phev_diesel;
			case fuelCell:
				return phev_fc;
			default:
				return null;			
			}
		default:
			return null;
		}
	}
	
	public static FuelType fuelType(PowertrainType ptType) {
		switch (ptType) {
		case cv_cng:
		case hev_cng:
		case phev_cng:
			return FuelType.ng;
		case cv_diesel:
		case hev_diesel:
		case phev_diesel:
			return FuelType.diesel;
		case cv_gas:
		case hev_gas:
		case phev_gas:
			return FuelType.gas;
		case hev_fc:
		case phev_fc:
			return FuelType.h2;
		default:
			return null;	
		}
	}
	
	public enum FuelType {
		gas, diesel, ng, h2
	}
}
