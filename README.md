# PVC
A software for assessing the efficacy of various vehicle powertrains at mitigation of greenhouse gas emissions

Users that are only interested in obtaining a ready-to-use pre-compiled program and/or results visualization should refer to https://www.carghg.org/

This software is coded entirely in Java; aside from standard Java library, has no dependencies except on the Java implementation of FASTSim (fuel economy simulation tool by NREL). See github.com/khamza075/FASTSim-Java for more details

Source code is available in the folder: /source/pvc/

Basic data, sample vehicle models with results and associated folder structure may be found in the folder: /data/

Note that to keep the file size reasonable, the fuel economy simulations in this sample result were generated via only 20 vehicles (435 trips) from California Household Travel Survey (CHTS) dataset. Users and contributors interested in the result for full CHTS dataset may download all trips in CHTS dataset (~253MB) then use PVC software to re-run the fuel economy simulations: https://bit.ly/pvc_chts_full
