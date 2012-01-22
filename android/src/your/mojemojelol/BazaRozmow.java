package your.mojemojelol;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class BazaRozmow
{

    static final String dbName = "komunikatorDB";
    static final String rozmowyTabela = "Rozmowy";
    static final String rtIDWiadomosc = "IDWiadomosc";
    static final String rtIDRozmowa = "IDRozmowa";
    static final String rtOdKogo = "KtoNadal";
    static final String rtDoKogo = "KtoOtrzymal";
    static final String rtCzas = "Czas";
    static final String rtTresc = "Tresc";

    static final String kontaktyTabela = "Kontakty";
    static final String ktID = "IDOsoby";
    static final String ktIDKontakt = "IDKontaktu";
    static final String ktNickKontakt = "ImieKontaktu";

    static final String viewEmps = "ViewEmps";

    DBHelper dbHelper;
    final Context context;
    SQLiteDatabase bazaDanych;

    static int odKogoIndex = 2;
    static int doKogoIndex = 3;
    static int czasIndex = 4;
    static int trescIndex = 5;
    static int idKontaktIndex = 1;
    static int nickKontaktIndex = 2;

    public BazaRozmow(Context context)
    {
        this.context = context;
    }

    public void dodajWiadomosc(int rozmowa, int odKogo, int doKogo,
            String czas, String tresc)
    {
        ContentValues wejscie = new ContentValues();
        wejscie.put(this.rtIDRozmowa, rozmowa);
        wejscie.put(this.rtOdKogo, odKogo);
        wejscie.put(this.rtDoKogo, doKogo);
        wejscie.put(this.rtCzas, czas);
        wejscie.put(this.rtTresc, tresc);
        bazaDanych.insert(rozmowyTabela, null, wejscie);
    }

    public ArrayList<Wiadomosc> odczytajWszystkieWiadomosciMiedzyPara(Kontakt odbiorca,
            Kontakt nadawca)
    {
        int idJA = odbiorca.getId();
        int idRozmowca = nadawca.getId();
        ArrayList<Wiadomosc> lista = new ArrayList<Wiadomosc>();
        Cursor cur = pobierzKursorWiadomosci(idJA, idRozmowca);
        if (cur.isAfterLast()) return null;

        do
        {
            Wiadomosc wiadomosc = przygotujWiadomosc(idJA, cur, odbiorca,nadawca);
            lista.add(wiadomosc);
        } while (cur.moveToNext());
        return lista;
    }

    private Cursor pobierzKursorWiadomosci(int odKogo, int doKogo)
    {
        Cursor cur = bazaDanych.rawQuery("SELECT * from " + rozmowyTabela
                + " where (" + rtDoKogo + " = " + doKogo + " and " + rtOdKogo
                + " = " + odKogo + ") or (" + rtOdKogo + " = " + doKogo
                + " and " + rtDoKogo + " = " + odKogo + ")", null);
        cur.moveToFirst();
        return cur;
    }

    private Wiadomosc przygotujWiadomosc(int odKogo, Cursor cur,
            Kontakt kontaktJA, Kontakt rozmowca)
    {
        Wiadomosc wiadomosc = new Wiadomosc();
        if (cur.getInt(odKogoIndex) != odKogo)
        {
            wiadomosc.setOdbiorca(kontaktJA);
            wiadomosc.setNadawca(rozmowca);
        }
        else
        {
            wiadomosc.setOdbiorca(rozmowca);
            wiadomosc.setNadawca(kontaktJA);
        }
        wiadomosc.ustawTresc(cur.getString(trescIndex));
        wiadomosc.setData(cur.getString(czasIndex));
        return wiadomosc;
    }

    public void usunRozmowy()
    {
        Cursor cur = bazaDanych.rawQuery("SELECT name FROM sqlite_master WHERE name=" + "'"+rozmowyTabela+"'", null);
        if(!cur.isAfterLast()) bazaDanych.execSQL("DELETE FROM " + rozmowyTabela);
    }
    
    public void dodajKontakt(Kontakt dodajacy,Kontakt nowy)
    {
        ContentValues wejscie = new ContentValues();
        wejscie.put(ktID, dodajacy.getId());
        wejscie.put(ktIDKontakt, nowy.getId());
        wejscie.put(ktNickKontakt, nowy.getNazwa());
        System.out.println(dodajacy.getId() + " " + nowy.getId() + " " + nowy.getNazwa());
        bazaDanych.insert(kontaktyTabela, null, wejscie);
    }
    
    public void edytujKontakt(Kontakt edytujacy,Kontakt zmieniany)
    {
        ContentValues wejscie = new ContentValues();
        wejscie.put(ktID, edytujacy.getId());
        wejscie.put(ktIDKontakt, zmieniany.getId());
        wejscie.put(ktNickKontakt, zmieniany.getNazwa());
        bazaDanych.update(kontaktyTabela, wejscie, ktIDKontakt + " = " +zmieniany.getId(), null);
    }
    
    public void usunKontakt(Kontakt usuwajacy,Kontakt usuwany)
    {
        bazaDanych.delete(kontaktyTabela, ktIDKontakt + " = " +usuwany.getId() + " and " + ktID + " = " + usuwajacy.getId(), null);
    }
    
    public ArrayList<Kontakt> odczytajListeKontaktow(Kontakt osoba)
    {
        ArrayList<Kontakt> lista = new ArrayList<Kontakt>();
        Cursor cur = pobierzKursorWiadomosci(osoba);
        if (cur.isAfterLast()) return null;
        do
        {
            Kontakt kontakt = przygotujKontakt(osoba, cur);
            lista.add(kontakt);
        } while (cur.moveToNext());
        return lista;
    }
    
    public String zwrocNickKontaktu(int id, int ktoPyta)
    {
        Cursor cur = bazaDanych.rawQuery("SELECT * FROM " + kontaktyTabela + " WHERE " + ktID +" = " + ktoPyta + " AND " + ktIDKontakt + " = " + id, null);
        cur.moveToFirst();
        if (cur.isAfterLast())
        {
            String nieznany = new String("Nieznany "+id);
            dodajKontakt(new Kontakt("",ktoPyta), new Kontakt(nieznany,id));
            return nieznany;
        }
        String nowy = cur.getString(nickKontaktIndex);
        return nowy;
    }

    private Cursor pobierzKursorWiadomosci(Kontakt osoba)
    {
        Cursor cur = bazaDanych.rawQuery("SELECT * from " + kontaktyTabela
                + " where " + ktID + " = " + osoba.getId(), null);
        cur.moveToFirst();
        return cur;
    }

    private Kontakt przygotujKontakt(Kontakt osoba, Cursor cur)
    {
        System.out.println(nickKontaktIndex + " " + idKontaktIndex);
        String nick = cur.getString(nickKontaktIndex);
        int id = cur.getInt(idKontaktIndex);
        Kontakt kontakt = new Kontakt(nick,id);
        return kontakt;
    }

    public BazaRozmow open()
    {
        dbHelper = new DBHelper(context);
        bazaDanych = dbHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        dbHelper.close();
    }
   
    private static class DBHelper extends SQLiteOpenHelper
    {

        public DBHelper(Context context)
        {
            super(context, dbName, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL("CREATE TABLE " + rozmowyTabela + " (" + rtIDWiadomosc
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + rtIDRozmowa
                    + " INTEGER, " + rtOdKogo + " INTEGER, " + rtDoKogo
                    + " INTEGER, " + rtCzas + " TEXT, " + rtTresc + " TEXT)");

            db.execSQL("CREATE TABLE " + kontaktyTabela + " (" + ktID
                    + " INTEGER, " + ktIDKontakt + " INTEGER, " + ktNickKontakt
                    + " TEXT)");
            
            /*
            Cursor cur = db.rawQuery("Select * From " + rozmowyTabela, null);
            odKogoIndex = cur.getColumnIndex(rtOdKogo);
            doKogoIndex = cur.getColumnIndex(rtDoKogo);
            czasIndex = cur.getColumnIndex(rtCzas);
            trescIndex = cur.getColumnIndex(rtTresc);
            
            cur = db.rawQuery("Select * From " + kontaktyTabela, null);
            idKontaktIndex = cur.getColumnIndex(ktIDKontakt);
            nickKontaktIndex = cur.getColumnIndex(ktNickKontakt);
        */
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS " + rozmowyTabela);
            db.execSQL("DROP TABLE IF EXISTS " + kontaktyTabela);
            onCreate(db);
        }

    }
}
