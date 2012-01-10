import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class WatekSieciowy implements Runnable
{

    static final short REJESTRACJA = 1;
    static final short LOGOWANIE = 2;
    static final short STAN_ZNAJOMYCH = 3;
    static final short OBSLUGA_WIADOMOSCI = 5;

    static int port;
    boolean polaczony;
    static Socket gniazdo;
    static String adres;
    Kontakt daneDoLogowania;
    ListaKontaktow listaKontaktow;
    OknoRozmowy oknoRozmowy;
    static BufferedReader wejscie;
    ArrayList<Wiadomosc> odebraneWiadomosci = new ArrayList<Wiadomosc>();
    Map<Integer, String> nadawcy = new HashMap<Integer, String>();
    private boolean flaga;
    static int wynikLogowania = -1;
    static int wynikRejestracji = -1;

    static OutputStream outputStream;
    static ByteArrayOutputStream wyjscie;
    static ArrayList<Wiadomosc> listaWiadomosci = new ArrayList<Wiadomosc>();

    public WatekSieciowy(String adres, int port/*
                                                * , ListaKontaktow
                                                * listaKontaktow, OknoRozmowy
                                                * oknoRozmowy
                                                */)
    {
        super();
        this.adres = adres;
        this.port = port;
        polaczony = false;
        flaga = true;
        polacz();
    }

    @Override
    public void run()
    {
        while (flaga)
        {
            try
            {
                Thread.sleep(50);
                if (!gniazdo.isConnected())
                {
                    polacz();
                }
                else
                {
                    odczytajDane();
                    wyslijDane();
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

        }

    }

    public void wylacz()
    {
        flaga = false;
    }

    private static void polacz()
    {
        try
        {
            InetAddress inAddress = zwrocInetAddress(adres);
            gniazdo = new Socket(inAddress, port);
            outputStream = gniazdo.getOutputStream();
            wyjscie = new ByteArrayOutputStream();
            wejscie = new BufferedReader(new InputStreamReader(gniazdo
                    .getInputStream()));
        }
        catch (UnknownHostException e)
        {
            System.out.println("Nieznany host");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            System.out.println("Blad odczytu");
            e.printStackTrace();
        }
    }

    private static InetAddress zwrocInetAddress(String adres)
    {
        try
        {
            int licznik = 0;
            byte tablica[] = new byte[4];
            StringTokenizer st = new StringTokenizer(adres, ".");
            while (st.hasMoreTokens())
            {
                tablica[licznik] = (byte) (int) Integer.valueOf(st.nextToken());
                licznik++;
            }
            return InetAddress.getByAddress(tablica);
        }
        catch (UnknownHostException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private void odczytajDane()
    {
        String wiadomosc;
        int dlugosc;
        int kod;
        try
        {
            while (wejscie.ready())
            {
                przetworzWiadomosc();
            }
            if (odebraneWiadomosci.size() > 0)
            {
                przekazWiadomosci();
                wyczyscWiadomosci();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static int zalogujSie(short daneUzytkownika, String haslo)
    {
        char id = (char) daneUzytkownika;
        int dlugosc = 2 + haslo.getBytes().length + 1;
        if (!gniazdo.isConnected()) polacz();
        wpiszLiczbe2B((short) dlugosc);
        wpiszLiczbe1B(WatekSieciowy.LOGOWANIE);
        wpiszLiczbe2B(daneUzytkownika);
        wpiszString(haslo);
        zakonczWpisywanie();
        return 0;
    }

    public static int wynikLogowania()
    {
        if (!gniazdo.isConnected()) return 0;
        else return wynikLogowania;
    }

    public static void zarejestrujSie(String haslo)
    {
        int dlugosc = haslo.getBytes().length + 1;
        if (!gniazdo.isConnected()) polacz();
        wpiszLiczbe2B((short) dlugosc);
        wpiszLiczbe1B(WatekSieciowy.REJESTRACJA);
        wpiszString(haslo);
        zakonczWpisywanie();
    }

    public static int wynikRejestracji()
    {
        return wynikRejestracji;
    }

    private short zwrocDlugosc(byte[] tablica)
    {
        return (short) (tablica[1] * 256 + tablica[0]);
    }

    private void przetworzWiadomosc()
    {
        int dlugosc = wczytajLiczbe2B();
        int kod = wczytajLiczbe1B();
        przetworzTrescWiadomosci(kod, dlugosc);
    }

    private void przetworzTrescWiadomosci(int kod, int dlugosc)
    {
        switch (kod)
        {
        case WatekSieciowy.REJESTRACJA:
            rejestracjaZwrotne();
            break;
        case WatekSieciowy.LOGOWANIE: // Logowanie
            logowanieZwrotne();
            break;
        case WatekSieciowy.STAN_ZNAJOMYCH: // Status
            // statusZwrotny(dane, dlugosc);
            break;
        case WatekSieciowy.OBSLUGA_WIADOMOSCI:
            // odebranieWiadomosci(kod);
            break;
        }
    }

    private void rejestracjaZwrotne()
    {
        wynikRejestracji = wczytajLiczbe2B();
    }

    private void logowanieZwrotne()
    {
        wynikLogowania = wczytajLiczbe1B();
    }

    private void statusZwrotny(byte[] dane, int dlugosc) throws IOException
    {
        int idKontaktu;
        int dostepnosc;
        int licznik = dlugosc / 3;
        char id[] = new char[2];
        char stan[] = new char[1];
        Map<Integer, Integer> mapa = new HashMap<Integer, Integer>();
        while (licznik > 0)
        {
            wejscie.read(id, 0, 2);
            idKontaktu = Integer.valueOf(new String(id));
            wejscie.read(stan, 0, 1);
            id[1] = '\0';
            dostepnosc = Integer.valueOf(new String(stan));
            mapa.put(idKontaktu, dostepnosc);
            licznik--;
        }
        przekazStatusZwrotny(mapa);
    }

    private void przekazStatusZwrotny(Map<Integer, Integer> mapa)
    {
        this.listaKontaktow.ustawStanyKontaktow(mapa);
    }

    private void odebranieWiadomosci(int dlugosc) throws IOException
    {
        String tresc;
        int idKontaktu;
        char id[] = new char[2];
        wejscie.read(id, 0, 2);
        idKontaktu = Integer.valueOf(new String(id));
        tresc = wejscie.readLine();
        utworzWiadomosc(tresc, idKontaktu);

    }

    private void utworzWiadomosc(String tresc, int idKontaktu)
    {
        Wiadomosc wiadomosc = new Wiadomosc();
        wiadomosc.ustawTresc(tresc);

        if (nadawcy.get(idKontaktu) != null)
        {
            wiadomosc.setNadawca(new Kontakt(nadawcy.get(idKontaktu),
                    idKontaktu));
        }
        else
        {
            String nick = this.listaKontaktow.zwrocNick(idKontaktu);
            wiadomosc.setNadawca(new Kontakt(nadawcy.get(idKontaktu),
                    idKontaktu));
        }
        wiadomosc.setData(tresc);
        odebraneWiadomosci.add(wiadomosc);
    }

    private void przekazWiadomosci()
    {
        Collections.sort(odebraneWiadomosci);
        this.oknoRozmowy.odbierzWiadomosci(odebraneWiadomosci);
    }

    private void wyczyscWiadomosci()
    {
        odebraneWiadomosci.clear();
        nadawcy.clear();
    }

    private void wyslijDane()
    {
        if (listaWiadomosci.size() > 0)
        {
            for (Wiadomosc wiadomosc : this.listaWiadomosci)
            {
                wpiszWiadomoscNaWyjscie(wiadomosc);
            }
            zakonczWpisywanie();
            listaWiadomosci.clear();
        }
    }

    private void wpiszWiadomoscNaWyjscie(Wiadomosc wiadomosc)
    {
        byte tresc[] = wiadomosc.zwrocTresc().getBytes();
        byte czas[] = wiadomosc.zwrocCzas().getBytes();
        short id = (short) wiadomosc.getOdbiorca().getId();
        short dlugosc = (short) (tresc.length /*+ czas.length*/ + 3);
        wpiszLiczbe2B(dlugosc);
        wpiszLiczbe1B(WatekSieciowy.OBSLUGA_WIADOMOSCI);
        wpiszLiczbe2B(id);
        // wpiszString(wiadomosc.zwrocCzas());
        wpiszString(wiadomosc.zwrocTresc());
    }

    private int wczytajLiczbe2B()
    {
        try
        {
            int l1, l2;
            l1 = wejscie.read();
            l2 = wejscie.read();
            return l1 + l2 * 256;
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    private int wczytajLiczbe1B()
    {
        try
        {
            return wejscie.read();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    private static void wpiszLiczbe2B(short dlugosc)
    {
        byte tablica[] = new byte[2];
        tablica[1] = (byte) (dlugosc / 256);
        tablica[0] = (byte) (dlugosc % 256);
        try
        {
            wyjscie.write(tablica);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void wpiszLiczbe1B(int i)
    {
        byte tablica[] = new byte[1];
        tablica[0] = (byte) i;
        try
        {
            wyjscie.write(tablica);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void wpiszString(String tekst)
    {
        try
        {
            wyjscie.write(tekst.getBytes());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void zakonczWpisywanie()
    {
        try
        {
            wyjscie.writeTo(outputStream);
            wyjscie.reset();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void dodajWiadomosc(String tresc, String zwrocObecnyCzas,
            Kontakt rozmowca)
    {
        Wiadomosc wiadomosc = new Wiadomosc(null, tresc, zwrocObecnyCzas);
        wiadomosc.setOdbiorca(rozmowca);
        listaWiadomosci.add(wiadomosc);
    }
}
