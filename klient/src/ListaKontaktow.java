import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;

/**
 * @author Jan
 * Przechowuje i wyœwietla listê kontaktów.
 */
public class ListaKontaktow extends JList implements MouseListener
{

    DefaultListModel kontakty = new DefaultListModel();
    int licznikKonwersacji = 0;
    GlowneOkno parent;
    Kontakt kontaktJA;
    private ZnajomiCellRenderer znajomiCellRenderer;

    public ListaKontaktow(GlowneOkno parent, Kontakt kontaktJA, DefaultListModel kontakty)
    {
        super(kontakty);
        this.kontakty = kontakty;
        this.parent = parent;
        this.kontaktJA = kontaktJA;
        this.setVisibleRowCount(10);
        this.setCellRenderer(new ZnajomiCellRenderer());
        addMouseListener(this);
        zgloszenieDoOdpytywania();
    }

    public ListaKontaktow(ListModel dataModel)
    {
        super(dataModel);
        znajomiCellRenderer = new ZnajomiCellRenderer();
        this.setCellRenderer(znajomiCellRenderer);
    }

    /**
     * Lista kontaktów zg³aszaj¹ siê do W¹tekSieciowy, by co jakiœ czas sprawdza³
     * dostêpnoœc znajomych na liœcie kontaków.
     */
    private void zgloszenieDoOdpytywania()
    {
        WatekSieciowy.zgloszenieDoOdpytywania(this);
    }

    /**
     * @param mapa
     * Funkcja wywo³ywana przez W¹tekSieciowy. Dziêki niej przesy³a siê aktualne wyniki
     * dostêpnoœci znajomych.
     */
    public void ustawStanyKontaktow(Map<Integer, Integer> mapa)
    {
        int licznik = 0;
        Set<Integer> zbiorKluczy = mapa.keySet();
        System.out.println("Wyniki: ");
        for (int id : zbiorKluczy)
        {
            System.out.println(id + " " + mapa.get(id));
            ustawStan(id, mapa.get(id));
        }
        this.validate();
        this.repaint();
    }

    /**
     * @param id
     * @param status
     * Ustawia status (dostêpny/niedostêpny) kontaktu o danym id.
     */
    private void ustawStan(int id, Integer status)
    {
        Kontakt kontakt;
        for (int i = 0; i < kontakty.size(); i++)
        {
            kontakt = (Kontakt) kontakty.get(i);
            if (kontakt.getId() == id)
            {
                if (status == 0)
                    kontakt.setOnline(false);
                else
                    kontakt.setOnline(true);
            }
        }
    }

    /**
     * @param idKontaktu
     * @return
     * Zwraca nick kontaktu o podanym id.
     */
    public String zwrocNick(int idKontaktu)
    {
        Kontakt kontakt;
        for (int i = 0; i < kontakty.size(); i++)
        {
            kontakt = (Kontakt) kontakty.get(i);
            if (kontakt.getId() == idKontaktu)
            {
                return kontakt.getNazwa();
            }
        }
        String nowyNick = "Nieznany " + String.valueOf(idKontaktu);
        Kontakt nieznany = new Kontakt(nowyNick, idKontaktu);
        dodajKontakt(nieznany);
        return nowyNick;
    }

    public Kontakt zwrocZaznaczonyKontakt()
    {
        int numer = this.getSelectedIndex();
        Kontakt kontakt = (Kontakt) this.kontakty.get(numer);
        return kontakt;
    }

    /**
     * @param ostatecznyNick
     * Zwraca nick kontaktu który jest zaznaczony na liœcie.
     */
    public void zmienZaznaczonyNick(String ostatecznyNick)
    {
        int numer = this.getSelectedIndex();
        Kontakt kontakt = (Kontakt) this.kontakty.get(numer);
        kontakt.setNazwa(ostatecznyNick);
    }

    public void dodajKontakt(String nazwa, int id)
    {
        int rozmiar;
        Kontakt nowyKontakt = new Kontakt(nazwa, id);
        rozmiar = kontakty.getSize();
        kontakty.add(rozmiar, nowyKontakt);
    }

    public void dodajKontakt(Kontakt osoba)
    {
        int rozmiar = kontakty.getSize();
        kontakty.add(rozmiar, osoba);
    }

    /**
     * Usuwa zaznaczony kontakt.
     */
    public void usunKontakt()
    {
        int n = getSelectedIndex();
        kontakty.remove(n);
    }

    /**
     * @param id
     * Usuwa z listy znajomych kontakt o podanym id.
     */
    public void usunKontakt(int id)
    {
        for (int i = 0; i < kontakty.getSize(); i++)
        {
            if (((Kontakt) kontakty.get(i)).getId() == id)
            {
                kontakty.remove(i);
                break;
            }
        }
    }
    
    public DefaultListModel zwrocKontakty()
    {
        return kontakty;
    }
    
    /**
     * @return
     * Zwraca w tablicy numery ID wszystkich kontaktów znajduj¹cych siê na liœcie.
     */
    public short[] zwrocTabliceID()
    {
        short[] tablica = new short[kontakty.size()];
        for(int i=0; i<kontakty.size();i++)
        {
            tablica[i] = (short)((Kontakt)kontakty.get(i)).getId();
        }
        return tablica;
        
    }

    @Override
    public void mouseClicked(MouseEvent arg0)
    {
        if (arg0.getClickCount() < 2)
            return;

        int numer = this.locationToIndex(arg0.getPoint());
        Kontakt osoba = (Kontakt) kontakty.get(numer);
        if (!osoba.czyKonwersacja() || (osoba.czyKonwersacja() && osoba.getId() == kontaktJA.getId()))
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

    public DefaultListModel zwrocKontaktyDoZapisu()
    {
        Kontakt osoba;
        for(int i=0; i<kontakty.size();i++)
        {
            osoba = (Kontakt) kontakty.get(i);
            osoba.setKonwersacja(false);
        }
        return kontakty;
    }
}
