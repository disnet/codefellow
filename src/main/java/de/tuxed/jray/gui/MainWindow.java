package de.tuxed.jray.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class MainWindow extends JFrame {

    public static void main(String[] args) {
	MainWindow mw = new MainWindow();
	mw.setVisible(true);
    }

    public MainWindow() {
	setSize(600, 900);
	setTitle("JRay");
	setContentPane(new MainPanel());
	addWindowListener(new WindowAdapter() {
            @Override
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});

    }

}
