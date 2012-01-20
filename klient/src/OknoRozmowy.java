import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;


public class OknoRozmowy extends JFrame
{
	private JTabbedPane zbiorZakladek = new JTabbedPane();
	private DefaultListModel ukryteZakladki = new DefaultListModel();
	Map<Integer, Integer> mapaPolozeniaRozmow = new HashMap<Integer, Integer>();
    private Kontakt kontaktJA;

	public OknoRozmowy(Kontakt kontaktJA) throws HeadlessException
	{
		super();
		setSize(500, 400);
		this.kontaktJA = kontaktJA;
		add(zbiorZakladek);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setVisible(false);
		zgloszenieDoOdczytywaniaWiadomosci();
	}

	private void zgloszenieDoOdczytywaniaWiadomosci()
    {
        WatekSieciowy.zgloszenieDoOdbieraniaWiadomosci(this);
    }

    public void dodajRozmowe(Kontakt osoba)
	{
		PanelRozmowy nowa = new PanelRozmowy(osoba, kontaktJA );
		
		if( ustawAktualnaRozmowa(osoba)) return;
		
		nowa.ustawId(osoba.getId());
		zbiorZakladek.addTab(osoba.getNazwa(), nowa);
		zbiorZakladek.setTabComponentAt(zbiorZakladek.getTabCount()-1, 
									new NaglowekZakladki(this));
		mapaPolozeniaRozmow.put(osoba.getId(), zbiorZakladek.getTabCount()-1);
		setVisible(true);
	}

    public boolean ustawAktualnaRozmowa(Kontakt osoba)
	{
		int id = osoba.getId();
		int numerWUkrytychZakladkach = -1;
		if ( !szukajWOtwartychRozmowach(id) )
		{
		    numerWUkrytychZakladkach = szukajWUkrytychRozmowach(id);
			if (numerWUkrytychZakladkach > -1 )
			{
			    przywrocZakladke(numerWUkrytychZakladkach);
	            setVisible(true);
	            return true;
			}
            setVisible(true);
			return false;
		}
		else 
		{
	        setVisible(true);
		    return true;
		}
	}

	private int szukajWUkrytychRozmowach(int id)
	{
		PanelRozmowy zakladka;
		for (int i = 0; i < ukryteZakladki.getSize(); i++)
		{
			zakladka = (PanelRozmowy) ukryteZakladki.get(i);
			//System.out.println(zakladka.zwrocId() + " " + id);
			if (zakladka.zwrocId() == id)
			{
				//System.out.println(i);
				return i;
			}
		}
		return -1;
	}

    private void przywrocZakladke(int i) {
        PanelRozmowy zakladka = (PanelRozmowy) ukryteZakladki.get(i);
        zbiorZakladek.addTab(zakladka.getRozmowcaNazwa(), zakladka);
        zbiorZakladek.setTabComponentAt(zbiorZakladek.getTabCount()-1, 
        							new NaglowekZakladki(this));
        zbiorZakladek.setSelectedIndex(i);
        ukryteZakladki.remove(i);
    }

	private boolean szukajWOtwartychRozmowach(int id)
	{
		PanelRozmowy zakladka;
		for (int i = 0; i < zbiorZakladek.getTabCount(); i++)
		{
			zakladka = (PanelRozmowy) zbiorZakladek.getComponentAt(i);
			//System.out.println(zakladka.zwrocId() + " " + id);
			if (zakladka.zwrocId() == id)
			{
				zbiorZakladek.setSelectedIndex(i);
				//System.out.println(i);
				return true;
			}
		}
		return false;
	}

    public void odbierzWiadomosci(ArrayList<Wiadomosc> odebraneWiadomosci)
    {
        int polozenie;
        PanelRozmowy panelRozmowy;
        Wiadomosc wiadomosc;
        this.setVisible(true);
        for(int i=0; i < odebraneWiadomosci.size() ; i++)
        {
            wiadomosc = odebraneWiadomosci.get(i);
            if( mapaPolozeniaRozmow.get(wiadomosc.getNadawca().getId()) == null ) dodajRozmowe(wiadomosc.getNadawca());
            polozenie = mapaPolozeniaRozmow.get(wiadomosc.getNadawca().getId());
            panelRozmowy = (PanelRozmowy) this.zbiorZakladek.getComponentAt(polozenie);
            panelRozmowy.odbierzWiadomosc(wiadomosc);
        }
    }
	
	public String zwrocNazweRozmowcy(int n)
	{
		return this.zbiorZakladek.getTitleAt(n);
	}
	
	public int zwrocNumerKomponentu(Component obiekt)
	{
		return this.zbiorZakladek.indexOfTabComponent(obiekt);
	}
	
	public int iloscKomponentow()
	{
		return this.zbiorZakladek.getTabCount();
	}
	
	public void ukryjRozmowe(int n)
	{
		PanelRozmowy zakladka = (PanelRozmowy) this.zbiorZakladek.getComponentAt(n);
		this.ukryteZakladki.addElement(zakladka);
		this.zbiorZakladek.remove(n);
	}

    public void zmienNickKontaktu(Kontakt kontakt,
            String ostatecznyNick)
    {
        if( this.mapaPolozeniaRozmow.get(kontakt.getId()) !=null)
        {
            int polozenie = this.mapaPolozeniaRozmow.get(kontakt.getId());
            this.zbiorZakladek.setTitleAt(polozenie, ostatecznyNick);
        }
    }
}

class NaglowekZakladki extends JPanel {
    private final OknoRozmowy okno;

    public NaglowekZakladki(final OknoRozmowy okno) {
    	
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (okno == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.okno = okno;
        setOpaque(false);
        
        JLabel label = new JLabel() {
            public String getText() {
                int i = okno.zwrocNumerKomponentu(NaglowekZakladki.this);
                if (i != -1) {
                    return okno.zwrocNazweRozmowcy(i);
                }
                return null;
            }
        };
        
        add(label);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        JButton button = new PrzyciskZakladki();
        add(button);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    private class PrzyciskZakladki extends JButton implements ActionListener {
        public PrzyciskZakladki() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            int i = okno.zwrocNumerKomponentu(NaglowekZakladki.this);
            if (i != -1) {
            	okno.ukryjRozmowe(i);
            }
            if (okno.iloscKomponentow() == 0) okno.setVisible(false);
        }

        //we don't want to update UI for this button
        public void updateUI() {
        }

        //paint the cross
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            if (getModel().isRollover()) {
                g2.setColor(Color.MAGENTA);
            }
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }

    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
}