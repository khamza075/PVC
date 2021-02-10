# PVC
A software for assessing the efficacy of various vehicle powertrains at mitigation of greenhouse gas emissions

This software is coded entirely in Java; aside from standrad Java library, has no dependencies exept on the Java implementation of FASTSim (fuel economy simulation tool by NREL). See github.com/khamza075/FASTSim-Java for more details

Source code is available in the folder: /source/pvc/

A ready to run compiled .jar along with a brief user manual, a quick start quide, sample vehicle models and results may be found in the folder: /public_compiled/ 

Note that to keep the file size reasonable, the fuel economy simulations in this sample result were generated via only 20 vehicles (435 trips) from California Household Travel Survey (CHTS) dataset. Users and contributors interested in the result for full CHTS dataset may do one of the following:
* Download a pre-completed analysis with all CHTS dataset (~42 MB): https://drive.google.com/file/d/1JbikgDRTLnnE6HA_8klVl-WYVAYk-afZ/view?usp=sharing 
* Download all trips in CHTS dataset (~253MB) then use PVC software to re-run the fuel economy simulations: https://drive.google.com/file/d/1vzOfcJKXdaZ4Z537GV2XBBJNwdPqFT9p/view?usp=sharing
