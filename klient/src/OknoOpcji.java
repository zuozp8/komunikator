import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;


public class OknoOpcji extends JFrame
{
	JTabbedPane zakladki;
	JTextField poleAdresIP;

    JTextField poleNumerPortu;
    JTextField poleNick;

	OknoOpcji()
	{
		super();
		zakladki = new JTabbedPane();
		add(zakladki);
		//ustawOgraniczenia();
		utworzKartePolaczenia();
		ustawieniaOkna();
	}

	private void utworzKartePolaczenia()
	{
		SpringLayout layout = new SpringLayout();
		JPanel panel = new JPanel();
		JLabel labelIP = new JLabel("Adres IP serwera");
		JLabel labelPort = new JLabel("Numer portu");
        JLabel labelNick = new JLabel("Twój nick");
		poleAdresIP = new JTextField(20);
		poleNumerPortu = new JTextField(8);
		poleNick = new JTextField(20);

		panel.setLayout(layout);
		panel.add(labelIP);
		panel.add(poleAdresIP);
		panel.add(labelPort);
		panel.add(poleNumerPortu);
        panel.add(labelNick);
        panel.add(poleNick);
		
		layout.putConstraint(SpringLayout.WEST,  labelIP, 10, SpringLayout.WEST,  panel);
		layout.putConstraint(SpringLayout.NORTH, labelIP, 25, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, poleAdresIP, 25, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, poleAdresIP, 20, SpringLayout.EAST, labelIP);

		layout.putConstraint(SpringLayout.WEST,  labelPort, 10, SpringLayout.WEST,  panel);
		layout.putConstraint(SpringLayout.NORTH, labelPort, 65, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, poleNumerPortu, 65, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, poleNumerPortu, 50, SpringLayout.EAST, labelPort);
		
		layout.putConstraint(SpringLayout.WEST,  labelNick, 10, SpringLayout.WEST,  panel);
        layout.putConstraint(SpringLayout.NORTH, labelNick, 105, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, poleNick, 105, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, poleNick, 80, SpringLayout.EAST, labelNick);
		
		panel.setSize(400, 300);
		panel.setVisible(true);

		this.zakladki.addTab("Ustawienia",panel);
		ustawieniaDomyslne();
	}

    private void ustawieniaOkna()
	{
		setSize(400,300);
		setVisible(true);
		this.setResizable(false);
	}
    
    public String zwrocAdresIP()
    {
        return poleAdresIP.getText();
    }
    
    public int zwrocNumerPortu()
    {
        try
        {
            int port = Integer.valueOf(poleNumerPortu.getText());
            return port;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }
    
    private void ustawieniaDomyslne()
    {
        poleAdresIP.setText("127.0.0.1");
        poleNumerPortu.setText("4790");
    }
}
