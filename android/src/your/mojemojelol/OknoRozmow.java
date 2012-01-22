package your.mojemojelol;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class OknoRozmow extends Activity implements OnItemClickListener,
		View.OnClickListener
{
	ArrayList<Wiadomosc> listaWiadomosci = new ArrayList<Wiadomosc>();
	TextView tvNaglowek;
	private ListView lvRozmowa;
	private EditText etWpiszWiadomosc;
	private HorizontalScrollView hsvRozmowa;
	private Button bWyslij;
	private Kontakt kontaktJA;
	private Kontakt rozmowca;
	private WiadomoscAdapter wiadomoscAdapter;
	WatekAktualizacjiRozmowy waRozmowa;
	SharedPreferences prefs;
	BazaRozmow dane;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oknorozmow);

		dodatkoweDane();
		inicjalizacja();
		if (savedInstanceState != null)
		{
			this.onRestoreInstanceState(savedInstanceState);
		}
		przywrocDane();
		zgloszenieDoOdbieraniaWiadomosci();
	}

	private void zgloszenieDoOdbieraniaWiadomosci()
	{
		WatekSieciowy.zgloszenieDoOdbieraniaWiadomosci(this);
		waRozmowa = new WatekAktualizacjiRozmowy(handler);
		waRozmowa.start();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		outState.putSerializable("rozmowa", listaWiadomosci);
		outState.putSerializable("adapter", this.wiadomoscAdapter);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		listaWiadomosci = (ArrayList<Wiadomosc>) savedInstanceState
				.getSerializable("rozmowa");
		wiadomoscAdapter = (WiadomoscAdapter) savedInstanceState
				.getSerializable("adapter");
		lvRozmowa.setAdapter(wiadomoscAdapter);
		wiadomoscAdapter.notifyDataSetChanged();
	}
	
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		// TODO Auto-generated method stub

	}

	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.bWyslij:
			dodajWiadomosc(kontaktJA,rozmowca);
			break;
		}

	}
	
	@Override
	public void onBackPressed()
	{
		waRozmowa.setState(WatekAktualizacjiRozmowy.STATE_DONE);
		super.onBackPressed();
	}

	private void dodatkoweDane()
	{
		Bundle dodatkowe = getIntent().getExtras().getBundle("dane");
		if (dodatkowe != null)
		{
			kontaktJA = (Kontakt) dodatkowe.get("ja");
			rozmowca = (Kontakt) dodatkowe.get("rozmowca");
		}

	}

	private void inicjalizacja()
	{
	    tvNaglowek = (TextView) findViewById(R.id.tvNick);
	    tvNaglowek.setText(rozmowca.getNazwa());
	    
		etWpiszWiadomosc = (EditText) findViewById(R.id.etWpiszWiadomosc);
		bWyslij = (Button) findViewById(R.id.bWyslij);
		hsvRozmowa = (HorizontalScrollView) findViewById(R.id.hsvRozmowa);
		lvRozmowa = (ListView) findViewById(R.id.lvRozmowa);
		wiadomoscAdapter = new WiadomoscAdapter(this,
				android.R.layout.simple_list_item_1, listaWiadomosci);
		lvRozmowa.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		lvRozmowa.setAdapter(wiadomoscAdapter);

		lvRozmowa.setOnItemClickListener(this);
		bWyslij.setOnClickListener(this);

	//	this.przykladoweWiadomosci();
	}

	private void przywrocDane() {
		dane = new BazaRozmow(this);
		dane.open();
		ArrayList<Wiadomosc> wiadomosci = dane.odczytajWszystkieWiadomosciMiedzyPara(kontaktJA, rozmowca);
		Wiadomosc wiadomosc;
		if(wiadomosci!= null && wiadomosci.size()>0)
		{
			for(int i=0;i<wiadomosci.size();i++)
			{
				listaWiadomosci.add(wiadomosci.get(i));
			}
			wiadomoscAdapter.notifyDataSetChanged();
		}
		dane.close();
	}


/*	private void przykladoweWiadomosci()
	{
		dodajWiadomosc(kontaktJA, rozmowca);
		dodajWiadomosc(kontaktJA, rozmowca);

		ListView lvRozmowa2 = (ListView) findViewById(R.id.lvRozmowa2);
		lvRozmowa2.setAdapter(new WiadomoscAdapter(this,
				android.R.layout.simple_list_item_1, listaWiadomosci));

	}
*/


	private void dodajWiadomosc(Kontakt zrodlo, Kontakt cel)
	{
		String tresc = this.etWpiszWiadomosc.getText().toString();
		tresc.trim();
		if(tresc.equals("") || tresc.matches(" +")) return;
		String czas = DateFormat.getDateInstance().format(new Date());
		Wiadomosc wiadomosc = new Wiadomosc(zrodlo, tresc, czas);
		this.listaWiadomosci.add(wiadomosc);
		this.etWpiszWiadomosc.setText("");
		this.wiadomoscAdapter.notifyDataSetChanged();
		
		dane.open();
		dane.dodajWiadomosc(0, zrodlo.getId(),cel.getId() ,czas, tresc);
		dane.close();

        WatekSieciowy.dodajWiadomosc(tresc,zwrocObecnyCzas(),rozmowca);
        lvRozmowa.setSelection(wiadomoscAdapter.getCount()-1);
	}

    public void odbierzWiadomosci(ArrayList<Wiadomosc> odebraneWiadomosci)
    {
        Wiadomosc wiadomosc;
        for(int i=0; i < odebraneWiadomosci.size() ; i++)
        {
            wiadomosc = odebraneWiadomosci.get(i);
            if(wiadomosc.getNadawca().getId() == rozmowca.getId()) odbierzWiadomoscOdRozmowcy(wiadomosc);
        }
        //wiadomoscAdapter.notifyDataSetChanged();
    }

    private void odbierzWiadomoscOdRozmowcy(Wiadomosc wiadomosc)
    {
        String czas = DateFormat.getDateInstance().format(new Date());
        wiadomosc.setData(czas);
        this.listaWiadomosci.add(wiadomosc);
    }

    private String zwrocObecnyCzas()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }
    
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int iloscWiadomosci = msg.arg1;
            if(iloscWiadomosci < listaWiadomosci.size())
            {
        		wiadomoscAdapter.notifyDataSetChanged();
                lvRozmowa.setSelection(wiadomoscAdapter.getCount()-1);
            }
        }
    };

    
    private class WatekAktualizacjiRozmowy extends Thread {
        Handler mHandler;
        final static int STATE_DONE = 0;
        final static int STATE_RUNNING = 1;
        int mState;
        int iloscWiadomosci;
       
        WatekAktualizacjiRozmowy(Handler h) {
            mHandler = h;
            iloscWiadomosci = listaWiadomosci.size();
        }
       
        public void run() {
            mState = STATE_RUNNING;
            while (mState == STATE_RUNNING) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message msg = mHandler.obtainMessage();
                msg.arg1 = iloscWiadomosci;
                mHandler.sendMessage(msg);
                iloscWiadomosci = listaWiadomosci.size();
            }
        }
        
        public void setState(int state) {
            mState = state;
        }
    }


}