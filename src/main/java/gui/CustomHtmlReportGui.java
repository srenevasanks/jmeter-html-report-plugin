package gui;

import listener.CustomHtmlReportListener;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import javax.swing.*;
import java.awt.*;

public class CustomHtmlReportGui extends AbstractJMeterGuiComponent {

    private static final long serialVersionUID = 1L;

    public CustomHtmlReportGui() {
        super();
        init();
    }

    @Override
    public String getStaticLabel() {
        return "API Automation HTML Report";
    }

    @Override
    public String getLabelResource() {
        return getClass().getCanonicalName(); // Not using properties file for simplicity
    }

    @Override
    public TestElement createTestElement() {
        CustomHtmlReportListener listener = new CustomHtmlReportListener();
        modifyTestElement(listener);
        return listener;
    }

    private JTextField reportPathField;
    private JTextField passThresholdField;

    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
        element.setProperty(CustomHtmlReportListener.REPORT_PATH, reportPathField.getText());
        element.setProperty(CustomHtmlReportListener.PASS_THRESHOLD, passThresholdField.getText());
    }

    @Override
    public void configureTestElement(TestElement element) {
        super.configureTestElement(element);
        reportPathField.setText(element.getPropertyAsString(CustomHtmlReportListener.REPORT_PATH));
        passThresholdField.setText(element.getPropertyAsString(CustomHtmlReportListener.PASS_THRESHOLD));
    }

    private void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new VerticalPanel();
        add(mainPanel, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel(
                "<html><p>This listener generates a custom HTML report.</p></html>");
        mainPanel.add(infoLabel);

        mainPanel.add(Box.createVerticalStrut(10));

        // Report Path
        JPanel pathPanel = new JPanel(new BorderLayout(5, 0));
        pathPanel.add(new JLabel("Report Output Path (Optional, default location is where the current jmx file is placed):"), BorderLayout.WEST);
        reportPathField = new JTextField();
        pathPanel.add(reportPathField, BorderLayout.CENTER);
        JButton browseBtn = new JButton("Browse...");
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (!reportPathField.getText().isEmpty()) {
                chooser.setCurrentDirectory(new java.io.File(reportPathField.getText()));
            }
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                reportPathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        pathPanel.add(browseBtn, BorderLayout.EAST);
        mainPanel.add(pathPanel);

        mainPanel.add(Box.createVerticalStrut(5));

        // Pass Threshold
        JPanel thresholdPanel = new JPanel(new BorderLayout(5, 0));
        thresholdPanel.add(new JLabel("Pass Threshold % (default: 90):"), BorderLayout.WEST);
        passThresholdField = new JTextField();
        thresholdPanel.add(passThresholdField, BorderLayout.CENTER);
        mainPanel.add(thresholdPanel);
    }

    @Override
    public java.util.Collection<String> getMenuCategories() {
        return java.util.Collections.singletonList(org.apache.jmeter.gui.util.MenuFactory.LISTENERS);
    }

    @Override
    public JPopupMenu createPopupMenu() {
        return org.apache.jmeter.gui.util.MenuFactory.getDefaultVisualizerMenu();
    }
}
