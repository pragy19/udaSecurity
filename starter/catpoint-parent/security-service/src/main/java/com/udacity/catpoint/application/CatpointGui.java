package com.udacity.catpoint.application;

import com.udacity.catpoint.data.PretendDatabaseSecurityRepositoryImpl;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.service.SecurityService;
import com.udacity.image.FakeImageService; // <-- This is the corrected import
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * This is the primary JFrame for the application that contains all the top-level JPanels.
 *
 * We're not using any dependency injection framework, so this class also handles constructing
 * all our dependencies and providing them to other classes as necessary.
 */
public class CatpointGui extends JFrame {
    private static final long serialVersionUID = 1L;

    private transient SecurityRepository securityRepository;
    private transient FakeImageService imageService;
    private transient SecurityService securityService;
    private DisplayPanel displayPanel;
    private ControlPanel controlPanel;
    private SensorPanel sensorPanel;
    private ImagePanel imagePanel;

    public CatpointGui() {
        securityRepository = new PretendDatabaseSecurityRepositoryImpl();
        imageService = new FakeImageService();
        securityService = new SecurityService(securityRepository, imageService);
        displayPanel = new DisplayPanel();
        controlPanel = new ControlPanel(securityService);
        sensorPanel = new SensorPanel(securityService);
        imagePanel = new ImagePanel(securityService);

        displayPanel.registerWith(securityService);
        imagePanel.registerWith(securityService);

        setLocation(100, 100);
        setSize(600, 850);
        setTitle("Very Secure App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new MigLayout());
        mainPanel.add(displayPanel, "wrap");
        mainPanel.add(imagePanel, "wrap");
        mainPanel.add(controlPanel, "wrap");
        mainPanel.add(sensorPanel);

        super.getContentPane().add(mainPanel);
    }
}