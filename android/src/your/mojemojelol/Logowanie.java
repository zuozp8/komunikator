package your.mojemojelol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Logowanie extends Activity implements OnClickListener
{

    Button bLogowanie;
    Button bRejestracja;
    EditText etID;
    EditText etPassword;
    Kontakt ja;
    SharedPreferences ustawienia;
    WatekSieciowy wSiec;
    Thread watekWS;
    private int port;
    private String adres;
    boolean czyProbaPolaczenia = false;
    boolean flagaZamkniecia = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logowanie);
        inicjalizacja();
        przygotowanieBazyDanych();
    	//probaPolaczenia();
    	//czyProbaPolaczenia = true;
    }

    private void inicjalizacja()
    {
        bLogowanie = (Button) findViewById(R.id.bLogowanie);
        bRejestracja = (Button) findViewById(R.id.bRejestracja);
        this.etID = (EditText) findViewById(R.id.etID);
        this.etPassword = (EditText) findViewById(R.id.etPassword);

        bLogowanie.setOnClickListener(this);
        bRejestracja.setOnClickListener(this);
    }

    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.bLogowanie:
        	probaPolaczenia();
            logowanie();
            break;
        case R.id.bRejestracja:
        	probaPolaczenia();
            rejestracja();
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuInflater blowUp = getMenuInflater();
        blowUp.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.opcje:
            Intent opcje = new Intent(Logowanie.this, Opcje.class);
            startActivity(opcje);
            break;
        case R.id.wyjscie:
            zamknij();
            break;
        }
        return false;
    }

    private void probaPolaczenia()
	{
		if(czyProbaPolaczenia == false)
		{
	        pobierzUstawienia();
	        utworzWatekSieciowy();
	        czyProbaPolaczenia =true;
		}
	}

	private void pobierzUstawienia()
    {
        ustawienia = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        zdobadzDanePolaczenia();
    }

    private void zdobadzDanePolaczenia()
    {
        if(ustawienia != null)
        {
            port = 4790;
            adres = ustawienia.getString("ipaddress", "192.168.1.115");
        }
        else
        {
            port = 4790;
            adres = "192.168.1.115";
        }
    }

    private void utworzWatekSieciowy()
    {
        wSiec = new WatekSieciowy(adres, port);
        wSiec.ustawBazeRozmow(new BazaRozmow(this));
        watekWS = new Thread(wSiec);
        watekWS.start();
    }

    private void rejestracja()
    {
        int id = 0;
        String haslo = etPassword.getText().toString();
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
            e.printStackTrace();
        }
        if (id > 0) poprawnaRejestracja(id);
        else bladRejestracji();
    }

    private void poprawnaRejestracja(int id)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String wiadomosc = getString(R.string.poprawnaRejestracja) + id;
        builder.setMessage(wiadomosc);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void bladRejestracji()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.bladRejestracji);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void bladLogowania()
	{
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.bladLogowania);
        AlertDialog alert = builder.create();
        alert.show();
	}

    private void logowanie()
    {
        int wynikLogowania = -1;
        String idString = etID.getText().toString();
        short id = (short) Short.valueOf(idString);
        String password = etPassword.getText().toString();
        try
        {
            WatekSieciowy.zalogujSie(id, password);
            while (true)
            {
                Thread.sleep(100);
                wynikLogowania = WatekSieciowy.wynikLogowania();
                if (wynikLogowania != -1) break;
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        if (wynikLogowania == 1)
        {
            ja = new Kontakt("Ja", id);
            otworzListeKontaktow();
        }
        else
        {
        	bladLogowania();
        }
    }

	private void otworzListeKontaktow()
    {
        try
        {
            Intent listaKontaktow = new Intent(
                    "your.mojemojelol.LISTAKONTAKTOW");
            zalaczDodatkoweDane(listaKontaktow);
            this.flagaZamkniecia = true;
            startActivity(listaKontaktow);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void przygotowanieBazyDanych()
    {
        BazaRozmow baza = new BazaRozmow(this);
        baza.open();
        baza.usunRozmowy();
        baza.close();
    }

    private void zalaczDodatkoweDane(Intent listaKontaktow)
    {
        Bundle dane = new Bundle();
        dane.putSerializable("ja", ja);
        listaKontaktow.putExtra("dane", dane);
    }
    
    private void zamknij()
    {
        WatekSieciowy.wylacz();
        System.exit(1);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(flagaZamkniecia) finish();
    }

}
