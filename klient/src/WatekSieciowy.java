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
    static short wynikLogowania = -1;
    static short wynikRejestracji = -1;

    static OutputStream outputStream;
    static ByteArrayOutputStream wyjscie;
    static ArrayList<Wiadomosc> listaWiadomosci = new ArrayList<Wiadomosc>();

    public WatekSieciowy(String adres, int port/*, ListaKontaktow listaKontaktow,
            OknoRozmowy oknoRozmowy*/)
    {
        super();
        this.adres = adres;
        this.port = port;
        polaczony = false;
        polacz();
    }

    @Override
    public void run()
    {
        while (true)
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
                break;
            }
            // /////////////////////////////////////////////////
            break;
            // /////////////////////////////////////////////

        }

    }

    private static void polacz()
    {
        try
        {
            InetAddress inAddress= zwrocInetAddress(adres);
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
            StringTokenizer st = new StringTokenizer(adres,".");
            while(st.hasMoreTokens())
            {
                tablica[licznik] = (byte)(int)Integer.valueOf(st.nextToken());
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
            do
            {
                wiadomosc = wejscie.readLine();
                przetworzWiadomosc(wiadomosc);
            } while (wejscie.ready());
            przekazWiadomosci();
            wyczyscWiadomosci();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static int zalogujSie(int daneUzytkownika, String haslo)
    {
        char wiadomosc[] = new char[50];
        char id = (char) daneUzytkownika;
        int dlugosc = 2 * (haslo.toCharArray().length + 1 + 1);
        if(!gniazdo.isConnected()) polacz();
        wpiszLiczbe2B((short) dlugosc);
        wpiszLiczbe1B(2);
        wpiszLiczbe2B((short) daneUzytkownika);
        wpiszString(haslo);
        zakonczWpisywanie();
        return 0;
    }

    public static int wynikLogowania()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public static void zarejestrujSie(String haslo)
    {
        char wiadomosc[] = new char[50];
        int dlugosc = haslo.getBytes().length + 1;
        if(!gniazdo.isConnected()) polacz();
        wpiszLiczbe2B((short) dlugosc);
        wpiszLiczbe1B(1);
        wpiszString(haslo);
        zakonczWpisywanie();
    }

    public static int wynikRejestracji()
    {
        return wynikRejestracji;
    }

    private short zwrocDlugosc(byte[] tablica)
    {
        return (short) (tablica[0] * 256 + tablica[1]);
    }

    private void przetworzWiadomosc(String wiadomosc)
    {
        byte tablica[] = wiadomosc.getBytes();
        short dlugosc = zwrocDlugosc(tablica);
        int kod = tablica[2];
        przetworzTrescWiadomosci(kod, tablica, dlugosc);
    }

    private void przetworzTrescWiadomosci(int kod, byte[] dane, int dlugosc)
    {
        switch (kod)
        {
        case 1: // Rejestracja
            rejestracjaZwrotne(dane);
            break;
        case 2: // Logowanie
            logowanieZwrotne(dane);
            break;
        case 3: // Status
            //statusZwrotny(dane, dlugosc);
            break;
        case 5:
            //odebranieWiadomosci(kod);
            break;
        }
    }

    private void rejestracjaZwrotne(byte[] dane)
    {
        wynikRejestracji = (short) ((short) dane[3] * 256 + dane[4]);
    }

    private void logowanieZwrotne(byte[] dane)
    {
        wynikLogowania = dane[3];
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
    //    if (daneDoLogowania != null)
      //      wyslijDaneDoLogowania();
    }
    
    private static void wpiszLiczbe2B(short dlugosc)
    {
        byte tablica[] = new byte[2];
        tablica[0] = (byte) (dlugosc / 256);
        tablica[1] = (byte) (dlugosc % 256);
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
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
