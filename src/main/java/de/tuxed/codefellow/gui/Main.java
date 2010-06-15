package de.tuxed.codefellow.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class Main extends JFrame {

    public static void main(String[] args) throws Exception {
	Main mw = new Main();
	mw.setVisible(true);
    }

    public Main() {
	setSize(1000, 700);
	setTitle("CodeFellow");
	setContentPane(new MainPanel());
	addWindowListener(new WindowAdapter() {
            @Override
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});

    }

}
