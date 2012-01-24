package your.mojemojelol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

public class ListaKontaktow extends Activity implements View.OnClickListener,
        OnItemClickListener, OnClickListener, OnItemLongClickListener
{

    static final int DODAWANIE = 0;
    static final int EDYCJA = 1;

    ArrayList<Kontakt> lista = new ArrayList<Kontakt>();
	ArrayList<Wiadomosc> dodatkoweWiadomosci = null;
    int ostatniKlikniety;
    ListView lvKontakty;
    Kontakt kontaktJA;
    ImageButton ibDodaj;
    ImageButton ibWyjdz;
    ImageButton ibOpcje;
    UserItemAdapter userItemAdapter;
    AlertDialog oknoPotwierdzenia;
    AlertDialog oknoWyboru;
    BazaRozmow dane;
	
    WatekWyswietlaniaKomunikatow wwKomunikatow;
	int flagaAktualizacjiStatusu = 0;
	int flagaNowychWiadomosci = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listakontaktow);
        dodatkoweDane();
        inicjalizacja();
        przywrocDane();
        if (savedInstanceState != null)
        {
            onRestoreInstanceState(savedInstanceState);
        }
        zgloszenieDoOdpytywaniaKontakow();
    }

	private void zgloszenieDoOdpytywaniaKontakow()
	{
		WatekSieciowy.zgloszenieDoOdpytywania(this);
		wlaczWyswietlanieKomunikatow();
	}

	private void wlaczWyswietlanieKomunikatow()
	{
		wwKomunikatow = new WatekWyswietlaniaKomunikatow(handler);
		wwKomunikatow.start();
	}
	
	private void wstrzymajWyswietlanieKomunikatow()
	{
		wwKomunikatow.setState(WatekWyswietlaniaKomunikatow.STATE_PAUSED);
	}
	
	private void wylaczWyswietlanieKomunikatow()
	{
		wwKomunikatow.setState(WatekWyswietlaniaKomunikatow.STATE_DONE);
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putSerializable("kontakty", lista);
        outState.putSerializable("adapter", userItemAdapter);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        lista = (ArrayList<Kontakt>) savedInstanceState
                .getSerializable("kontakty");
        userItemAdapter = (UserItemAdapter) savedInstanceState
                .getSerializable("adapter");
        lvKontakty.setAdapter(userItemAdapter);
        userItemAdapter.notifyDataSetChanged();
    }

    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.lvKontakty:
            // przejscieDoRozmowy(lvKontakty.getPositionForView(v));
            break;
        case R.id.ibDodaj:
        	wstrzymajWyswietlanieKomunikatow();
            obslugaDodaniaKontaktu();
            break;

        case R.id.ibOpcje:
        	wstrzymajWyswietlanieKomunikatow();
            Intent opcje = new Intent(ListaKontaktow.this, Opcje.class);
            startActivity(opcje);
            break;

        case R.id.ibWyjdz:
            zamknij();
            break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ListaKontaktow.DODAWANIE)
        {
            Kontakt nowy = odebranieDanychKontaktu(data);
            dodajKontakt(nowy);
        }
        else if (resultCode == RESULT_OK
                && requestCode == ListaKontaktow.EDYCJA)
        {
            Kontakt nowy = odebranieDanychKontaktu(data);
            edytujKontakt(nowy);
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view,
            int position, long id)
    {
        this.rozpoczecieRozmowy(position);
    }

    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
            long arg3)
    {
        this.ostatniKlikniety = arg2;
        String nick = this.lista.get(ostatniKlikniety).getNazwa();
        this.oknoWyboru.setTitle(nick);
        this.oknoWyboru.show();
        return true;
    }

    public void onClick(DialogInterface dialog, int which)
    {
        if (dialog.hashCode() == this.oknoPotwierdzenia.hashCode())
            oknoPotwierdzeniaObsluga(which);
        else
            oknoWyboruObsluga(which);
        return;
    }

    private void dodatkoweDane()
    {
        Bundle dodatkowe = getIntent().getExtras().getBundle("dane");
        if (dodatkowe != null)
        {
            this.kontaktJA = (Kontakt) dodatkowe.getSerializable("ja");
        }
    }

    private void inicjalizacja()
    {
        stworzOknoPotwierdzenia();
        stworzOknoWyboru();
        ibDodaj = (ImageButton) findViewById(R.id.ibDodaj);
        ibWyjdz = (ImageButton) findViewById(R.id.ibWyjdz);
        ibOpcje = (ImageButton) findViewById(R.id.ibOpcje);
        lvKontakty = (ListView) findViewById(R.id.lvKontakty);
        userItemAdapter = new UserItemAdapter(this,
                android.R.layout.simple_list_item_1, lista);
        lvKontakty.setAdapter(userItemAdapter);

        lvKontakty.setOnItemClickListener(this);
        lvKontakty.setOnItemLongClickListener(this);
        ibDodaj.setOnClickListener(this);
        ibWyjdz.setOnClickListener(this);
        ibOpcje.setOnClickListener(this);
    }

    private void przywrocDane()
    {
        dane = new BazaRozmow(this);
        dane.open();
        ArrayList<Kontakt> kontakty = dane.odczytajListeKontaktow(kontaktJA);
        if (kontakty != null && kontakty.size() > 0)
        {
            for (int i = 0; i < kontakty.size(); i++)
            {
                lista.add(kontakty.get(i));
                System.out.println(kontakty.get(i).getNazwa() + " " + kontakty.get(i).getId());
            }
            userItemAdapter.notifyDataSetChanged();
        }
        dane.close();
    }

    private void stworzOknoPotwierdzenia()
    {
        String tekst = getString(R.string.usuniecieCzyNaPewno);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(tekst).setCancelable(true).setPositiveButton("Tak",
                this).setNegativeButton("Nie", this);
        this.oknoPotwierdzenia = builder.create();
    }

    private void stworzOknoWyboru()
    {
        String[] wartosci = getResources().getStringArray(
                R.array.pozycjeOknaWyboru);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wybierz akcję:");
        builder.setItems(wartosci, this);
        this.oknoWyboru = builder.create();
    }

    private void oknoPotwierdzeniaObsluga(int which)
    {
        switch (which)
        {
        case -1: // Potwierdzenie
            this.usunOstatniElementLongClicked();
            break;
        case -2: // Odrzucenie
            break;
        }
    }

    private void oknoWyboruObsluga(int which)
    {
        switch (which)
        {
        case 0: // Rozmowa
            this.rozpoczecieRozmowy(ostatniKlikniety);
            break;
        case 1: // Usunięcie
            this.oknoPotwierdzenia.show();
            break;
        case 2:
            this.obslugaEdycjiKontaktu();
            break;
        case 3:
            break;
        }
    }

    private void obslugaEdycjiKontaktu()
    {
        Intent intent = new Intent(ListaKontaktow.this, EdycjaKontaktu.class);
        this.zalaczDaneKontaktu(ostatniKlikniety, intent);
        startActivityForResult(intent, ListaKontaktow.EDYCJA);
    }

    private void obslugaUsuwaniaKontaktu()
    {
        int numer = this.lvKontakty.getSelectedItemPosition();
        if (numer != ListView.INVALID_POSITION)
            this.oknoPotwierdzenia.show();
        else
            bladAkcji(R.string.bladUsuniecia);

    }

    private void obslugaDodaniaKontaktu()
    {
        Intent intentForResult = new Intent(ListaKontaktow.this,
                DodanieKontaktu.class);
        startActivityForResult(intentForResult, ListaKontaktow.DODAWANIE);
    }

    private void dodajKontakt(Kontakt nowy)
    {
        if (!czyWystepujeWLiscie(nowy))
        {
            lista.add(nowy);
            userItemAdapter.notifyDataSetChanged();
            dodajKontaktDoBazyDanych(nowy);
        }
        else
            bladAkcji(R.string.bladDodania);
    }

    private void dodajKontaktDoBazyDanych(Kontakt nowy)
    {
        dane.open();
        dane.dodajKontakt(kontaktJA, nowy);
        dane.close();
    }

    private void edytujKontakt(Kontakt nowy)
    {
        lista.set(ostatniKlikniety, nowy);
        userItemAdapter.notifyDataSetChanged();
        edytujKontaktWBazieDanych(nowy);
    }

    private void edytujKontaktWBazieDanych(Kontakt nowy)
    {
        dane.open();
        dane.edytujKontakt(kontaktJA, nowy);
        dane.close();
    }

    private void usunOstatniElementLongClicked()
    {
        int numer = this.ostatniKlikniety;
        Kontakt usuwany = lista.get(numer);
        lista.remove(numer);
        this.userItemAdapter.notifyDataSetChanged();
        usunKontaktZBazy(usuwany);
    }

    private void usunKontaktZBazy(Kontakt usuwany)
    {
        dane.open();
        dane.usunKontakt(kontaktJA, usuwany);
        dane.close();
    }

    public short[] zwrocTabliceID()
    {
        if(lista.size() == 0) return null;
        short tablica[] = new short[lista.size()];
        for(int i=0; i< lista.size();i++)
        {
            tablica[i] =(short) lista.get(i).getId();
        }
        return tablica;
    }

    public void ustawStanyKontaktow(Map<Integer, Integer> mapa)
    {
        Set<Integer> zbiorKluczy = mapa.keySet();
        for (int id : zbiorKluczy)
        {
            ustawStan(id, mapa.get(id));
        }
        flagaAktualizacjiStatusu = 1;
    }

    private void ustawStan(int id, Integer status)
    {
        Kontakt kontakt;
        for (int i = 0; i < this.lista.size(); i++)
        {
            kontakt = (Kontakt) this.lista.get(i);
            if (kontakt.getId() == id)
            {
                if (status == 0)
                    kontakt.setOnline(false);
                else
                    kontakt.setOnline(true);
            }
        }
    }
    
    private void rozpoczecieRozmowy(int position)
    {
    	wylaczWyswietlanieKomunikatow();
        Kontakt osoba = this.lista.get(position);
        if (!osoba.czyKonwersacja())
        {
            Intent rozmowa = new Intent(this, OknoRozmow.class);
            zalaczenieDodatkowychDanychDoRozmowy(osoba, rozmowa);
            startActivity(rozmowa);
        }
    }

    private void zalaczDaneKontaktu(int numer, Intent intent)
    {
        Kontakt kontakt = this.lista.get(numer);
        intent.putExtra("nick", kontakt.getNazwa());
        intent.putExtra("id", kontakt.getId());
    }

    private void zalaczenieDodatkowychDanychDoRozmowy(Kontakt osoba,
            Intent intent)
    {
        Bundle dane = new Bundle();
        dane.putSerializable("ja", kontaktJA);
        dane.putSerializable("rozmowca", osoba);
        intent.putExtra("dane", dane);
    }

    private Kontakt odebranieDanychKontaktu(Intent data)
    {
        String kluczBundle = getString(R.string.dodanieKontaktu);
        Bundle bundle = data.getExtras().getBundle(kluczBundle);
        String kluczKontakt = getString(R.string.nowyKontakt);
        Kontakt nowy = (Kontakt) bundle.getSerializable(kluczKontakt);
        return nowy;
    }

    private boolean czyWystepujeWLiscie(Kontakt nowy)
    {
        Kontakt osoba;
        for (int x = 0; x < lista.size(); x++)
        {
            osoba = lista.get(x);
            if (osoba.getId() == nowy.getId())
                return true;
        }
        return false;
    }

    private void bladAkcji(int stringID)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(stringID);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void zamknij()
    {
    	wwKomunikatow.setState(WatekWyswietlaniaKomunikatow.STATE_DONE);
        WatekSieciowy.wylacz();
        System.exit(1);
    }

    public void zawiadomOWiadomosciach(ArrayList<Wiadomosc> odebraneWiadomosci)
    {
    	//flagaNowychWiadomosci = 1;
    	if(dodatkoweWiadomosci != null)
    	{
    		dodatkoweWiadomosci.addAll(odebraneWiadomosci);
    	}
    	else
    	{
    		dodatkoweWiadomosci = (ArrayList<Wiadomosc>) odebraneWiadomosci.clone();
    		int licznik = 1;
    		licznik++;
    	}
    }

	private void pokazWiadomosci()
	{
		if(dodatkoweWiadomosci == null) return;
		Set<Integer> zbiorID = new HashSet<Integer>();
        String tresc = "Masz nowe wiadomosci od: ";
        for(Wiadomosc wiadomosc : dodatkoweWiadomosci)
        {
        	Kontakt nadawca = wiadomosc.getNadawca();
        	if(!czyWystepujeWLiscie(nadawca)) dodajKontakt(nadawca);
            if(!zbiorID.contains(nadawca.getId()))
            {
            	zbiorID.add(nadawca.getId());
            	tresc+=nadawca.getNazwa() + "; ";
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(tresc);
        AlertDialog alert = builder.create();
        alert.show();
        dodatkoweWiadomosci = null;
	}
    
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            //int czySaWiadomosci = msg.arg1;
            int flagaAktualizacjiStatusu = msg.arg1;
            /*if(czySaWiadomosci > 0)
            {
            	userItemAdapter.notifyDataSetChanged();
        		pokazWiadomosci();
            	flagaNowychWiadomosci = 0;
            }*/
            if(flagaAktualizacjiStatusu > 0)
            {
            	userItemAdapter.notifyDataSetChanged();
            	//lvKontakty.refreshDrawableState();
        		pokazWiadomosci();
            	flagaAktualizacjiStatusu = 0;
            }
        }
    };

    
    private class WatekWyswietlaniaKomunikatow extends Thread {
        Handler mHandler;
        final static int STATE_DONE = 0;
        final static int STATE_RUNNING = 1;
        final static int STATE_PAUSED = 2;
        int mState;
       
        WatekWyswietlaniaKomunikatow(Handler h) {
            mHandler = h;
        }
       
        public void run() {
            mState = STATE_RUNNING;
            while (mState != STATE_DONE) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(mState !=  STATE_PAUSED)
                {
                    Message msg = mHandler.obtainMessage();
                    //msg.arg1 = flagaNowychWiadomosci;
                    msg.arg1 = flagaAktualizacjiStatusu;
                    mHandler.sendMessage(msg);
                }
            }
        }
        
        public void setState(int state) {
            mState = state;
        }
    }
}
