import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @author Jan
 * Zarz¹dza po³¹czeniem z serwerem, nadawaniem i odbieraniem wiadomoœci wszystkich typów.
 * Sk³ada siê z dwóch w¹tków. Jeden z nich wysy³a dane, a drugi wczytuje dane.
 */
class WatekSieciowy implements Runnable
{

	static final short REJESTRACJA = 1;
	static final short LOGOWANIE = 2;
	static final short STAN_ZNAJOMYCH = 3;
	static final short OBSLUGA_WIADOMOSCI = 5;

	static int port;
	static SocketChannel gniazdo;
	static String adres;
	static GlowneOkno glowneOkno;
	static ListaKontaktow listaKontaktow;
	static OknoRozmowy oknoRozmowy;
	static int wynikLogowania = -1;
	static int wynikRejestracji = -1;
	static int licznikOD = 0;
	static boolean flaga;
	static int flagaCzynnosci = 0;

	static OutputStream outputStream;
	static ByteBuffer wejscie;
	static ByteArrayOutputStream wyjscie;
	static ArrayList<Wiadomosc> listaWiadomosci = new ArrayList<Wiadomosc>();
	static boolean flagaOdpytywaniaKontaktow = false;
	static boolean flagaOdbieraniaRozmow = false;

	boolean polaczony;
	Kontakt daneDoLogowania;
	static ArrayList<Wiadomosc> odebraneWiadomosci = new ArrayList<Wiadomosc>();
	private Thread watekWyjsciaThread;
	static Selector selector = null, selectorW = null;
	static Semaphore sem = null;

