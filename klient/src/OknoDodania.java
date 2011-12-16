import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.border.*;

public class OknoDodania extends JDialog {

    private JTextField poleNazwa;
    private JFormattedTextField poleId;
    private JLabel lbNazwa;
    private JLabel lbId;
    private JButton btnDodaj;
    private Kontakt osoba;
    private boolean succeeded;

    public OknoDodania(Frame obiektNadrzeny) {
        super(obiektNadrzeny, "Dodawanie znajomego", true);
        
        JPanel panelKomponentow = inicjalizujKomponenty();
        JPanel panelPrzycisku = inicjalizujPrzycisk();

        inicjalizujOkno(obiektNadrzeny, panelKomponentow, panelPrzycisku);
    }

	private void inicjalizujOkno(Frame parent, JPanel panel, JPanel bp)
	{
		getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
	}

	private JPanel inicjalizujPrzycisk()
	{
		JPanel bp = new JPanel();
        btnDodaj = new JButton("Dodaj");
        btnDodaj.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            	osoba = new Kontakt(poleNazwa.getText(), Integer.parseInt(poleId.getText()) );
            	dispose();
            }
        });
        bp.add(btnDodaj);
		return bp;
	}

	private JPanel inicjalizujKomponenty()
	{
		JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        lbNazwa = new JLabel("Nick: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbNazwa, cs);

        poleNazwa = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(poleNazwa, cs);

        lbId = new JLabel("ID: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbId, cs);

        poleId = new JFormattedTextField(NumberFormat.getInstance());
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(poleId, cs);
        panel.setBorder(new LineBorder(Color.GRAY));
		return panel;
	}

    public String getUsername() {
        return poleNazwa.getText().trim();
    }

    public int getId() {
        return Integer.parseInt(poleId.getText());
    }

    public boolean isSucceeded() {
        return succeeded;
    }
    
    public Kontakt zwrocWynik()
    {
    	this.setVisible(true);
    	return osoba;
    }
}