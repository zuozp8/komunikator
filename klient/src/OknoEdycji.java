import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;



public class OknoEdycji extends JDialog
{
    JButton btnEdytuj;
    String ostatecznyNick;
    private JTextField poleNick;
    
    public OknoEdycji(Frame obiektNadrzeny, String nick) {
        super(obiektNadrzeny, "Dodawanie znajomego", true);
        
        JPanel panelKomponentow = inicjalizujKomponenty(nick);
        JPanel panelPrzycisku = inicjalizujPrzycisk();

        inicjalizujOkno(obiektNadrzeny, panelKomponentow, panelPrzycisku);
    }

    private void inicjalizujOkno(Frame parent, JPanel panel, JPanel bp)
    {
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private JPanel inicjalizujKomponenty(String nickPoczatkowy)
    {
        JPanel panel = new JPanel();
        JLabel nick = new JLabel("Nick");
        poleNick = new JTextField(20);
        poleNick.setText(nickPoczatkowy);
        
        
        panel.setLayout(new FlowLayout());
        panel.add(nick);
        panel.add(poleNick);
        
        return panel;
    }

    private JPanel inicjalizujPrzycisk()
    {
        JPanel bp = new JPanel();
        btnEdytuj = new JButton("Edytuj");
        btnEdytuj.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ostatecznyNick = poleNick.getText();
                dispose();
            }
        });
        bp.add(btnEdytuj);
        return bp;
    }

    public String zwrocNick()
    {
        this.setVisible(true);
        return this.ostatecznyNick;
    }
}
