import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WatekSieciowy implements Runnable
{

    int port;
    boolean polaczony;
    Socket gniazdo;
    String adres;
    Kontakt daneDoLogowania;
    ListaKontaktow listaKontaktow;
    OknoRozmowy oknoRozmowy;
    PrintWriter wyjscie;
    BufferedReader wejscie;

    static ArrayList<Wiadomosc> listaWiadomosci = new ArrayList<Wiadomosc>();

    ArrayList<Wiadomosc> odebraneWiadomosci = new ArrayList<Wiadomosc>();
    Map<Integer, String> nadawcy = new HashMap<Integer, String>();

    public WatekSieciowy(String adres, int port, ListaKontaktow listaKontaktow,
            OknoRozmowy oknoRozmowy)
    {
        super();
        this.adres = adres;
        this.port = port;
        this.listaKontaktow = listaKontaktow;
        this.oknoRozmowy = oknoRozmowy;
        polaczony = false;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                Thread.sleep(50);
                if (!polaczony)
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

    private void polacz()
    {
        try
        {
            gniazdo = new Socket(adres, port);
            wyjscie = new PrintWriter(gniazdo.getOutputStream(), true);
            wejscie = new BufferedReader(new InputStreamReader(gniazdo
                    .getInputStream()));
            polaczony = true;
        }
        catch (UnknownHostException e)
        {
            polaczony = false;
            System.out.println("Nieznany host");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            polaczony = false;
            System.out.println("Blad odczytu");
            e.printStackTrace();
        }
    }

    private void odczytajDane()
    {
        String tresc;
        String pomoc;
        int dlugosc;
        int kod;
        try
        {
            do
            {
                dlugosc = wczytajDlugosc();
                kod = wczytajKodWejscia();
                wczytajTrescWiadomosci(dlugosc);
            } while (wejscie.ready());
            przekazWiadomosci();
            wyczyscWiadomosci();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public int zalogujSie(int daneUzytkownika, String haslo)
    {
        char wiadomosc[] = new char[50];
        char id = (char)daneUzytkownika;
        int dlugosc = 2*(haslo.toCharArray().length + 1 + 1);
        wyjscie.write((char)dlugosc);
        wyjscie.write(((char)2));
        wyjscie.write(id);
        wyjscie.write(haslo.toCharArray());
        //if(daneUzytkownika == 1 && haslo == "gryberg") return 1;
        //if(daneUzytkownika == 5 && haslo == "holor") return 1;
        return 0;
    }

    public void zarejestrujSie(String haslo)
    {
        char wiadomosc[] = new char[50];
        int dlugosc = 2*(haslo.toCharArray().length + 1);
        wyjscie.write((char)dlugosc);
        wyjscie.write(((char)1));
        wyjscie.write(haslo.toCharArray());
    }

    private Integer wczytajDlugosc() throws IOException
    {
        char dane[] = new char[5];
        wejscie.read(dane, 0, 4);
        dane[4] = '\0';
        return Integer.valueOf(new String(dane));
    }

    private int wczytajKodWejscia() throws IOException
    {
        char dane[] = new char[1];
        wejscie.read(dane, 0, 1);
        return Integer.valueOf(new String(dane));
    }

    private void wczytajTrescWiadomosci(int dlugosc) throws IOException
    {
        switch (dlugosc)
        {
        case 1: // Rejestracja
            break;
        case 2: // Logowanie
            logowanieZwrotne();
            break;
        case 3: // Status
            statusZwrotny(dlugosc);
            break;
        case 5:
            odebranieWiadomosci(dlugosc);
            break;
        }
    }

    private int logowanieZwrotne() throws IOException
    {
        char dane[] = new char[2];
        wejscie.read(dane, 0, 2);
        return Integer.valueOf(new String(dane));
    }

    private void statusZwrotny(int dlugosc) throws IOException
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
        if (daneDoLogowania != null)
            wyslijDaneDoLogowania();
    }

    private void wyslijDaneDoLogowania()
    {
        wyjscie.println('2' + String.valueOf(daneDoLogowania.getId()) + '3');
    }

    public void wyslijDaneDoRejestracji()
    {
        wyjscie.println('1' + String.valueOf(daneDoLogowania.getId()) + '3');
    }
}
