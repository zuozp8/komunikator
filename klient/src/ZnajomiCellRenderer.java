import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author Jan
 * Klasa pomocnica s³u¿¹ca do wyœwietlania nicku oraz ikony dostêpnoœci w liœcie kontaków.
 */
class ZnajomiCellRenderer extends JLabel implements ListCellRenderer {
      private static final Color HIGHLIGHT_COLOR = new Color(0, 0, 128);
      private ImageIcon dost = new ImageIcon("dost1.gif");
      private ImageIcon niedost = new ImageIcon("niedost.gif");
      
      public ZnajomiCellRenderer() {
        setOpaque(true);
        setIconTextGap(12);
      }

      public Component getListCellRendererComponent(JList list, Object value,
          int index, boolean isSelected, boolean cellHasFocus) {
        Kontakt obiekt = (Kontakt) value;
        setText(obiekt.getNazwa());
        
        if (obiekt.isOnline())
        {
            setIcon(dost);
        }
        else
        {
            setIcon(niedost);
        }
        
        if (isSelected) {
          setBackground(HIGHLIGHT_COLOR);
          setForeground(Color.white);
        } else {
          setBackground(Color.white);
          setForeground(Color.black);
        }
        return this;
      }
}