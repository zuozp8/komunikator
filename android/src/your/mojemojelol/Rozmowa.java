package your.mojemojelol;

import java.util.ArrayList;

public class Rozmowa
{
	ArrayList<Wiadomosc> listaWiadomosci = new ArrayList<Wiadomosc>();
	private Kontakt kontaktJA;
	private Kontakt rozmowca;
	
	public Rozmowa(ArrayList<Wiadomosc> listaWiadomosci, Kontakt kontaktJA,
			Kontakt rozmowca)
	{
		super();
		this.listaWiadomosci = listaWiadomosci;
		this.kontaktJA = kontaktJA;
		this.rozmowca = rozmowca;
	}

	public ArrayList<Wiadomosc> getListaWiadomosci()
	{
		return listaWiadomosci;
	}

	public Kontakt getKontaktJA()
	{
		return kontaktJA;
	}

	public Kontakt getRozmowca()
	{
		return rozmowca;
	}
}
