import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;


public class OknoOpcji extends JFrame
{
	JTabbedPane zakladki;
	JTextField poleAdresIP;
	JTextField poleNumerPortu;
	/*
	private GridBagConstraints ograniczeniaWzor = null;
    private GridBagConstraints ograniczeniaPol = null;
    private GridBagConstraints ograniczeniaLabel = null;
	*/
	OknoOpcji()
	{
		super();
		zakladki = new JTabbedPane();
		add(zakladki);
		//ustawOgraniczenia();
		utworzKartePolaczenia();
		ustawieniaOkna();
	}
	/*
    public void ustawOgraniczenia() {
    	ograniczeniaWzor = new GridBagConstraints();
    	ograniczeniaWzor.fill = GridBagConstraints.HORIZONTAL;
    	ograniczeniaWzor.anchor = GridBagConstraints.NORTHWEST;
        ograniczeniaWzor.weightx = 1.0;
        ograniczeniaWzor.gridwidth = GridBagConstraints.REMAINDER;
        ograniczeniaWzor.insets = new Insets(1, 1, 1, 1);

        ograniczeniaPol = (GridBagConstraints) ograniczeniaWzor.clone();
        ograniczeniaPol.gridwidth = GridBagConstraints.RELATIVE;

        ograniczeniaLabel = (GridBagConstraints) ograniczeniaWzor.clone();
        ograniczeniaLabel.weightx = 0.0;
        ograniczeniaLabel.gridwidth = 1;
    }
*/
	private void utworzKartePolaczenia()
	{
		SpringLayout layout = new SpringLayout();
		JPanel panel = new JPanel();
		JLabel labelIP = new JLabel("Adres IP serwera");
		JLabel labelPort = new JLabel("Numer portu");
		poleAdresIP = new JTextField(20);
		poleNumerPortu = new JTextField(8);

		panel.setLayout(layout);
		panel.add(labelIP);
		panel.add(poleAdresIP);
		panel.add(labelPort);
		panel.add(poleNumerPortu);
		
		layout.putConstraint(SpringLayout.WEST,  labelIP, 10, SpringLayout.WEST,  panel);
		layout.putConstraint(SpringLayout.NORTH, labelIP, 25, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, poleAdresIP, 25, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, poleAdresIP, 20, SpringLayout.EAST, labelIP);

		layout.putConstraint(SpringLayout.WEST,  labelPort, 10, SpringLayout.WEST,  panel);
		layout.putConstraint(SpringLayout.NORTH, labelPort, 65, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, poleNumerPortu, 65, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, poleNumerPortu, 50, SpringLayout.EAST, labelPort);
		
		panel.setSize(400, 300);
		panel.setVisible(true);

		this.zakladki.addTab("Połączenie",panel);
	}

	private void ustawieniaOkna()
	{
		setSize(400,300);
		setVisible(true);
		this.setResizable(false);
	}
}
