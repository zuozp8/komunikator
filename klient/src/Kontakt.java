import java.io.Serializable;


public class Kontakt implements Comparable, Serializable {

	private String nazwa;
	private boolean online;
	private boolean konwersacja;
	private int id;

	public Kontakt(String nazwa, int id) {
		super();
		this.nazwa = nazwa;
		this.id = id;
		this.online = false;
	}
	
	public String getNazwa() {
		return nazwa;
	}

	public void setNazwa(String nazwa) {
		this.nazwa = nazwa;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setKonwersacja(boolean konwersacja)
	{
		this.konwersacja = konwersacja;
	}

	public boolean czyKonwersacja()
	{
		return konwersacja;
	}

    @Override
    public int compareTo(Object kontakt)
    {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;
        
        if(this == kontakt) return EQUAL;
        
        if(this.getId() < ((Kontakt) kontakt).getId()) return BEFORE;
        else if (this.getId() > ((Kontakt) kontakt).getId()) return AFTER;
        else return EQUAL;
    }
}
