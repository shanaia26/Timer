
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

import net.miginfocom.swing.MigLayout;

public class Timer extends JFrame {

	// Interface components

	// Fonts to be used
	Font countdownFont = new Font("Arial", Font.BOLD, 20);
	Font elapsedFont = new Font("Arial", Font.PLAIN, 14);

	// Labels and text fields
	JLabel countdownLabel = new JLabel("Seconds remaining:");
	JTextField countdownField = new JTextField(15);
	JLabel elapsedLabel = new JLabel("Time running:");
	JTextField elapsedField = new JTextField(15);
	JButton startButton = new JButton("START");
	JButton pauseButton = new JButton("PAUSE");
	JButton stopButton = new JButton("STOP");

	// The text area and the scroll pane in which it resid jm,bes
	JTextArea display;

	JScrollPane myPane;

	// These represent the menus
	JMenuItem saveData = new JMenuItem("Save data", KeyEvent.VK_S);
	JMenuItem displayData = new JMenuItem("Display data", KeyEvent.VK_D);

	JMenu options = new JMenu("Options");

	JMenuBar menuBar = new JMenuBar();

	JFileChooser jfc = new JFileChooser();

	// These booleans are used to indicate whether the START button has been clicked
	boolean started;

	// and the state of the timer (paused or running)
	boolean paused;

	// Number of seconds
	long totalSeconds = 0;
	long secondsToRun = 0;
	long secondsSinceStart = 0;

	// This is the thread that performs the countdown and can be started, paused and
	// stopped
	TimerThread countdownThread;

	TimerDialog td;
	Thread myRunnable;

	// Interface constructed
	Timer() {

		setTitle("Timer Application");

		MigLayout layout = new MigLayout("fillx");
		JPanel panel = new JPanel(layout);
		getContentPane().add(panel);

		options.add(saveData);
		options.add(displayData);
		menuBar.add(options);

		panel.add(menuBar, "spanx, north, wrap");

		MigLayout centralLayout = new MigLayout("fillx");

		JPanel centralPanel = new JPanel(centralLayout);

		GridLayout timeLayout = new GridLayout(2, 2);

		JPanel timePanel = new JPanel(timeLayout);

		countdownField.setEditable(false);
		countdownField.setHorizontalAlignment(JTextField.CENTER);
		countdownField.setFont(countdownFont);
		countdownField.setText("00:00:00");

		timePanel.add(countdownLabel);
		timePanel.add(countdownField);

		elapsedField.setEditable(false);
		elapsedField.setHorizontalAlignment(JTextField.CENTER);
		elapsedField.setFont(elapsedFont);
		elapsedField.setText("00:00:00");

		timePanel.add(elapsedLabel);
		timePanel.add(elapsedField);

		centralPanel.add(timePanel, "wrap");

		GridLayout buttonLayout = new GridLayout(1, 3);

		JPanel buttonPanel = new JPanel(buttonLayout);

		buttonPanel.add(startButton);
		buttonPanel.add(pauseButton, "");
		buttonPanel.add(stopButton, "");

		centralPanel.add(buttonPanel, "spanx, growx, wrap");

		panel.add(centralPanel, "wrap");

		display = new JTextArea(100, 150);
		display.setMargin(new Insets(5, 5, 5, 5));
		display.setEditable(false);

		JScrollPane myPane = new JScrollPane(display, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		panel.add(myPane, "alignybottom, h 100:320, wrap");

		// Initial state of system
		paused = false;
		started = false;

		// Allowing interface to be displayed
		setSize(400, 500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);

		jfc.setCurrentDirectory(new File(System.getProperty("user.dir")));

		// TODO: SAVE: This method should allow the user to specify a file name to which
		// to save the contents of the text area using a
		// JFileChooser. You should check to see that the file does not already exist in
		// the system.
		saveData.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = jfc.showSaveDialog(Timer.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = jfc.getSelectedFile();
					// This is where a real application would open the file.
					System.out.println("Saving: " + file.getName() + "\n");
					try {
						writeDataFile(file);
						display.setText("");
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					System.out.println("Save command cancelled by user." + "\n");

				}
			}
		});

		// TODO: DISPLAY DATa: This method should retrieve the contents of a file
		// representing a previous report using a JFileChooser.
		// The result should be displayed as the contents of a dialog object.

