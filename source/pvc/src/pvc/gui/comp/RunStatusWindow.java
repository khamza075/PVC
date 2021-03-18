package pvc.gui.comp;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.Border;

import pvc.runners.RunWrapper;

@SuppressWarnings("serial")
public class RunStatusWindow extends JFrame implements PropertyChangeListener {
	//GUI
	private static final int numDsiplayLines = 12;
	private static final int numStoredLines = 120;
	private JList<String> lstDisplay;
	private ArrayList<String> lstStore;
	private JScrollPane lstScroll;

	
	//Data objects
	private RunWrapper runWrapper;
	private RunStatusWindow stWindow;
	private boolean alreadyHadError;
	
	private boolean abortRequested;
	public boolean abortRequested() {return abortRequested;}	//Scanned by the running process
	
	//Window Execution management
	private TaskRunner workerTask;

	public RunStatusWindow(RunWrapper runner, String windowTitle) {
		//Call super
		super(windowTitle);
		
		//Set variables
		runWrapper = runner;
		stWindow = this;
		alreadyHadError = false;

		lstStore = new ArrayList<String>();

		//Create GUI and be on standby for run
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent event) {
	            exitProcedure();
	        }
	    });
		
		JPanel ct = createContentPane();
		setContentPane(ct);

		JFrame frame = new JFrame();
		frame.pack();
		Insets insets = frame.getInsets();
        int addedWidth = insets.left + insets.right;
        int addedHeight = insets.top + insets.bottom;
     
        int winWidth = ct.getWidth() + addedWidth;
        int winHeight = ct.getHeight() + addedHeight;
         
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        setLocation((screenWidth-winWidth)/2, (screenHeight-winHeight)/2);
        setSize(winWidth, winHeight);
        setResizable(false);
	}
	
	public void startRun() {
		setVisible(true);
		
		abortRequested = false;
		workerTask = new TaskRunner();
		workerTask.addPropertyChangeListener(this);
		workerTask.execute();
	}

    class TaskRunner extends SwingWorker<Void, Void> {    	
        //Executed in background
        @Override
        public Void doInBackground() {
        	runWrapper.doRun(stWindow);
        	exitProcedure();
			return null;
        }
        //Doorway to trigger status change
        public void changeStatus() {
        	setProgress(1 -getProgress());
        }
    }	
	
	private void exitProcedure() {		
		if (!runWrapper.runCompleted()) {
			runWrapper.abortCurrentRun();
			abortRequested = true;
		}
		
		runWrapper.returnToOwnerWindow();
		setVisible(false);
		dispose();
	}
	public void runHadError() {
		if (alreadyHadError) return;
		alreadyHadError = true;
		
		runWrapper.abortCurrentRun();
		abortRequested = true;
		
		println(" ");
		String[] mlS = breakUpMultiLines(runWrapper.errString());
		for (int i=0; i<mlS.length; i++) println(mlS[i]);		
		println("--- You May Close this Window ---");
		
		runWrapper.returnToOwnerWindow();
	}
	public void println(String lineMsg) {
		if (lstStore.size() >= numStoredLines) {
			lstStore.remove(0);
		}
		
		lstStore.add(lineMsg);
		
		//Trigger property change function
		workerTask.changeStatus();
	}	

	private JPanel createContentPane() {
		//Sizing values
		int winMargin = 10;
		int lblHeight = 18;
		int lblWidth = 500;
		int lineSpacing = lblHeight + 2;
				
		int panelWidth = lblWidth + winMargin*2;
		int panelHeight = lineSpacing*numDsiplayLines + winMargin*2;
		
		//Create
		JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);
        totalGUI.setSize(panelWidth, panelHeight);
        
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        lstDisplay = new JList<String>();
        lstDisplay.setLocation(0, 0);
        lstDisplay.setSize(lblWidth, lineSpacing*numDsiplayLines);
        lstDisplay.setBorder(blackBorder);	
        
        lstScroll = new JScrollPane(lstDisplay);
        lstScroll.setLocation(winMargin, winMargin);
        lstScroll.setSize(lblWidth, lineSpacing*numDsiplayLines);  
        totalGUI.add(lstScroll);

        //Return
        totalGUI.setOpaque(true);
		return totalGUI;
	}
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		storeToDisplay();
	}	
	private void storeToDisplay() {
		String[] sta = new String[lstStore.size()];
		for (int i=0; i<sta.length; i++) sta[i] = lstStore.get(i);
		lstDisplay.setListData(sta);
		
		lstScroll.getVerticalScrollBar().setValue(lstScroll.getVerticalScrollBar().getMaximum());
	}
	
	private static String[] breakUpMultiLines(String s) {
		ArrayList<String> lst = new ArrayList<String>();
		
		String remSt = new String(s);
		boolean moreMultiLinesExist = true;
		
		while (moreMultiLinesExist) {
			int index10 = remSt.indexOf(10);
			int index13 = remSt.indexOf(13);
			
			if (index10 > 0) {
				lst.add(remSt.substring(0, index10));
				remSt = remSt.substring(index10+1);
			} else if (index13 > 0) {
				lst.add(remSt.substring(0, index13));
				remSt = remSt.substring(index13+1);
			} else {
				lst.add(remSt);
				moreMultiLinesExist = false;
			}
		}
		
		String[] mlS = new String[lst.size()];
		for (int i=0; i<mlS.length; i++) mlS[i] = lst.get(i);
		return mlS;
	}

}
