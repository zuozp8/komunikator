package your.mojemojelol;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserItemAdapter extends ArrayAdapter<Kontakt> implements Serializable
{
	private ArrayList<Kontakt> users;

	public UserItemAdapter(Context context, int textViewResourceId,
			ArrayList<Kontakt> users)
	{
		super(context, textViewResourceId, users);
		this.users = users;
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
		v = vi.inflate(R.layout.listakontaktowelement, null);
		return v;
	}

	private void ustawWidgety(int position, View v)
	{
		Kontakt kontakt = users.get(position);
		if (kontakt != null)
		{
			TextView username = (TextView) v.findViewById(R.id.tvNazwaKontaktu);
			ImageView ivOnline = (ImageView) v.findViewById(R.id.ivOnline);

			if (username != null)
			{
				username.setText(kontakt.getNazwa());
			}
			if (kontakt.isOnline())
			{
				ivOnline.setImageResource(R.drawable.dost);
			}
			else
			{
				ivOnline.setImageResource(R.drawable.niedost);
			}
		}
	}
}