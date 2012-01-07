
public class Wiadomosc implements Comparable
{
    Kontakt odbiorca;
    private Kontakt nadawca;
    private String tresc;
    private String data;

    public Wiadomosc()
    {

    }

    public Wiadomosc(Kontakt nadawca, String tresc, String data)
    {
        this.nadawca = nadawca;
        this.ustawTresc(tresc);
        this.data = data;
    }

    public void ustawTresc(String tresc)
    {
        this.tresc = tresc;
    }

    public String zwrocTresc()
    {
        return tresc;
    }

    public Kontakt getNadawca()
    {
        return nadawca;
    }

    public void setNadawca(Kontakt nadawca)
    {
        this.nadawca = nadawca;
    }

    public String zwrocCzas()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public Kontakt getOdbiorca()
    {
        return odbiorca;
    }

    public void setOdbiorca(Kontakt odbiorca)
    {
        this.odbiorca = odbiorca;
    }

    @Override
    public int compareTo(Object wiadomosc)
    {
        return this.getNadawca().compareTo(((Wiadomosc)wiadomosc).getNadawca());
    }
}