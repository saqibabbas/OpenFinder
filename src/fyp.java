import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

public class fyp {

	private JFrame frame;
	private JPanel controlPanel;
	private JFileChooser filechooser;
	private JFileChooser Imagechooser;
	CascadeClassifier faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");
	CascadeClassifier eyesdetector = new CascadeClassifier("haarcascade_eye_tree_eyeglasses.xml");
	private JLabel imageLabel;

	SplashScreen screen;

	Mat Face = null;
	JButton Select_Videos = new JButton("Select Video(s)");
	JButton Select_Image = new JButton("Select Image");
	JButton Go = new JButton("Go");

	boolean ImageSelected = false;
	boolean VideoSelected = false;

	JLabel pic;
	Timer tm;
	int x = 0;
	String[] list = { "Slider/1.png", // 0
			"Slider/2.png", // 1
			"Slider/3.png", // 2
			"Slider/4.png", // 3
			"Slider/5.png", // 4
	};
	ImageIcon[] SliderImages = null;

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public fyp() {
		frame = new JFrame("Open Finder");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem Reset = new JMenuItem("Reset");
		Reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				frame.setVisible(false);
				fyp fyp = new fyp();
				fyp.initGUIs();
			}
		});
		menu.add(Reset);
		JMenuItem Credits = new JMenuItem("Credits");
		Credits.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"Developed by \n                      Syed Saqib Abbas Rizvi \n                      Shahruk Ahmed Khan \n                      Abdul Basit Rana"
								+ " \nIncharge \n                      Dr Muhammad Saeed   \n\n Department Of Computer Science,University Of Karachi. ",
						"Credits ", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menu.add(Credits);
		JMenuItem Close = new JMenuItem("Close");
		Close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.gc();

				System.exit(0);
			}
		});
		menu.add(Close);
		menubar.add(menu);
		frame.setJMenuBar(menubar);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		// frame.setLocation(dim.width / 2 - frame.getSize().width / 2,
		// dim.height / 2 - frame.getSize().height / 2);
		int screenwidth = dim.width / 2;
		int screenheight = dim.height / 2;

		int width = (screenwidth) - 200;
		int height = (screenheight) + 200;

		frame.setSize(width, height);
		// frame.pack();
		frame.setLocationRelativeTo(null);

		// frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		GridLayout layout = new GridLayout(1, 1);

		// FlowLayout layout = new FlowLayout();
		// layout.setHgap(10);
		// layout.setVgap(10);
		frame.setLayout(layout);
		GridLayout panellayout = new GridLayout(2, 1);

		controlPanel = new JPanel();
		// controlPanel.setLayout(panellayout);
		frame.add(controlPanel);

		Slider Slider = new Slider(controlPanel, frame, screenwidth, screenheight);
		Thread slide = new Thread(Slider);
		slide.start();

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		fyp fyp = new fyp();
		fyp.initGUI();

	}

	private void initGUI() {

		splashScreenInit();
		// do something here to simulate the program doing something that
		// is time consuming
		for (int i = 0; i <= 100; i++) {

			try {
				Thread.sleep(70);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			screen.setProgress(i + " %", i); // progress bar with a
												// message

		}
		splashScreenDestruct();

		imageLabel = new JLabel();
		imageLabel.setLocation(0, 0);
		controlPanel.add(imageLabel);

		Select_Videos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				filechooser = new JFileChooser();
				filechooser.setMultiSelectionEnabled(true);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("mp4", "MP4");
				filechooser.setFileFilter(filter);

				int returnVal = 99;

				returnVal = filechooser.showDialog(frame, "Choose videos");
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File[] file = filechooser.getSelectedFiles();
					if (file.length > 0) {
						VideoSelected = true;
						Select_Videos.setBackground(Color.green);

						if (ImageSelected) {
							Go.setBackground(Color.green);
						}

					}
				}
			}
		});
		controlPanel.add(Select_Videos);

		Select_Image.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Imagechooser = new JFileChooser();
				Imagechooser.setMultiSelectionEnabled(false);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("jpg", "png");
				Imagechooser.setFileFilter(filter);

				int returnVal = 99;

				returnVal = Imagechooser.showDialog(frame, "Choose Image");
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = Imagechooser.getSelectedFile();
					String path = file.getPath();
					// start
					Mat Imageframe = Highgui.imread(path);

					MatOfRect faceDetections = new MatOfRect();
					Imgproc.cvtColor(Imageframe, Imageframe, Imgproc.COLOR_BGR2GRAY);
					Imgproc.equalizeHist(Imageframe, Imageframe);
					faceDetector.detectMultiScale(Imageframe, faceDetections, 1.2, 3, 0 | Objdetect.CASCADE_SCALE_IMAGE,
							new Size((Imageframe.width() * 4) / 100, (Imageframe.height() * 4) / 100),
							new Size((Imageframe.width() * 70) / 100, (Imageframe.height() * 70) / 100));

					if (faceDetections.toArray().length > 0) {
						Rect rect = faceDetections.toArray()[0];
						Face = new Mat(Imageframe, rect);
						Imgproc.resize(Face, Face, new Size(125, 150));

						ImageIcon imageIcon = new ImageIcon();
						Image tempImage;

						tempImage = toBufferedImage(Face);

						imageIcon = new ImageIcon(tempImage, "Captured video");
						imageLabel.setIcon(imageIcon);
						Select_Image.setBackground(Color.green);

						if (VideoSelected) {
							ImageSelected = true;
							Go.setBackground(Color.green);

						} else
							ImageSelected = true;
					}

					// end
				}
			}
		});

		controlPanel.add(Select_Image);
		Go.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (VideoSelected && ImageSelected) {
					File[] file = filechooser.getSelectedFiles();
					for (File filename : file) {
						String name = filename.getPath();
						Video Video = new Video(name, Face, controlPanel, frame);
						Thread trd = new Thread(Video);
						trd.start();
					}
				} else {
					JOptionPane.showMessageDialog(null, "Please select video(s) and image first.", "Alert ",
							JOptionPane.INFORMATION_MESSAGE);

				}

			}
		});

		Go.setBackground(Color.red);

		controlPanel.add(Go);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(frame, "Are you sure to close this window?", "Really Closing?",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

					System.gc();

					System.exit(0);
				}
			}
		});
		frame.setVisible(true);
	}

	private void initGUIs() {

		splashScreenInit();
		// do something here to simulate the program doing something that
		// is time consuming

		try {
			Thread.sleep(70);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		// message

		splashScreenDestruct();

		imageLabel = new JLabel();
		imageLabel.setLocation(0, 0);
		controlPanel.add(imageLabel);

		Select_Videos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				filechooser = new JFileChooser();
				filechooser.setMultiSelectionEnabled(true);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("mp4", "MP4");
				filechooser.setFileFilter(filter);

				int returnVal = 99;

				returnVal = filechooser.showDialog(frame, "Choose videos");
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File[] file = filechooser.getSelectedFiles();
					if (file.length > 0) {
						VideoSelected = true;
						Select_Videos.setBackground(Color.green);

						if (ImageSelected) {
							Go.setBackground(Color.green);
						}

					}
				}
			}
		});
		controlPanel.add(Select_Videos);

		Select_Image.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Imagechooser = new JFileChooser();
				Imagechooser.setMultiSelectionEnabled(false);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("jpg", "png");
				Imagechooser.setFileFilter(filter);

				int returnVal = 99;

				returnVal = Imagechooser.showDialog(frame, "Choose Image");
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = Imagechooser.getSelectedFile();
					String path = file.getPath();
					// start
					Mat Imageframe = Highgui.imread(path);

					MatOfRect faceDetections = new MatOfRect();
					Imgproc.cvtColor(Imageframe, Imageframe, Imgproc.COLOR_BGR2GRAY);
					Imgproc.equalizeHist(Imageframe, Imageframe);
					faceDetector.detectMultiScale(Imageframe, faceDetections, 1.2, 3, 0 | Objdetect.CASCADE_SCALE_IMAGE,
							new Size((Imageframe.width() * 4) / 100, (Imageframe.height() * 4) / 100),
							new Size((Imageframe.width() * 70) / 100, (Imageframe.height() * 70) / 100));

					if (faceDetections.toArray().length > 0) {
						Rect rect = faceDetections.toArray()[0];
						Face = new Mat(Imageframe, rect);
						Imgproc.resize(Face, Face, new Size(125, 150));

						ImageIcon imageIcon = new ImageIcon();
						Image tempImage;

						tempImage = toBufferedImage(Face);

						imageIcon = new ImageIcon(tempImage, "Captured video");
						imageLabel.setIcon(imageIcon);
						Select_Image.setBackground(Color.green);

						if (VideoSelected) {
							ImageSelected = true;
							Go.setBackground(Color.green);

						} else
							ImageSelected = true;
					}

					// end
				}
			}
		});

		controlPanel.add(Select_Image);
		Go.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (VideoSelected && ImageSelected) {
					File[] file = filechooser.getSelectedFiles();
					for (File filename : file) {
						String name = filename.getPath();
						Video Video = new Video(name, Face, controlPanel, frame);
						Thread trd = new Thread(Video);
						trd.start();
					}
				} else {
					JOptionPane.showMessageDialog(null, "Please select video(s) and image first.", "Alert ",
							JOptionPane.INFORMATION_MESSAGE);

				}

			}
		});

		Go.setBackground(Color.red);

		controlPanel.add(Go);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(frame, "Are you sure to close this window?", "Really Closing?",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

					System.gc();

					System.exit(0);
				}
			}
		});
		frame.setVisible(true);
	}

	public void SetImageSize(int i) {

		pic.setIcon(SliderImages[i]);
	}

	public void MakeImageIcons(int i) {
		ImageIcon icon = new ImageIcon(list[i]);
		Image img = icon.getImage();
		Image newImg = img.getScaledInstance(pic.getWidth(), pic.getHeight(), Image.SCALE_SMOOTH);
		ImageIcon newImc = new ImageIcon(newImg);
		SliderImages[i] = newImc;

	}

	private void splashScreenInit() {
		ImageIcon myImage = new ImageIcon("Splash/splash.png");
		screen = new SplashScreen(myImage);
		screen.setLocationRelativeTo(null);
		screen.setProgressMax(100);
		screen.setScreenVisible(true);
	}

	private void splashScreenDestruct() {
		screen.setScreenVisible(false);
	}

	public Image toBufferedImage(Mat matrix) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (matrix.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
		byte[] buffer = new byte[bufferSize];
		matrix.get(0, 0, buffer); // get all the pixels
		BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);

		return image;

	}

}
