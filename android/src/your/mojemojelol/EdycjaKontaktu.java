package your.mojemojelol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EdycjaKontaktu extends Activity implements OnClickListener
{
	EditText etNick, etID;
	Button bDodaj;
	private String nick = "";
	private int id = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edycjakontaktu);
		dodatkoweDane();
		inicjalizacja();
	}

	private void dodatkoweDane()
	{
		nick = getIntent().getExtras().getString("nick");
		id = getIntent().getExtras().getInt("id");
	}

	private void inicjalizacja()
	{
		etNick = (EditText) findViewById(R.id.etNick);
		etID = (EditText) findViewById(R.id.etID);
		bDodaj = (Button) findViewById(R.id.bDodaj);
		bDodaj.setOnClickListener(this);

		etNick.setText(nick);
		etID.setText(String.valueOf(id));
		etID.setEnabled(false);
		bDodaj.setText("Zedytuj kontakt");
	}

	public void onClick(View v)
	{
		String kluczBundle = getString(R.string.dodanieKontaktu);
		String kluczKontakt = getString(R.string.nowyKontakt);
		Intent person = new Intent();
		Bundle bundle = new Bundle();
		bundle.putSerializable(kluczKontakt, zwrocKontakt());
		person.putExtra(kluczBundle, bundle);
		setResult(RESULT_OK, person);
		finish();
	}

	private Kontakt zwrocKontakt()
	{
		String nazwa = this.etNick.getText().toString();
		int id = Integer.parseInt(this.etID.getText().toString());
		return new Kontakt(nazwa, id);
	}

}
