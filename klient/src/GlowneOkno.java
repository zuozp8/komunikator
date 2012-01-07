import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
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

public class GlowneOkno
{

    private JFrame frame;
    private Kontakt kontaktJA;
    private ListaKontaktow listaKontaktow;
    private OknoRozmowy oknoRozmowy;
    WatekSieciowy wSiec;
    private OknoOpcji oknoOpcji;

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
            if (window.frame == null)
                return;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        /*
         * } });
         */
    }

    /**
     * Create the application.
     */
    public GlowneOkno()
    {
        initialize();

        //utworzWatekSieciowy();
        this.logowanie();
        
        inicjalizujListeKontaktow();
        inicjalizujOknoRozmowy();
        utworzOknoOpcji();
        this.oknoOpcji.setVisible(false);
    }

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
            e.printStackTrace();
        }
        return new DefaultListModel();
    }
    
    public void zapiszDane()
    {
        try
        {
            File plik = new File(String.valueOf(kontaktJA.getId()));
            if (!plik.exists())
                plik.createNewFile();
            OutputStream file = new FileOutputStream(plik/*
                                                          * String.valueOf(kontaktJA
                                                          * .getId())
                                                          */);
            file.write((new String()).getBytes());
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(this.kontaktJA);
            output.writeObject(this.listaKontaktow.zwrocKontakty());
            output.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
        oknoRozmowy = new OknoRozmowy();
        oknoRozmowy.setVisible(false);
    }

    private void utworzWatekSieciowy()
    {
        int port = pobierzPort();
        String adres = pobierzAdres();
        wSiec = new WatekSieciowy(adres, port, this.listaKontaktow,
                this.oknoRozmowy);
        Thread watek = new Thread(wSiec);
        watek.start();
    }

    private String pobierzAdres()
    {
        return oknoOpcji.zwrocAdresIP();
    }

    private int pobierzPort()
    {
        return oknoOpcji.zwrocNumerPortu();
    }

    private void logowanie()
    {
        int wynik = 0;
        while (wynik != 1)
        {
            OknoLogowania oknoLogowania = new OknoLogowania(this.frame);
            int daneUzytkownika = oknoLogowania.zwrocWynik();
            String haslo = new String(oknoLogowania.zwrocHaslo());
            if (oknoLogowania.czyRejestracja())
            {
                
            }
            if (daneUzytkownika > 0)
            {
                kontaktJA = new Kontakt("Abel",daneUzytkownika);
                wynik = 1;
                //wSiec.zalogujSie(daneUzytkownika, haslo);
                //wSiec.czyUdaloSieLogowanie();
            }
            else if (daneUzytkownika == -2)
            {
                this.zakoncz();
            }
        }
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        frame = new JFrame();
        frame.setBounds(100, 100, 280, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu mnRotlfmao = new JMenu("ROTLFMAO");
        menuBar.add(mnRotlfmao);

        JMenuItem mntmZalogujSie = new JMenuItem("Zaloguj sie");
        mntmZalogujSie.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                logowanie();
            }
        });
        mnRotlfmao.add(mntmZalogujSie);

        JMenuItem mntmZmienProfil = new JMenuItem("Zmien profil");
        mnRotlfmao.add(mntmZmienProfil);

        JMenuItem mntmOpcje = new JMenuItem("Opcje");
        mntmOpcje.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent arg0)
            {
                otworzOknoOpcji();
            }
        });
        mnRotlfmao.add(mntmOpcje);

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

        JMenuItem mntmUsuZnajomego = new JMenuItem("Usuń znajomego");
        mntmUsuZnajomego.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent arg0)
            {
                if (listaKontaktow.getSelectedIndex() == -1)
                    bladUsuniecia();
                else if (czyChceszUsunac())
                    listaKontaktow.usunKontakt();
            }
        });
        mnKontakty.add(mntmUsuZnajomego);

        JMenuItem mntmRozpocznijRozmow = new JMenuItem("Rozpocznij rozmowę");
        mnKontakty.add(mntmRozpocznijRozmow);

        JMenuItem mntmArchiwum = new JMenuItem("Archiwum");
        mnKontakty.add(mntmArchiwum);
    }

    protected void utworzOknoOpcji()
    {
        oknoOpcji = new OknoOpcji();
        oknoOpcji.setVisible(false);
    }

    protected void otworzOknoOpcji()
    {
        oknoOpcji.setVisible(true);
    }

    protected void bladUsuniecia()
    {
        JOptionPane.showMessageDialog(frame,
                "Najpierw zaznacz kontakt z listy!", "Błąd",
                JOptionPane.ERROR_MESSAGE);
    }

    protected boolean czyChceszUsunac()
    {
        Object[] options = { "Tak", "Nie" };
        int n = JOptionPane.showOptionDialog(frame,
                "Czy na pewno chcesz usunąć zaznaczony kontakt?",
                "Czy aby na pewno?", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (n == 0)
            return true;
        else
            return false;
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
        if (osoba != null)
            this.listaKontaktow.dodajKontakt(osoba);
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
        this.oknoRozmowy.dispose();
        this.zapiszDane();
        frame.setVisible(false);
        frame.dispose();
        System.exit(0);
    }

}
