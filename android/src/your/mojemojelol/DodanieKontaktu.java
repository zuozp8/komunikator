package your.mojemojelol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class DodanieKontaktu extends Activity implements OnClickListener
{
	EditText etNick,etID;
	Button bDodaj;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edycjakontaktu);
		inicjalizacja();
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

	private void inicjalizacja()
	{
		etNick = (EditText) findViewById(R.id.etNick);
		etID = (EditText) findViewById(R.id.etID);
		bDodaj = (Button) findViewById(R.id.bDodaj);
		bDodaj.setOnClickListener(this);
	}

	private Kontakt zwrocKontakt()
	{
		String nazwa = this.etNick.getText().toString();
		int id = Integer.parseInt(this.etID.getText().toString());
		return new Kontakt(nazwa,id);
	}
	
}
