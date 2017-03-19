import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Slider implements Runnable {
	JLabel pic;
	Timer tm;
	int x = 0;
	private JPanel controlPanel;
	private JFrame frame;

	String[] list = null;
	int width;
	int height;
	ImageIcon[] Icons = null;

	public Slider(JPanel controlPanel, JFrame frame, int width, int height) {

		this.frame = frame;
		this.controlPanel = controlPanel;
		this.width = width;
		this.height = height;
		File dirF = new File("Slider");
		list = dirF.list(new FilenameFilter() {
			public boolean accept(File f, String name) {
				return name.endsWith(".png");
			}
		});
		Icons = new ImageIcon[list.length];

	}

	@Override
	public void run() {

		pic = new JLabel();
		pic.setBounds((width / 2), height / 2, width, height - 100);

		for (int i = 0; i < list.length; i++) {

			MakeImageIcons(i);
		}

		// set a timer
		tm = new Timer(2000, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SetImageSize(x);
				x += 1;
				if (x >= list.length)
					x = 0;
			}
		});
		controlPanel.add(pic);
		tm.start();
	}

	public void SetImageSize(int i) {

		pic.setIcon(Icons[i]);
	}

	public void MakeImageIcons(int i) {
		ImageIcon icon = new ImageIcon("Slider" + "/" + list[i]);
		Image img = icon.getImage();
		Image newImg = img.getScaledInstance(pic.getWidth(), pic.getHeight(), Image.SCALE_SMOOTH);
		ImageIcon newImc = new ImageIcon(newImg);
		Icons[i] = newImc;

	}
}
