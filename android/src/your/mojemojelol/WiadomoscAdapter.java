package your.mojemojelol;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class WiadomoscAdapter extends ArrayAdapter<Wiadomosc> implements Serializable
{
	private ArrayList<Wiadomosc> wiadomosci;

	public WiadomoscAdapter(Context context, int textViewResourceId,
			ArrayList<Wiadomosc> wiadomosc)
	{
		super(context, textViewResourceId, wiadomosc);
		this.wiadomosci = wiadomosc;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View v = convertView;
		if (v == null)
		{
			v = zwrocWidok();
		}
		ustawWidgety(position, v);
		return v;
	}

	private View zwrocWidok()
	{
		View v;
		LayoutInflater vi = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = vi.inflate(R.layout.rozmowawiadomosc, null);
		return v;
	}
	
	private View zwrocWidokDrugi()
    {
        View v;
        LayoutInflater vi = (LayoutInflater) this.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.rozmowawiadomosc2, null);
        return v;
    }

	private void ustawWidgety(int position, View v)
	{
		Wiadomosc wiadomosc = wiadomosci.get(position);
		if (wiadomosc != null || wiadomosci.size()==0)
		{
		    //if(wiadomosc.getNadawca().getId() != WatekSieciowy.zwrocMnie()) v = zwrocWidokDrugi();
			TextView nick = (TextView) v.findViewById(R.id.tvNick);
			TextView czas = (TextView) v.findViewById(R.id.tvCzas);
			TextView poleWiadomosci = (TextView) v.findViewById(R.id.tvTekstWiadomosci);
			
			nick.setText(wiadomosc.getNadawca().getNazwa());
			czas.setText(wiadomosc.zwrocCzas());
			poleWiadomosci.setText(wiadomosc.zwrocTresc());
		}
	}
}