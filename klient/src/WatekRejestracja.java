
public class WatekRejestracja implements Runnable
{

    private final String haslo;

    WatekRejestracja(String haslo)
    {
        this.haslo = haslo;
    }

    public void run()
    {
        int id = 0;
        WatekSieciowy.zarejestrujSie(haslo);
        while (true)
        {
            try
            {
                Thread.sleep(1000);
                id = WatekSieciowy.wynikRejestracji();
                System.out.println(id);
                if (id != -1) break;
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}