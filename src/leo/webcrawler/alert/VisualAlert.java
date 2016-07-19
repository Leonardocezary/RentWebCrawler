package leo.webcrawler.alert;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class VisualAlert extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel panel; 
	
	private void setLink(JLabel website, final String url, String text) {

		website.setText("<html> Website : <a href=\"\">" + text + "</a></html>");
		website.setCursor(new Cursor(Cursor.HAND_CURSOR));
		website.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				try {
					Desktop.getDesktop().browse(new URI(url));
				}
				catch (Exception ex) {
					// It looks like there's a problem
				}
			}
		});
	}
}
