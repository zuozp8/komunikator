import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;


/**
 * @author Jan
 * Zarz¹dza pojedyñcz¹ rozmow¹, zawiera okno wpisywania wiadomoœci, okno rozmowy i przycisk wys³ania.
 */
class PanelRozmowy extends JPanel
{
	final int GRUBA = 1;
	final int NORMALNA = 0;
	private FlowLayout layout;
	private JTextPane poleRozmowy;
	private JTextArea poleWiadomosci;
	private JScrollPane panelRozmowy;
	private JScrollPane panelWiadomosci;
	private JButton wyslij;
	
	private Kontakt kontaktJA;
	private Kontakt rozmowca;

	private int id;
	private int myWidth = 350, myHeight = 250;
	private StyledDocument doc;
	private Style regular;
	private Style bold;
    //private Kontakt kontaktJA;

	public PanelRozmowy(Kontakt osoba, Kontakt ja)
	{
		super();
		setSize(myWidth, myHeight);
		
		dodajPanelRozmowy();
		dodajPanelWiadomosci();
		dodajPrzyciskWyslania(); 
		
		rozmowca = osoba;
		kontaktJA = ja;

		ustawLayout();
		utworzStyle();
	}

	private void dodajPrzyciskWyslania()
	{
		wyslij = new JButton("Wyslij");
		wyslij.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                wyslijWiadomosc();
            }
        });
	}

	/**
	 * Dodaje panel rozmowy tzn. miejsce gdzie wyœwietlana bêdzie konwersacja.
	 */
	private void dodajPanelRozmowy()
	{
		poleRozmowy = new JTextPane();
		poleRozmowy.setEditable(false);
		panelRozmowy = new JScrollPane(poleRozmowy);
	}

	/**
	 * Dodaje panel wiadomoœci tzn. miejsce wpisania wiadomoœci.
	 */
	private void dodajPanelWiadomosci()
	{
		poleWiadomosci = new JTextArea();
		panelWiadomosci = new JScrollPane(poleWiadomosci);
		poleWiadomosci.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0)
			{
				if(arg0.getKeyCode() == arg0.VK_ENTER)
				{
					wyslijWiadomosc();
					arg0.consume();
				}
			}
			
			@Override
			public void keyReleased(KeyEvent arg0)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyTyped(KeyEvent arg0)
			{
				// TODO Auto-generated method stub
				
			}
			
		});
	}

	private void ustawLayout()
	{
		layout = new FlowLayout();
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 120;
		c.weightx = 1;
		c.weighty = 1.3;
		c.gridx = 1;
		c.gridy = 0;
		this.add(panelRozmowy, c);

		c.fill = GridBagConstraints.BOTH;
		c.ipady = 100; // make this component tall
		c.weightx = 1;
		c.weighty = 1.9;
		c.gridx = 1;
		c.gridy = 1;
		this.add(panelWiadomosci, c);

		c.fill = GridBagConstraints.EAST;
		c.anchor = GridBagConstraints.WEST;
		c.ipady = 10;
		c.ipadx = 10;
		c.weightx = 1;
		c.weighty = 0.1;
		c.gridx = 1;
		c.gridy = 2;
		add(wyslij, c);
	}

	/**
	 * Tworzy style które bêd¹ u¿ywane w polu rozmowy.
	 */
	private void utworzStyle()
	{
		doc = this.poleRozmowy.getStyledDocument();
		Style def = StyleContext.getDefaultStyleContext().getStyle( StyleContext.DEFAULT_STYLE );
	    regular = doc.addStyle( "regular", def );
	    
	    bold = doc.addStyle( "bold", regular );
	    StyleConstants.setBold( bold, true );
	}
	
	public void odbierzWiadomosc(Wiadomosc wiadomosc)
	{
        wyslijWiadomoscDoPolaRozmowy(wiadomosc.zwrocTresc(), true);
	}

	/**
	 * Wysy³a wiadomosc u¿ytkownika klienta do jego rozmówcy, dodaje wiadomosc
	 * do okna rozmowy i czysci pole z wpisan¹ wczesniej wiadomoscia.
	 */
	private void wyslijWiadomosc()
	{
		String wiadomosc = this.poleWiadomosci.getText();
		if(wiadomosc.trim().length() == 0) return;
		else wiadomosc += "\n";
		wyczyscPoleWiadomosci();
		WatekSieciowy.dodajWiadomosc(wiadomosc,zwrocObecnyCzas(),rozmowca);
		wyslijWiadomoscDoPolaRozmowy(wiadomosc, false);
	}

	private void wyczyscPoleWiadomosci()
	{
		this.poleWiadomosci.setText("");
	}

	/**
	 * @param wiadomosc
	 * @param przychodzaca
	 * Dodaje tresc wiadomosci do pola rozmowy, jak rowniez przygotowuje nag³ówek czyli Nick
	 * nadawcy i czas.
	 */
	private void wyslijWiadomoscDoPolaRozmowy(String wiadomosc, boolean przychodzaca)
	{
		try
		{
			String poczatek = this.stworzRozpoczecieWiadomosci(przychodzaca);
			doc.insertString(doc.getLength(), poczatek,  this.bold);
			doc.insertString(doc.getLength(), wiadomosc, this.regular);
		}
		catch (BadLocationException e) {
		     // e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
		}
	}

	/**
	 * @param przychodzaca
	 * @return
	 * Tworzy nag³ówek wiadomoœci tzn. Nick nadawcy i czas dostarczenia(w przypadku przychodz¹cych)
	 * lub nadania (w przypadku wychodz¹cych).
	 */
	private String stworzRozpoczecieWiadomosci(boolean przychodzaca)
	{
		String poczatek = new String();
		if(this.poleRozmowy.getText().length() > 0) poczatek = "\n";
 	   	if(!przychodzaca)
 	   		poczatek += this.kontaktJA.getNazwa() + " " + this.zwrocObecnyCzas() + " :\n ";
 	   	else
 	   		poczatek += this.rozmowca.getNazwa() + " " + this.zwrocObecnyCzas() + " :\n ";
		return poczatek;
	}

	private void ustawCzcionke(int wartosc)
	{
		if(wartosc == this.GRUBA)
		{
			Font font = new Font("Serif", Font.BOLD, 12);
	        this.poleRozmowy.setFont(font);
		}
		else if(wartosc == this.NORMALNA)
		{
			Font font = new Font("Serif", Font.PLAIN, 12);
	        this.poleRozmowy.setFont(font);
		}
	}

	private String zwrocObecnyCzas()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
 	   	return dateFormat.format(cal.getTime());
	}

	public void ustawId(int id)
	{
		this.id = id;
	}

	public int zwrocId()
	{
		return id;
	}
	
	public String getRozmowcaNazwa()
	{
		return this.rozmowca.getNazwa();
	}

}