	class WatekWyjscia implements Runnable
	{

		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					Thread.sleep(30);
					licznikOD++;
					WatekSieciowy.sem.acquire();
					WatekSieciowy.wyslijDane();
					WatekSieciowy.sem.release();
				} catch (InterruptedException e)
				{
				}
			}
		}

	}

	public WatekSieciowy(String adres, int port, GlowneOkno glowneOkno)
	{
		super();
		sem = new Semaphore(1);
		WatekSieciowy.glowneOkno = glowneOkno;
		WatekSieciowy.adres = adres;
		WatekSieciowy.port = port;
		polaczony = false;
		flaga = true;
		polacz();
		WatekWyjscia watekWyj = new WatekWyjscia();
		watekWyjsciaThread = new Thread(watekWyj);
		watekWyjsciaThread.start();
	}

	@Override
	public void run()
	{
		while (flaga)
		{
			if (!gniazdo.isOpen())
			{
				polacz();
			}
			try
			{
				selector.select();
				System.out.println(selector.selectedKeys().size());
			} catch (IOException e)
			{
				// e.printStackTrace();
				glowneOkno.zakoncz();
				System.exit(1);
			}
			odczytajDane();
		}

	}

	/**
	 * Zamyka gniazdo po³¹czenia.
	 */
	public static void wylacz()
	{
		try
		{
			gniazdo.close();
		} catch (IOException e)
		{
			// e.printStackTrace();
		}
	}

	public static void zakonczWatek()
	{
		flaga = false;
	}

	/**
	 * £¹czy siê z serwerem, tworzy bufor wejœciowy i inicjalizuje selektory, które
	 * u¿ywane bêd¹ do odczytywania i wysy³ania wiadomoœci.
	 */
	public static void polacz()
	{
		try
		{
			WatekSieciowy.sem.acquire();
			selector = Selector.open();
			selectorW = Selector.open();

			gniazdo = SocketChannel.open();
			gniazdo.configureBlocking(false);
			
			InetAddress inA = InetAddress.getByName(adres);
			InetSocketAddress inAddress = new InetSocketAddress(inA.getHostAddress(), port);
			
			gniazdo.connect(inAddress);
			while (!gniazdo.finishConnect())
				;
			wyjscie = new ByteArrayOutputStream();
			gniazdo.register(selector, SelectionKey.OP_READ);
			gniazdo.register(selectorW, SelectionKey.OP_WRITE);
			WatekSieciowy.sem.release();
		} catch (ConnectException e)
		{
			bladWatku();
			uspij();
			// e.printStackTrace();
		} catch (UnknownHostException e)
		{
			System.out.println("Nieznany host");
			bladWatku();
			// e.printStackTrace();
		} catch (IOException e)
		{
			System.out.println("Blad odczytu");
			// e.printStackTrace();
		} catch (InterruptedException e)
		{
			// e.printStackTrace();
			glowneOkno.zakoncz();
			System.exit(8);
		}
	}

	/**
	 * W przypadku nie powodzenia logowania/rejestracji albo albo zerwania po³¹czenia
	 * odpowiednio reaguje.
	 */
	private static void bladWatku()
	{
		switch (WatekSieciowy.flagaCzynnosci)
		{
		case WatekSieciowy.LOGOWANIE:
			wynikLogowania = 0;
			// glowneOkno.bladLogowania();
			break;
		case WatekSieciowy.REJESTRACJA:
			wynikRejestracji = 0;
			// glowneOkno.bladRejestracji();
			break;
		case 0:
			glowneOkno.brakPolaczenia();
			glowneOkno.zakoncz();
			System.exit(1);
			break;
		}
	}

	private static void uspij()
	{
		try
		{
			Thread.sleep(10000);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Zczytuje dane z bufore wejœciowego. Jeœli wyst¹pi³ b³¹d w odczycie to po³¹czenie zosta³o
	 * zerwane i rzucany jest wyj¹tek. W przypadku poprawnego odczytania pocz¹tku wiadomoœci
	 * wywo³uje funkcje zajmuj¹ce siê dalsz¹ czêœci¹ wiadomoœci oraz wys³aniem odpowiednich danych
	 * do listy kontaktów i okna rozmów.
	 */
	private void odczytajDane()
	{
		try
		{
			wejscie = ByteBuffer.allocate(2);
			int readedBytes = gniazdo.read(wejscie);
			if (readedBytes < 1)
			{
				gniazdo.close();
				bladWatku();
				throw new IOException("Broken connection");
			} else if (readedBytes == 1)
			{
				ByteBuffer wejscie2 = ByteBuffer.allocate(2);
				wejscie2.put(wejscie.get());
				wejscie = ByteBuffer.allocate(1);
				readedBytes = gniazdo.read(wejscie);
				if (readedBytes < 1)
				{
					gniazdo.close();
					bladWatku();
					throw new IOException("Broken connection");
				}
				wejscie2.put(wejscie.get());
				wejscie = wejscie2;
			}
			wejscie.flip();
			int length = wczytajLiczbe2B();
			int remainingLength = length;
			wejscie = ByteBuffer.allocate(remainingLength);
			while (remainingLength > 0)
			{
				selector.select();
				readedBytes = gniazdo.read(wejscie);
				if (readedBytes < 1)
				{
					gniazdo.close();
					bladWatku();
					throw new IOException("Broken connection");
				}
				remainingLength -= readedBytes;
			}
			wejscie.flip();
			int kod = wczytajLiczbe1B();
			przetworzTrescWiadomosci(kod, length);
			if (odebraneWiadomosci.size() > 0)
			{
				przekazWiadomosci();
				wyczyscWiadomosci();
			}
			wejscie.clear();
		} catch (IOException e)
		{
			//e.printStackTrace();
			try
			{
				gniazdo.close();
			} catch (IOException e1)
			{
				//e1.printStackTrace();
			}
		}
	}

	/**
	 * @param daneUzytkownika
	 * @param haslo
	 * @return
	 * Wysy³a dane do logowania
	 */
	public static int zalogujSie(short daneUzytkownika, String haslo)
	{
		int dlugosc = 2 + haslo.getBytes().length + 1;
		if (!gniazdo.isConnected())
			polacz();
		wpiszLiczbe2B((short) dlugosc);
		wpiszLiczbe1B(WatekSieciowy.LOGOWANIE);
		wpiszLiczbe2B(daneUzytkownika);
		wpiszString(haslo);
		zakonczWpisywanie();
		flagaCzynnosci = WatekSieciowy.LOGOWANIE;
		return 0;
	}

	public static int wynikLogowania()
	{
		if ((!gniazdo.isConnected()) && wynikLogowania < 0)
			return 0;
		else
			return wynikLogowania;
	}

	/**
	 * @param haslo
	 * Wysy³a dane do rejestracji.
	 */
	public static void zarejestrujSie(String haslo)
	{
		int dlugosc = haslo.getBytes().length + 1;
		if (!gniazdo.isConnected())
			polacz();
		wpiszLiczbe2B((short) dlugosc);
		wpiszLiczbe1B(WatekSieciowy.REJESTRACJA);
		wpiszString(haslo);
		flagaCzynnosci = WatekSieciowy.REJESTRACJA;
		// zakonczWpisywanie();
	}

	public static int wynikRejestracji()
	{
		return wynikRejestracji;
	}

	/**
	 * @param kod
	 * @param dlugosc
	 * Na podstawie kodu, zgodnie ze specyfikacj¹, rozpoznaje jaki to rodzaj wiadomoœci i
	 * podejmuje w³aœciwe temu dzia³ania.
	 */
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
		flagaCzynnosci = 0;
		try
		{
			gniazdo.close();
		} catch (IOException e)
		{
		}
	}

	private void logowanieZwrotne()
	{
		wynikLogowania = wczytajLiczbe1B();
		flagaCzynnosci = 0;
	}

	/**
	 * @param dlugosc
	 * Wczytuje dane dotycz¹ce dostêpnoœci znajomych.
	 */
	private void statusZwrotny(int dlugosc)
	{
		int idKontaktu;
		int dostepnosc;
		int licznik = (dlugosc - 1) / 3;
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
		WatekSieciowy.listaKontaktow.ustawStanyKontaktow(mapa);
	}

	/**
	 * @param dlugosc
	 * Odbiera wiadomosc.
	 */
	private void odebranieWiadomosci(int dlugosc)
	{
		String tresc;
		int idKontaktu = wczytajLiczbe2B();
		tresc = wczytajTrescWiadomosci(dlugosc - 3);
		utworzWiadomosc(tresc, idKontaktu);

	}

	/**
	 * @param tresc
	 * @param idKontaktu
	 * Tworzy wiadomosc na podstawie istniejacych danych.
	 */
	private void utworzWiadomosc(String tresc, int idKontaktu)
	{
		Wiadomosc wiadomosc = new Wiadomosc();
		wiadomosc.ustawTresc(tresc);
		wiadomosc.setNadawca(new Kontakt("", idKontaktu));
		wiadomosc.setData(tresc);
		odebraneWiadomosci.add(wiadomosc);
	}

	/**
	 * Na podstawie ID wyszukuje nicki znajomych.
	 */
	private static void przypiszNazwyNadawcowDoWiadomosci()
	{
		for (Wiadomosc wiadomosc : odebraneWiadomosci)
		{
			int id = wiadomosc.getNadawca().getId();
			String nick = WatekSieciowy.listaKontaktow.zwrocNick(id);
			wiadomosc.getNadawca().setNazwa(nick);
		}
	}

	/**
	 * Przesyla odebrane wiadomoœci do okna rozmów.
	 */
	private static void przekazWiadomosci()
	{
		if (flagaOdbieraniaRozmow)
		{
			Collections.sort(odebraneWiadomosci);
			przypiszNazwyNadawcowDoWiadomosci();
			oknoRozmowy.odbierzWiadomosci(odebraneWiadomosci);
		} else if (flagaOdpytywaniaKontaktow)
		{
			return;
		}
	}

	private static void wyczyscWiadomosci()
	{
		if (flagaOdbieraniaRozmow || flagaOdpytywaniaKontaktow)
		{
			odebraneWiadomosci.clear();
		}
	}

	/**
	 * Zarz¹dza wysy³aniem danych: odpytywanie stanu kontaktów i wiadomosci
	 * nadane przez uzytkownika.
	 */
	private static void wyslijDane()
	{
		if (listaWiadomosci.size() > 0)
		{
			for (Wiadomosc wiadomosc : WatekSieciowy.listaWiadomosci)
			{
				wpiszWiadomoscNaWyjscie(wiadomosc);
			}
			listaWiadomosci.clear();
		}
		if (flagaOdpytywaniaKontaktow && licznikOD > 1000)
		{
			wyslijZapytanieOStanKontaktow();
			licznikOD = 0;
		}

		zakonczWpisywanie();
	}

	/**
	 * Tworzy tresc zapytania o stan kontaktow.
	 */
	private static void wyslijZapytanieOStanKontaktow()
	{
		short tablica[] = WatekSieciowy.listaKontaktow.zwrocTabliceID();
        if(tablica == null || tablica.length < 1) return;
		short dlugosc = (short) (2 * tablica.length + 1);
		wpiszLiczbe2B(dlugosc);
		wpiszLiczbe1B(WatekSieciowy.STAN_ZNAJOMYCH);
		for (int i = 0; i < tablica.length; i++)
		{
			wpiszLiczbe2B(tablica[i]);
		}
	}

	/**
	 * @param wiadomosc
	 * Przygotowuje tresc pojedynczego komunikatu zawieraj¹cego wiadomosc u¿ytkownika
	 * klienta.
	 */
	private static void wpiszWiadomoscNaWyjscie(Wiadomosc wiadomosc)
	{
		byte tresc[] = wiadomosc.zwrocTresc().getBytes();
		short id = (short) wiadomosc.getOdbiorca().getId();
		short dlugosc = (short) (tresc.length + 3);
		wpiszLiczbe2B(dlugosc);
		wpiszLiczbe1B(WatekSieciowy.OBSLUGA_WIADOMOSCI);
		wpiszLiczbe2B(id);
		wpiszString(wiadomosc.zwrocTresc());
	}

	/**
	 * @param dlugosc
	 * @return
	 * Wczytuje tresc odebranej wiadomoœci od rozmówcy.
	 */
	private String wczytajTrescWiadomosci(int dlugosc)
	{
		char tablica[] = new char[dlugosc];
		byte tabliczka[] = new byte[dlugosc];
		int licznik = 0;
		while (licznik < dlugosc)
		{
			tabliczka[licznik] = wejscie.get();
			//System.out.println(Integer.toHexString(tablica[licznik]));
			licznik++;
		}
		String tresc = new String(tabliczka);
		//System.out.println(tresc.codePointAt(tresc.length() - 1));
		if (!tresc.endsWith("\n"))
			tresc.concat("\n");
		return tresc;
	}

	private int wczytajLiczbe2B()
	{
		short l1, l2;
		l1 = wejscie.get();
		l2 = wejscie.get();
		if (l1 < 0)
			l1 += 256;
		if (l2 < 0)
			l2 += 256;
		return l1 + l2 * 256;
	}

	private int wczytajLiczbe1B()
	{
		short l = wejscie.get();
		if (l < 0)
			l += 256;
		return l;
	}

	private static void wpiszLiczbe2B(short dlugosc)
	{
		byte tablica[] = new byte[2];
		tablica[1] = (byte) (dlugosc / 256);
		tablica[0] = (byte) (dlugosc % 256);
		try
		{
			wyjscie.write(tablica);
		} catch (IOException e)
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
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void wpiszString(String tekst)
	{
		try
		{
			wyjscie.write(tekst.getBytes());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Koñczy wpisywanie danych na bufor wyjœciowy, w przypadku ma³ego bufora
	 * wysy³a dane a¿ do pustego bufora.
	 */
	private static void zakonczWpisywanie()
	{
		try
		{
			ByteBuffer wyjBufor = ByteBuffer.wrap(wyjscie.toByteArray());
			wyjscie.reset();
			while (true)
			{
				selectorW.select();
				int writtenBytes = gniazdo.write(wyjBufor);
				if (writtenBytes <= 0)
				{
					// Skonczylismy pisaÄ‡ lub poÅ‚Ä…czenie siÄ™ zerwaÅ‚o
					break;// TODO:
				}
			}
		} catch (IOException e)
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

	/**
	 * @param lista
	 * Zg³oszenie listy kontaktow do odpytywania
	 */
	public static void zgloszenieDoOdpytywania(ListaKontaktow lista)
	{
		WatekSieciowy.listaKontaktow = lista;
		flagaOdpytywaniaKontaktow = true;
	}

	/**
	 * @param okno
	 * Zg³oszenie okna rozmów do odbierania wiadomoœci
	 */
	public static void zgloszenieDoOdbieraniaWiadomosci(OknoRozmowy okno)
	{
		WatekSieciowy.oknoRozmowy = okno;
		flagaOdbieraniaRozmow = true;
		if (odebraneWiadomosci.size() > 0)
		{
			przekazWiadomosci();
			wyczyscWiadomosci();
		}
	}
}
