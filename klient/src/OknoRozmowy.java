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

	public OknoRozmowy() throws HeadlessException
	{
		super();
		setSize(500, 400);
		
		add(zbiorZakladek);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}

	public void dodajRozmowe(Kontakt osoba)
	{
		PanelRozmowy nowa = new PanelRozmowy(osoba);
		nowa.ustawId(osoba.getId());
		zbiorZakladek.addTab(osoba.getNazwa(), nowa);
		zbiorZakladek.setTabComponentAt(zbiorZakladek.getTabCount()-1, 
									new NaglowekZakladki(this));

		setVisible(true);
	}

	public void ustawAktualnaRozmowa(Kontakt osoba)
	{
		int id = osoba.getId();
		
		if ( !szukajWOtwartychRozmowach(id) )
		{
			szukajWUkrytychRozmowach(id);
		}
		
		setVisible(true);
	}

	private void szukajWUkrytychRozmowach(int id)
	{
		PanelRozmowy zakladka;
		for (int i = 0; i < ukryteZakladki.getSize(); i++)
		{
			zakladka = (PanelRozmowy) ukryteZakladki.get(i);
			System.out.println(zakladka.zwrocId() + " " + id);
			if (zakladka.zwrocId() == id)
			{
				zbiorZakladek.addTab(zakladka.getRozmowcaNazwa(), zakladka);
				zbiorZakladek.setTabComponentAt(zbiorZakladek.getTabCount()-1, 
											new NaglowekZakladki(this));
				zbiorZakladek.setSelectedIndex(i);
				ukryteZakladki.remove(i);
				System.out.println(i);
				return;
			}
		}
	}

	private boolean szukajWOtwartychRozmowach(int id)
	{
		PanelRozmowy zakladka;
		for (int i = 0; i < zbiorZakladek.getTabCount(); i++)
		{
			zakladka = (PanelRozmowy) zbiorZakladek.getComponentAt(i);
			System.out.println(zakladka.zwrocId() + " " + id);
			if (zakladka.zwrocId() == id)
			{
				zbiorZakladek.setSelectedIndex(i);
				System.out.println(i);
				return true;
			}
		}
		return false;
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
        
        //make JLabel read titles from JTabbedPane
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
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //tab button
        JButton button = new PrzyciskZakladki();
        add(button);
        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    private class PrzyciskZakladki extends JButton implements ActionListener {
        public PrzyciskZakladki() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
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