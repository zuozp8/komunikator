import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class GlowneOkno{

	private JFrame frame;
	private Kontakt daneUzytkownika;
	private ListaKontaktow listaKontaktow;
	private OknoRozmowy oknoRozmowy;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
					
					GlowneOkno window = new GlowneOkno();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GlowneOkno() {
		initialize();
		
		inicjalizujListeKontaktow();
		inicjalizujOknoRozmowy();
		
		//this.logowanie();
	}

	private void inicjalizujListeKontaktow()
	{
		listaKontaktow = new ListaKontaktow(this);
		JPanel panelListy = utworzPanelListy();
		dodajPrzewijanie(panelListy);
	}

	private JPanel utworzPanelListy()
	{
		JPanel panelListy = new JPanel(new BorderLayout());
		panelListy.add(listaKontaktow, BorderLayout.NORTH);
		panelListy.setBackground(new Color(255,255,255));
		return panelListy;
	}

	private void dodajPrzewijanie(JPanel panelListy)
	{
		JScrollPane pane = new JScrollPane(panelListy);
		frame.getContentPane().add(pane);
		pane.setSize(150, 150);
	}

	private void inicjalizujOknoRozmowy()
	{
		oknoRozmowy = new OknoRozmowy();
		oknoRozmowy.setVisible(false);
	}

	private void logowanie()
	{
		OknoLogowania oknoLogowania = new OknoLogowania(this.frame);
		Kontakt daneUzytkownika = oknoLogowania.zwrocWynik();
		//if(daneUzytkownika != null)
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 280, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnRotlfmao = new JMenu("ROTLFMAO");
		menuBar.add(mnRotlfmao);
		
		JMenuItem mntmZalogujSie = new JMenuItem("Zaloguj sie");
		mntmZalogujSie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logowanie();
			}
		});
		mnRotlfmao.add(mntmZalogujSie);
		
		JMenuItem mntmZmienProfil = new JMenuItem("Zmien profil");
		mnRotlfmao.add(mntmZmienProfil);
		
		JMenuItem mntmOpcje = new JMenuItem("Opcje");
		mntmOpcje.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				otworzOknoOpcji();
			}
		});
		mnRotlfmao.add(mntmOpcje);
		
		JMenuItem mntmZamknij = new JMenuItem("Zamknij");
		mntmZamknij.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				frame.dispose();
			}
		});
		mnRotlfmao.add(mntmZamknij);
		
		JMenu mnKontakty = new JMenu("Kontakty");
		menuBar.add(mnKontakty);
		
		JMenuItem mntmDodajZnajomego = new JMenuItem("Dodaj znajomego");
		mntmDodajZnajomego.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dodajZnajomego();
			}
		});
		mnKontakty.add(mntmDodajZnajomego);
		
		JMenuItem mntmUsuZnajomego = new JMenuItem("Usuń znajomego");
		mntmUsuZnajomego.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if ( listaKontaktow.getSelectedIndex() == -1 )
					bladUsuniecia();
				else if ( czyChceszUsunac() )
					listaKontaktow.usunKontakt();
			}
		});
		mnKontakty.add(mntmUsuZnajomego);
		
		JMenuItem mntmRozpocznijRozmow = new JMenuItem("Rozpocznij rozmowę");
		mnKontakty.add(mntmRozpocznijRozmow);
		
		JMenuItem mntmArchiwum = new JMenuItem("Archiwum");
		mnKontakty.add(mntmArchiwum);
	}


	protected void otworzOknoOpcji()
	{
		OknoOpcji oknoOpcji = new OknoOpcji();
	}

	protected void bladUsuniecia()
	{
		JOptionPane.showMessageDialog(frame, 
				"Najpierw zaznacz kontakt z listy!",
				"Błąd",
				JOptionPane.ERROR_MESSAGE);
	}

	protected boolean czyChceszUsunac()
	{
		Object[] options = {"Tak","Nie"};
		int n = JOptionPane.showOptionDialog(frame,
				"Czy na pewno chcesz usunąć zaznaczony kontakt?",
				"Czy aby na pewno?",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[1]);
		if(n == 0) return true;
		else return false;
	}

	public void dodajKarteRozmowy(Kontakt osoba)
	{
		oknoRozmowy.dodajRozmowe(osoba);
	}

	public void ustawAktualnaRozmowa(Kontakt osoba)
	{
		oknoRozmowy.ustawAktualnaRozmowa(osoba);
	}
	
	private void dodajZnajomego()
	{
		OknoDodania obiekt = new OknoDodania(this.frame);
		Kontakt osoba = obiekt.zwrocWynik();
		if( osoba != null) this.listaKontaktow.dodajKontakt(osoba);
	}
	
}
