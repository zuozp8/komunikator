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
    static ListaKontaktow listaKontaktow;
    static OknoRozmowy oknoRozmowy;
    static BufferedReader wejscie;
    ArrayList<Wiadomosc> odebraneWiadomosci = new ArrayList<Wiadomosc>();
    Map<Integer, String> nadawcy = new HashMap<Integer, String>();
    private static boolean flaga;
    static int wynikLogowania = -1;
    static int wynikRejestracji = -1;
    static int licznikOD = 0;

    static OutputStream outputStream;
    static ByteArrayOutputStream wyjscie;
    static ArrayList<Wiadomosc> listaWiadomosci = new ArrayList<Wiadomosc>();
    private static boolean flagaOdpytywaniaKontaktow = false;
    private static boolean flagaObieraniaRozmow = false;

    public WatekSieciowy(String adres, int port)
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
                    licznikOD++;
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

    public static void wylacz()
    {
        flaga = false;
        try
        {
            gniazdo.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        if (!gniazdo.isConnected() || gniazdo.isClosed()) polacz();
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
        if (!gniazdo.isConnected() || gniazdo.isClosed()) polacz();
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
        case WatekSieciowy.LOGOWANIE:
            logowanieZwrotne();
            break;
        case WatekSieciowy.STAN_ZNAJOMYCH:
            statusZwrotny(dlugosc);
            break;
        case WatekSieciowy.OBSLUGA_WIADOMOSCI:
            odebranieWiadomosci(dlugosc);
            break;
        }
    }

    private void rejestracjaZwrotne()
    {
        wynikRejestracji = wczytajLiczbe2B();
        try {
        	gniazdo.close();
        } catch (IOException e) {}
    }

    private void logowanieZwrotne()
    {
        wynikLogowania = wczytajLiczbe1B();
    }

    private void statusZwrotny(int dlugosc)
    {
        int idKontaktu;
        int dostepnosc;
        int licznik = (dlugosc-1) / 3;
        Map<Integer, Integer> mapa = new HashMap<Integer, Integer>();
        while (licznik > 0)
        {
            idKontaktu = wczytajLiczbe2B();
            dostepnosc = wczytajLiczbe1B();
            mapa.put(idKontaktu, dostepnosc);
            licznik--;
        }
        przekazStatusZwrotny(mapa);
    }

    private void przekazStatusZwrotny(Map<Integer, Integer> mapa)
    {
        this.listaKontaktow.ustawStanyKontaktow(mapa);
    }

    private void odebranieWiadomosci(int dlugosc)
    {
        String tresc;
        int idKontaktu = wczytajLiczbe2B();
        tresc = wczytajTrescWiadomosci(dlugosc-3);
        utworzWiadomosc(tresc, idKontaktu);

    }

    private void utworzWiadomosc(String tresc, int idKontaktu)
    {
        Wiadomosc wiadomosc = new Wiadomosc();
        wiadomosc.ustawTresc(tresc);
        wiadomosc.setNadawca(new Kontakt("",idKontaktu));
        wiadomosc.setData(tresc);
        odebraneWiadomosci.add(wiadomosc);
    }

    private void przypiszNazwyNadawcowDoWiadomosci()
    {
        for(Wiadomosc wiadomosc : odebraneWiadomosci)
        {
            int id = wiadomosc.getNadawca().getId();
            String nick = this.listaKontaktow.zwrocNick(id);
            wiadomosc.getNadawca().setNazwa(nick);
        }
    }

    private void przekazWiadomosci()
    {
        if(flagaObieraniaRozmow)
        {
            Collections.sort(odebraneWiadomosci);
            przypiszNazwyNadawcowDoWiadomosci();
            oknoRozmowy.odbierzWiadomosci(odebraneWiadomosci);
        }
        else if(flagaOdpytywaniaKontaktow)
        {
            return;
        }
    }

    private void wyczyscWiadomosci()
    {
        if(flagaObieraniaRozmow || flagaOdpytywaniaKontaktow)
        {
            odebraneWiadomosci.clear();
            nadawcy.clear();
        }
    }


    private void wyslijDane()
    {
        if (listaWiadomosci.size() > 0)
        {
            for (Wiadomosc wiadomosc : this.listaWiadomosci)
            {
                wpiszWiadomoscNaWyjscie(wiadomosc);
            }
            listaWiadomosci.clear();
        }
        /*if(flagaOdpytywaniaKontaktow && licznikOD>100)
        {
            wyslijZapytanieOStanKontaktow();
            licznikOD =0;
        }*/
        zakonczWpisywanie();
    }

    private void wyslijZapytanieOStanKontaktow()
    {
        short tablica[] = WatekSieciowy.listaKontaktow.zwrocTabliceID();
        short dlugosc = (short) (2*tablica.length + 1);
        wpiszLiczbe2B(dlugosc);
        wpiszLiczbe1B(WatekSieciowy.STAN_ZNAJOMYCH);
        for(int i=0;i<tablica.length;i++)
        {
            wpiszLiczbe2B(tablica[i]);
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

    private String wczytajTrescWiadomosci(int dlugosc)
    {
        char tablica[] = new char[dlugosc];
        try
        {
            int licznik = 0;
            while(licznik<dlugosc)
            {
                tablica[licznik] = (char)wejscie.read();
                //System.out.println(tablica[licznik] + " " + (int)tablica[licznik]);
                if ((int)tablica[licznik] > 255) dlugosc--;
                licznik++;
            }
            String tresc = new String(tablica);
            System.out.println(tresc.codePointAt(tresc.length()-1));
            if(!tresc.endsWith("\n")) tresc.concat("\n");
            return tresc;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
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
    
    public static void zgloszenieDoOdpytywania(ListaKontaktow lista)
    {
        WatekSieciowy.listaKontaktow = lista;
        flagaOdpytywaniaKontaktow = true;
    }
    public static void zgloszenieDoOdbieraniaWiadomosci(OknoRozmowy okno)
    {
        WatekSieciowy.oknoRozmowy = okno;
        flagaObieraniaRozmow  = true;
    }
}
