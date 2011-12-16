import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;


public class OknoLogowania extends JDialog
{

    private JPasswordField poleHaslo;
    private JFormattedTextField poleId;
    private JLabel lbHaslo;
    private JLabel lbId;
    private JButton btnLogowania;
    private JButton btnBrakLogowania;
    private Kontakt osoba;
    private boolean succeeded;

    public OknoLogowania(JFrame parent)
    {
    	super(parent,"Logowanie",true);
    	
        JPanel panelKomponentow = inicjalizujKomponenty();
        JPanel panelPrzyciskow = inicjalizujPanelPrzyciskow();
        inicjalizujOkno(parent, panelKomponentow, panelPrzyciskow);
    }

	private void inicjalizujOkno(JFrame parent, JPanel panel, JPanel bp)
	{
		getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    	setSize(300,150);
	}

	private JPanel inicjalizujPanelPrzyciskow()
	{
		JPanel bp = new JPanel();

        btnLogowania = new JButton("Zaloguj się");
        btnLogowania.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            	//wynik = logowanie(Integer.parseInt(poleId.getText()), poleHaslo.getPassword())
            	//osoba = new Kontakt(poleHaslo.getText(), Integer.parseInt(poleId.getText()) );
            	dispose();
            }
        });
        
        btnBrakLogowania = new JButton("Nie loguj się");
        btnBrakLogowania.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            	osoba = null;
            	dispose();
            }
        });
        
        
        bp.add(btnLogowania);
        bp.add(btnBrakLogowania);
		return bp;
	}

	private JPanel inicjalizujKomponenty()
	{
		JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        lbId = new JLabel("ID: ");
        cs.weighty = 10;
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbId, cs);

        poleId = new JFormattedTextField(NumberFormat.getInstance());
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(poleId, cs);

        lbHaslo = new JLabel("Hasło: ");
        cs.weighty = 10;
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbHaslo, cs);

        poleHaslo = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(poleHaslo, cs);
        panel.setBorder(new LineBorder(Color.GRAY));
		return panel;
	}
    
    public Kontakt zwrocWynik()
    {
    	this.setVisible(true);
    	return osoba;
    }
}