		displayData.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = jfc.showOpenDialog(Timer.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = jfc.getSelectedFile();
					// This is where a real application would open the file.
					System.out.println("Displaying: " + file.getName() + "\n");
					try {
						JOptionPane.showMessageDialog(Timer.this, readDataFile(file), file.getName(),
								JOptionPane.PLAIN_MESSAGE, null);
					} catch (HeadlessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					System.out.println("Display command cancelled by user." + "\n");
				}
			}
		});

		// TODO: START: This method should check to see if the application is already
		// running, and if not, launch a TimerThread object.
		// If the application is running, you may wish to ask the user if the existing
		// thread should be stopped and a new thread started.
		// It should begin by launching a TimerDialog to get the number of seconds to
		// count down, and then pass that number of seconds along
		// with the seconds since the start (0) to the TimerThread constructor.
		// It can then display a message in the text area stating how long the countdown
		// is for.
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (started == false) {
					td = new TimerDialog(Timer.this, secondsToRun, true);
					countdownThread = new TimerThread(countdownField, elapsedField, td.getSeconds(), secondsSinceStart,
							Timer.this);
					myRunnable = new Thread(countdownThread);
					myRunnable.start();

					display.append("Countdown for: " + td.getSeconds() + " Seconds\n");
					started = true;
				} else if (started == true) {
					// If start is clicked again, user is given the option to restart the timer
					int choice = JOptionPane.showConfirmDialog(Timer.this, "Do you want to restart the timer?");
					// It will return an int, yes=0, no=1, cancel =2
					if (choice == 0) {
						display.setText("");

						countdownThread.stop();

						td = new TimerDialog(Timer.this, secondsToRun, true);
						countdownThread = new TimerThread(countdownField, elapsedField, td.getSeconds(),
								secondsSinceStart, Timer.this);
						myRunnable = new Thread(countdownThread);
						myRunnable.start();

						display.append("Countdown for: " + td.getSeconds() + " Seconds\n");

					} else if (choice == 1 || choice == 2) {
						// Do nothing
					}

				}
			}
		});

		// TODO: PAUSE: This method should call the TimerThread object's pause method
		// and display a message in the text area
		// indicating whether this represents pausing or restarting the timer.
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			if (pauseButton.getText() == "PAUSE") {
			pauseButton.setText("RESUME");
			countdownThread.pause();
			display.append("Paused at: " + elapsedField.getText() + " Seconds\n");
			paused = true;
			}
			else if (pauseButton.getText() == "RESUME") {
			pauseButton.setText("PAUSE");
			paused = false;
			countdownThread.pause();
			display.append("Resumed at: " + countdownField.getText() + " Seconds\n");
			}
			}
			});

		// TODO: STOP: This method should stop the TimerThread object and use
		// appropriate methods to display the stop time
		// and the total amount of time remaining in the countdown (if any).
		stopButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				countdownThread.stop();
				started = false;
				display.append("Timer Stopped at: " + countdownField.getText() + "\n");
			}

		});

	}

	public void UpdateCountdownLabel(long seconds) {
		String strSeconds = countdownThread.convertToHMSString(seconds);
		countdownField.setText(strSeconds);
	}

	public void UpdateElapsedLabel(long secs) {
		String strSecs = countdownThread.convertToHMSString(secs);
		elapsedField.setText(strSecs);
	}

	// TODO: These methods can be used in the action listeners above.
	public synchronized void writeDataFile(File f) throws IOException, FileNotFoundException {
		// Create file OutputStream
		ObjectOutputStream out = null;
		String s = display.getText();
		try {
			// 1. Opening the Stream
			FileOutputStream fo = new FileOutputStream(f);
			out = new ObjectOutputStream(fo);

			// 2. Writing to Stream
			out.writeObject(s);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// TODO: These methods can be used in the action listeners above.
	public synchronized String readDataFile(File f) throws IOException, ClassNotFoundException {

		String result = new String();
		ObjectInputStream in = null;
		try {
			FileInputStream fi = new FileInputStream(f);
			in = new ObjectInputStream(fi);

			// Read from Stream
			result = (String)in.readObject();
		} catch (EOFException e) {
		} finally {
			in.close();
		}
		return result;
	}

	public static void main(String[] args) {

		Timer timer = new Timer();

	}

}