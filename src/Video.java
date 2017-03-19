import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

public class Video implements Runnable {
	private Mat ProvidedFace = null;
	private String Vname;
	private int ImageCount = 0;
	private JPanel controlPanel;
	JButton videobutton;
	int skipflag = 0;

	CascadeClassifier faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");
	CascadeClassifier eyesdetector = new CascadeClassifier("haarcascade_eye_tree_eyeglasses.xml");

	List<BufferedImage> myList = new ArrayList<BufferedImage>();
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public Video(String Vname, Mat ProvidedFace, JPanel controlPanel, JFrame frame) {
		this.Vname = Vname;
		this.ProvidedFace = ProvidedFace;
		this.controlPanel = controlPanel;

	}

	@Override

	public void run() {

		VideoCapture capture1 = new VideoCapture(Vname);
		Mat Imageframe = new Mat();
		MatOfRect faceDetections = new MatOfRect();
		MatOfRect eyesDetections = new MatOfRect();
		double Totalframes = capture1.get(7);
		int processedframes = 0;
		if (capture1.isOpened()) {
			int count = 0;
			String[] extens = Vname.split("\\\\");
			String extensionRemoved = extens[extens.length - 1].split("\\.")[0];
			String Fullpath = "Faces/" + extensionRemoved;
			File dirF = new File(Fullpath);
			if (!dirF.isDirectory()) {
				dirF.mkdir();

			} else {

				dirF.mkdir();

			}
			BuildEigenFaces BuildEigenFaces = new BuildEigenFaces();

			// FaceRecognition FaceRecognition = new FaceRecognition(0,
			// Fullpath);
			FaceRecognition FaceRecognition = null;

			videobutton = new JButton(extensionRemoved);
			videobutton.setBackground(Color.yellow);
			videobutton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Details Details = new Details("Details");
					Details.Show();
				}
			});

			controlPanel.add(videobutton);
			controlPanel.revalidate();

			long startTime = System.currentTimeMillis();

			while (true) {
				capture1.read(Imageframe);

				if (!Imageframe.empty()) {
					processedframes++;
					skipflag++;
					if (skipflag % 2 == 0) {
						double Percentage = (processedframes / Totalframes) * 100;
						videobutton.setLabel(extensionRemoved + ".mp4    " + (int) Percentage + " %");

						Imgproc.cvtColor(Imageframe, Imageframe, Imgproc.COLOR_BGR2GRAY);
						Imgproc.equalizeHist(Imageframe, Imageframe);
						faceDetector.detectMultiScale(Imageframe, faceDetections, 1.2, 3,
								0 | Objdetect.CASCADE_SCALE_IMAGE,
								new Size((Imageframe.width() * 4) / 100, (Imageframe.height() * 4) / 100),
								new Size((Imageframe.width() * 70) / 100, (Imageframe.height() * 70) / 100));

						for (Rect rect : faceDetections.toArray()) {
							count++;
							Mat Face = new Mat(Imageframe, rect);

							eyesdetector.detectMultiScale(Face, eyesDetections);

							if (eyesDetections.toArray().length > 0) {

								String full = Fullpath + "/" + extensionRemoved + count + ".png";
								Imgproc.resize(Face, Face, new Size(125, 150));

								if (ImageCount <= 1) {
									ImageCount++;

									if (ImageCount == 1) {
										Highgui.imwrite(full, Face);
										BuildEigenFaces.build(0, Fullpath);

									}

									if (ImageCount == 2) {
										Highgui.imwrite(full, Face);
										BuildEigenFaces.build(1, Fullpath);
										FaceRecognition = new FaceRecognition(ImageCount - 1, Fullpath);

									}
									count++;
								} else {
									BuildEigenFaces.build(ImageCount, Fullpath);
									FaceRecognition = new FaceRecognition(ImageCount - 1, Fullpath);
									Double result = FaceRecognition.RecognizeMat("", Face);
									if (result > 0.07) {
										count++;
										Highgui.imwrite(full, Face);

										BuildEigenFaces.build(ImageCount, Fullpath);
										FaceRecognition = new FaceRecognition(ImageCount - 1, Fullpath);
									}
								}

							}

						}
					}

				} else {
					capture1.release();
					System.out.println("completed");
					System.out.println("Total time taken: " + (System.currentTimeMillis() - startTime) + " ms");

					break;
				}

			}
			String[] images = dirF.list();
			if (images.length > 0) {
				if (FaceRecognition != null) {
					Double result = FaceRecognition.RecognizeMat("", ProvidedFace);
					System.out.println(result);
					if (result <= 0.07) {
						videobutton.setLabel(extensionRemoved + ".mp4    Found");
						videobutton.setBackground(Color.green);
					} else {
						videobutton.setLabel(extensionRemoved + ".mp4   Not Found");
						videobutton.setBackground(Color.red);
					}
				} else {
					videobutton.setLabel(extensionRemoved + ".mp4   Not Found");
					videobutton.setBackground(Color.red);
				}
			} else {
				videobutton.setLabel(extensionRemoved + ".mp4   Not Found");
				videobutton.setBackground(Color.red);
			}
		}
		capture1.release();
	}

}
