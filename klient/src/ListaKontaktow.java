import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;


public class ListaKontaktow extends JList implements MouseListener
{
	static DefaultListModel kontakty = new DefaultListModel();
	int licznikKonwersacji = 0;
	GlowneOkno parent;
	
	public ListaKontaktow(GlowneOkno parent) {
		super(kontakty);
		this.parent = parent;
		kontakty.clear();
		Kontakt osoba = new Kontakt("Boro",1);
		Kontakt osoba1 = new Kontakt("Zorro",2);
		kontakty.add(0, osoba);
		kontakty.add(1, osoba1);
	    this.setVisibleRowCount(4);
		this.setCellRenderer(new ZnajomiCellRenderer());
		addMouseListener(this);
		// TODO Auto-generated constructor stub
	}
	
	public void dodajKontakt(String nazwa, int id) {
		int rozmiar;
		Kontakt nowyKontakt = new Kontakt(nazwa, id);
		rozmiar = kontakty.getSize();
		kontakty.add(rozmiar, nowyKontakt);
	}
	
	public void dodajKontakt(Kontakt osoba) {
		int rozmiar = kontakty.getSize();
		kontakty.add(rozmiar, osoba);
	}
	
	public void usunKontakt()
	{
		int n = getSelectedIndex();
		kontakty.remove(n);
	}
	
	public void usunKontakt(int id){
		for(int i=0; i<kontakty.getSize(); i++) {
			if (((Kontakt)kontakty.get(i)).getId() == id ) {
				kontakty.remove(i);
				break;
			}
		}
	}
	
	public ListaKontaktow(ListModel dataModel) {
		super(dataModel);
		this.setCellRenderer(new ZnajomiCellRenderer());
		// TODO Auto-generated constructor stub
	}

	@Override
	public void mouseClicked(MouseEvent arg0)
	{
		if( arg0.getClickCount()<2 ) return;
		
		int numer = this.locationToIndex(arg0.getPoint());
		Kontakt osoba = (Kontakt) kontakty.get(numer);
		if(!osoba.czyKonwersacja())
		{
			osoba.setKonwersacja(true);
			this.parent.dodajKarteRozmowy(osoba);
			this.licznikKonwersacji++;
		}
		else
		{
			this.parent.ustawAktualnaRozmowa(osoba);
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
}

class ZnajomiCellRenderer extends JLabel implements ListCellRenderer {
	  private static final Color HIGHLIGHT_COLOR = new Color(0, 0, 128);
	  private ImageIcon dost = new ImageIcon("/home/johnny/workspace/Komunikator/bin/dost1.gif");
	  private ImageIcon niedost = new ImageIcon("/home/johnny/workspace/Komunikator/bin/niedost.gif");
	  
	  public ZnajomiCellRenderer() {
	    setOpaque(true);
	    setIconTextGap(12);
	  }

	  public Component getListCellRendererComponent(JList list, Object value,
	      int index, boolean isSelected, boolean cellHasFocus) {
	    Kontakt obiekt = (Kontakt) value;
	    setText(obiekt.getNazwa());
	    
	    if (obiekt.isOnline())
	    {
	    	setIcon(dost);
	    }
	    else
	    {
	    	setIcon(niedost);
	    }
	    
	    if (isSelected) {
	      setBackground(HIGHLIGHT_COLOR);
	      setForeground(Color.white);
	    } else {
	      setBackground(Color.white);
	      setForeground(Color.black);
	    }
	    return this;
	  }
}