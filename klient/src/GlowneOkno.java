import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

public class GlowneOkno
{

    private JFrame frame;
    private Kontakt kontaktJA;
    private ListaKontaktow listaKontaktow;
    private OknoRozmowy oknoRozmowy;
    WatekSieciowy wSiec;
    //private OknoOpcji oknoOpcji;
    private int wynikLogowania;
    private Thread watekWS;
    private boolean polaczenie = false;
    private String adres;
    private int port;

    /**
     * Launch the application.
     */
    public static void main(String[] args)
    {
        /*
         * EventQueue.invokeLater(new Runnable() {
         * 
         * public void run() {
         */
        try
        {
            UIManager
                    .setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            GlowneOkno window = new GlowneOkno();
            window.frame.setVisible(true);
            if (window.frame == null) return;
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        /*
         * } });
         */
    }

    /**
     * Konstrukotr przygotowuj�cy aplikacj�. Inicjuje po��czenie sieciowe,
     * zaczyna procedur� logowania/rejestracj, tworzy list� kontakt�w i okno rozm�w.
     */
    public GlowneOkno()
    {
        initialize();

        //utworzOknoOpcji();
        pobierzDanePolaczenia();
        utworzWatekSieciowy();
        polaczZKontem();
        inicjalizujListeKontaktow();
        inicjalizujOknoRozmowy();
    }

    /**
     * W tej funkcji nast�puje inicjalizacja niekt�rych element�w GUI jak menu
     */
    private void initialize()
    {
        frame = new JFrame();
        frame.setBounds(100, 100, 280, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu mnRotlfmao = new JMenu("ROTLF");
        menuBar.add(mnRotlfmao);

        /*JMenuItem mntmZalogujSie = new JMenuItem("Zaloguj sie");
        mntmZalogujSie.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {

                // polaczZKontem();
            }
        });
        mnRotlfmao.add(mntmZalogujSie);*/

        /*JMenuItem mntmZmienProfil = new JMenuItem("Zmien profil");
        mnRotlfmao.add(mntmZmienProfil);

        JMenuItem mntmOpcje = new JMenuItem("Opcje");
        mntmOpcje.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent arg0)
            {
                otworzOknoOpcji();
            }
        });
        mnRotlfmao.add(mntmOpcje);*/

        JMenuItem mntmZamknij = new JMenuItem("Zamknij");
        mntmZamknij.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                zakoncz();
            }
        });
        mnRotlfmao.add(mntmZamknij);

        JMenu mnKontakty = new JMenu("Kontakty");
        menuBar.add(mnKontakty);

