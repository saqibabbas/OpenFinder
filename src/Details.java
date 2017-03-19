import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Details {
	private JPanel controlPanel;
	File dirF;
	String[] Imagesname;
	JLabel[] Images = null;

	private JPanel gui;
	FilenameFilter fileNameFilter;
	private JMenuBar menuBar;
	DefaultListModel model;

	public Details(String path) {
		gui = new JPanel(new GridLayout());

		JPanel imageViewContainer = new JPanel(new GridBagLayout());
		final JLabel imageView = new JLabel();
		imageViewContainer.add(imageView);

		model = new DefaultListModel();
		final JList imageList = new JList(model);
		imageList.setCellRenderer(new IconCellRenderer());
		ListSelectionListener listener = new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent lse) {
				Object o = imageList.getSelectedValue();
				if (o instanceof BufferedImage) {
					imageView.setIcon(new ImageIcon((BufferedImage) o));
				}
			}

		};
		imageList.addListSelectionListener(listener);
		try {
			loadImages(new File(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		gui.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(imageList,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
				new JScrollPane(imageViewContainer)));
	}

	public Details(String path, int a) {
		dirF = new File(path);
		Imagesname = dirF.list();
		Images = new JLabel[Imagesname.length];

		for (int j = 0; j < Imagesname.length; j++) {
			Images[j] = new JLabel();
			Images[j].setBounds(0, 0, 700, 600);
			ImageIcon icon = new ImageIcon(path + File.separator + Imagesname[j]);
			Image img = icon.getImage();
			Image newImg = img.getScaledInstance(Images[j].getWidth(), Images[j].getHeight(), Image.SCALE_SMOOTH);
			ImageIcon newImc = new ImageIcon(newImg);
			Images[j].setIcon(newImc);
		}

	}

	public void Show() {

		JFrame f = new JFrame("Details");
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.add(getGui());
		f.setJMenuBar(getMenuBar());
		f.setLocationByPlatform(true);
		f.pack();
		f.setSize(800, 600);
		f.setVisible(true);

	}

	public Container getGui() {
		return gui;
	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}

	public void loadImages(File directory) throws IOException {
		File[] imageFiles = directory.listFiles(fileNameFilter);
		BufferedImage[] images = new BufferedImage[imageFiles.length];
		model.removeAllElements();
		for (int ii = 0; ii < images.length; ii++) {
			model.addElement(ImageIO.read(imageFiles[ii]));
		}
	}

	public static void main(String[] args) {
		Details Details = new Details("Details");
		Details.Show();

	}
}

class IconCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;

	private int size;
	private BufferedImage icon;

	IconCellRenderer() {
		this(100);
	}

	IconCellRenderer(int size) {
		this.size = size;
		icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (c instanceof JLabel && value instanceof BufferedImage) {
			JLabel l = (JLabel) c;
			l.setText("");
			BufferedImage i = (BufferedImage) value;
			l.setIcon(new ImageIcon(icon));

			Graphics2D g = icon.createGraphics();
			g.setColor(new Color(0, 0, 0, 0));
			g.clearRect(0, 0, size, size);
			g.drawImage(i, 0, 0, size, size, this);

			g.dispose();
		}
		return c;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(size, size);
	}
}