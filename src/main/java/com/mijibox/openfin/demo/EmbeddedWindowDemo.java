package com.mijibox.openfin.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.mijibox.openfin.FinEmbeddedPanel;
import com.mijibox.openfin.FinLauncher;
import com.mijibox.openfin.FinRuntime;
import com.mijibox.openfin.FinRuntimeConnectionListener;
import com.mijibox.openfin.bean.ApplicationOptions;
import com.mijibox.openfin.bean.Identity;
import com.mijibox.openfin.bean.RuntimeConfig;
import com.mijibox.openfin.bean.WindowOptions;
import com.sun.jna.Platform;

public class EmbeddedWindowDemo implements FinRuntimeConnectionListener {
	
	private JFrame frame;
	private JPanel glassPane;
	private JPanel contentPane;
	private FinEmbeddedPanel embeddedOpenFin;
	private FinRuntime fin;
	private String appUuid;

	public EmbeddedWindowDemo() {
		this.frame = new JFrame("OpenFin Embedded Window Demo");
		this.initGui();
		this.initOpenFin();
	}
	
	private JPanel createGlassPane() {
		this.glassPane = new JPanel(new BorderLayout());
		JLabel l = new JLabel("Loading, please wait......");
		l.setHorizontalAlignment(JLabel.CENTER);
		this.glassPane.add(l, BorderLayout.CENTER);
		this.glassPane.setBackground(Color.LIGHT_GRAY);
		return this.glassPane;
	}

	private void initGui() {
		JMenuBar menuBar = new JMenuBar();
		JMenu mainMenu = new JMenu("OpenFin");
		JMenu dummyMenu = new JMenu("Dummy");
		JMenuItem miToggle = new JMenuItem("Toggle Embedded Window");
		miToggle.addActionListener(e->{
			this.embeddedOpenFin.setVisible(!this.embeddedOpenFin.isVisible());
			//When mixing lightweight and heavyweight components, it's necessary to revalidate the parent container.
			//without it, the lightweight menu items wouldn't be rendered correctly. 
			this.frame.revalidate();
		});
		mainMenu.add(miToggle);
		JMenuItem miExit = new JMenuItem("Exit");
		miExit.addActionListener(e->{
			this.fin.disconnect();
		});
		mainMenu.add(miExit);
		for (int i=0; i<5; i++) {
			JMenuItem dummy = new JMenuItem("Dummy_" + (i+1));
			dummyMenu.add(dummy);
			if (i == 4) {
				JMenu subMenu = new JMenu("More Dummies");
				dummyMenu.add(subMenu);
				for (int j=0; j<10; j++) {
					JMenuItem subDummy = new JMenuItem("SubDummy_" + (j+1));
					subMenu.add(subDummy);
				}
			}
		}
		menuBar.add(mainMenu);
		menuBar.add(dummyMenu);
		this.frame.setJMenuBar(menuBar);
		JPanel pnlContent = new JPanel(new BorderLayout());
		pnlContent.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20), 
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Embedded OpenFin Window")));
		pnlContent.setPreferredSize(new Dimension(800, 600));
		this.contentPane = new JPanel(new BorderLayout());
		pnlContent.add(this.contentPane, BorderLayout.CENTER);
		this.embeddedOpenFin = new FinEmbeddedPanel();
		this.frame.setContentPane(pnlContent);
		this.frame.setGlassPane(this.createGlassPane());
		this.frame.pack();
		this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (fin != null) {
					fin.disconnect();
				}
				else {
					System.exit(0);
				}
			}
		});
		this.frame.setLocationRelativeTo(null);
		this.frame.setVisible(true);
		this.glassPane.setVisible(true);
		this.contentPane.add(this.embeddedOpenFin, BorderLayout.CENTER);
	}
	
	private void initOpenFin() {
		this.appUuid = UUID.randomUUID().toString();
		//main window option
		WindowOptions winOpts = new WindowOptions(appUuid);
		winOpts.setUrl("https://www.google.com");
		//startup app config
		ApplicationOptions appOpts = new ApplicationOptions(appUuid);
		appOpts.setMainWindowOptions(winOpts);
		//runtime config
		RuntimeConfig config = new RuntimeConfig();
		config.getRuntime().setVersion("stable-v15");
		config.setStartupApp(appOpts);
		//launch openfin
		FinLauncher.newLauncherBuilder()
			.runtimeConfig(config)
			.connectionListener(this)
			.build()
			.launch();
	}
	
	@Override
	public void onOpen(FinRuntime finRuntime) {
		this.fin = finRuntime;
		this.embeddedOpenFin.embed(fin, new Identity(appUuid, appUuid)).thenAccept(v->{
			SwingUtilities.invokeLater(()->{
				this.glassPane.setVisible(false);
			});
		});
	}
	
	@Override
	public void onClose(String reason) {
		System.exit(0);
	}

	public static void main(String[] args) {
		if (Platform.isWindows()) {
			SwingUtilities.invokeLater(()->{
				new EmbeddedWindowDemo();
			});
		}
		else {
			System.out.println("OpenFin Embedded NOT supported on this platform.");
		}
	}
}