        JMenuItem mntmDodajZnajomego = new JMenuItem("Dodaj znajomego");
        mntmDodajZnajomego.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent arg0)
            {
                dodajZnajomego();
            }
        });
        mnKontakty.add(mntmDodajZnajomego);

        JMenuItem mntmEdytujZnajomego = new JMenuItem("Edytuj znajomego");
        mntmEdytujZnajomego.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent arg0)
            {
                edytujZnajomego();
            }
        });
        mnKontakty.add(mntmEdytujZnajomego);

        JMenuItem mntmUsuZnajomego = new JMenuItem("Usu� znajomego");
        mntmUsuZnajomego.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent arg0)
            {
                if (listaKontaktow.getSelectedIndex() == -1) bladUsuniecia();
                else if (czyChceszUsunac()) listaKontaktow.usunKontakt();
            }
        });
        mnKontakty.add(mntmUsuZnajomego);

        /*JMenuItem mntmRozpocznijRozmow = new JMenuItem("Rozpocznij rozmowę");
        mnKontakty.add(mntmRozpocznijRozmow);

        JMenuItem mntmArchiwum = new JMenuItem("Archiwum");
        mnKontakty.add(mntmArchiwum);*/
    }

    /**
     * Pobiera dane po��czenia(adres ip serwera i numer portu) z pliku tekstowego
     */
    private void pobierzDanePolaczenia()
    {
        try
        {
            FileInputStream fstream = new FileInputStream("polaczenie.txt");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            adres = br.readLine();
            port = Integer.valueOf(br.readLine());
            in.close();
        }
        catch (Exception e)
        {
            //System.err.println("Error: " + e.getMessage());
            adres = "192.168.1.110";
            port = 4790;
        }
    }

    /**
     * @return
     * Wczytuje listy znajomych z pliku
     */
    private DefaultListModel wczytajDane()
    {
        try
        {
            File plik = new File(String.valueOf(kontaktJA.getId()));
            InputStream file = new FileInputStream(plik);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            kontaktJA = (Kontakt) input.readObject();
            DefaultListModel kontakty = (DefaultListModel) input.readObject();
            input.close();
            return kontakty;
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        return new DefaultListModel();
    }

    /**
     * Zapisuje list� znajomych do pliku
     */
    public void zapiszDane()
    {
        try
        {
            File plik = new File(String.valueOf(kontaktJA.getId()));
            if (!plik.exists()) plik.createNewFile();
            OutputStream file = new FileOutputStream(plik);
            file.write((new String()).getBytes());
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(this.kontaktJA);
            output.writeObject(this.listaKontaktow.zwrocKontaktyDoZapisu());
            output.close();
        }
        catch (Exception e)
        {
           // e.printStackTrace();
        }
    }

    private void inicjalizujListeKontaktow()
    {
        DefaultListModel dane = wczytajDane();
        listaKontaktow = new ListaKontaktow(this, kontaktJA, dane);
        JPanel panelListy = utworzPanelListy();
        dodajPrzewijanie(panelListy);
    }

    private JPanel utworzPanelListy()
    {
        JPanel panelListy = new JPanel(new BorderLayout());
        panelListy.add(listaKontaktow, BorderLayout.NORTH);
        panelListy.setBackground(new Color(255, 255, 255));
        return panelListy;
    }

    private void dodajPrzewijanie(JPanel panelListy)
    {
        JScrollPane pane = new JScrollPane(panelListy);
        frame.getContentPane().add(pane);
        pane.setSize(150, 150);
    }

    private void inicjalizujOknoRozmowy()
    {
        oknoRozmowy = new OknoRozmowy(kontaktJA);
    }

    private void utworzWatekSieciowy()
    {
        wSiec = new WatekSieciowy(adres, port, this);
        watekWS = new Thread(wSiec);
        watekWS.start();
    }

    /**
     * Wywo�uje okno logowania, kt�re b�dzie wy�wietlane do momentu
     * poprawnego zalogowania albo przerwania po��czenia
     */
    private void polaczZKontem()
    {
        int wynik = 0;
        while (wynik != 1)
        {
            OknoLogowania oknoLogowania = new OknoLogowania(this.frame);
            int daneUzytkownika = oknoLogowania.zwrocWynik();
            String haslo = new String(oknoLogowania.zwrocHaslo());
            if (oknoLogowania.czyRejestracja())
            {
                rejestracja(haslo);
            }
            else if (daneUzytkownika > 0)
            {
                kontaktJA = new Kontakt("Ja", daneUzytkownika);
                wynik = logowanie(daneUzytkownika, haslo);
            }
            else if (daneUzytkownika == -2)
            {
                this.zakoncz();
            }
        }
    }

    /**
     * @param daneUzytkownika
     * @param haslo
     * @return
     * Zarz�dza procedur� logowania
     */
    private int logowanie(final int daneUzytkownika, final String haslo)
    {
        int wynikLogowania = -1;
        try
        {
            WatekSieciowy.zalogujSie((short) daneUzytkownika, haslo);
            while (true)
            {
                Thread.sleep(100);
                wynikLogowania = WatekSieciowy.wynikLogowania();
                if (wynikLogowania > -1) break;
            }
        }
        catch (InterruptedException e)
        {
            //e.printStackTrace();
        }
        if(wynikLogowania == 0) bladLogowania();
        System.out.println(wynikLogowania);
        return wynikLogowania;
    }

    /**
     * @param haslo
     * Zarz�dza procesem rejestracji
     */
    private void rejestracja(final String haslo)
    {
        int id = 0;
        WatekSieciowy.zarejestrujSie(haslo);
        try
        {
            while (true)
            {
                Thread.sleep(1000);
                id = WatekSieciowy.wynikRejestracji();
                System.out.println(id);
                if (id != -1) break;
            }
        }
        catch (InterruptedException e)
        {
            //e.printStackTrace();
        }
        if (id > 0) poprawnaRejestracja(id);
        else bladRejestracji();
        return;
    }

    private void poprawnaRejestracja(int id)
    {
        JOptionPane.showMessageDialog(frame,
                "Zarejestrowa�e� si� poprawnie. Twoje id: " + id);
    }

    public void bladRejestracji()
    {
        JOptionPane.showMessageDialog(frame,
                "B��dna rejestracja. Spr�buj jeszcze raz");
    }

    public void brakPolaczenia()
    {
        JOptionPane.showMessageDialog(frame,
                "Brak po��czenia z serwerem. Nast�puje zamkni�cie programu.");
    }

    public void bladLogowania()
    {
        JOptionPane.showMessageDialog(frame,
        "B��dne logowanie.");
    }

    protected void bladUsuniecia()
    {
        JOptionPane.showMessageDialog(frame,
                "Najpierw zaznacz kontakt z listy!", "B��d",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @return
     * Wy�wietla okno potwierdzenia czy chce usun�c dany kontakt
     */
    protected boolean czyChceszUsunac()
    {
        Object[] options = { "Tak", "Nie" };
        int n = JOptionPane.showOptionDialog(frame,
                "Czy na pewno chcesz usun�c zaznaczony kontakt?",
                "Czy aby na pewno?", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (n == 0) return true;
        else return false;
    }

    public void dodajKarteRozmowy(Kontakt osoba)
    {
        oknoRozmowy.dodajRozmowe(osoba);
    }

    public void ustawAktualnaRozmowa(Kontakt osoba)
    {
        oknoRozmowy.ustawAktualnaRozmowa(osoba);
    }

    private void dodajZnajomego()
    {
        OknoDodania obiekt = new OknoDodania(this.frame);
        Kontakt osoba = obiekt.zwrocWynik();
        if (osoba != null) this.listaKontaktow.dodajKontakt(osoba);
    }

    protected void edytujZnajomego()
    {
        String nick = this.listaKontaktow.zwrocZaznaczonyKontakt().getNazwa();
        OknoEdycji obiekt = new OknoEdycji(this.frame, nick);
        String ostatecznyNick = obiekt.zwrocNick();
        if (ostatecznyNick != null && ostatecznyNick.length() > 0
                && ostatecznyNick != nick)
        {
            this.listaKontaktow.zmienZaznaczonyNick(ostatecznyNick);
            this.oknoRozmowy.zmienNickKontaktu(this.listaKontaktow
                    .zwrocZaznaczonyKontakt(), ostatecznyNick);
        }
    }

    protected void zakoncz()
    {
        WatekSieciowy.wylacz();
        WatekSieciowy.zakonczWatek();
        this.oknoRozmowy.dispose();
        this.zapiszDane();
        frame.setVisible(false);
        frame.dispose();
        System.exit(0);
    }

}
