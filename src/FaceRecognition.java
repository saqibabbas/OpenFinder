
// FaceRecognition.java
// Sajan Joseph, sajanjoseph@gmail.com
// http://code.google.com/p/javafaces/
// Modified by Andrew Davison, April 2011, ad@fivedots.coe.psu.ac.th

/* Use the eigen.cache containing eigenfaces, eigenvalues, and training
   image info to find the training image which most cloesly resembles 
   an input image.

   This code is a refactoring of the JavaFaces package by Sajan Joseph, available
   at http://code.google.com/p/javafaces/ The current version includes a GUI.
*/

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class FaceRecognition {
	FileUtils FileUtils = null;

	private static final float FACES_FRAC = 0.75f;
	// default fraction of eigenfaces used in a match

	private FaceBundle bundle = null;
	private double[][] weights = null; // training image weights
	private int numEFs = 0; // number of eigenfaces to be used in the
							// recognition

	private String FCache_Path = "";

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public FaceRecognition(String path) {
		FCache_Path = path;
		FileUtils = new FileUtils();

	}

	public FaceRecognition(int numEigenFaces, String path) {
		FCache_Path = path;

		FileUtils = new FileUtils();

		bundle = FileUtils.readCache(FCache_Path);
		if (bundle == null) {
			System.out.println("You must build an Eigenfaces cache before any matching");
			// System.exit(1);
		} else {

			int numFaces = bundle.getNumEigenFaces();
			// System.out.println("No of eigenFaces: " + numFaces);

			numEFs = numEigenFaces;
			if (numFaces > 0) {
				if ((numEFs < 1) || (numEFs > numFaces - 1)) {
					numEFs = Math.round((numFaces - 1) * FACES_FRAC); // set to
																		// less
																		// than
																		// max
					System.out.println("Number of matching eigenfaces must be in the range (1-" + (numFaces - 1) + ")"
							+ "; using " + numEFs);
				} else
					System.out.println("Number of eigenfaces: " + numEFs);

				weights = bundle.calcWeights(numEFs);
			}

		}

	} // end of FaceRecognition()

	public MatchResult match(String imFnm, Mat newImage)
	// match image in file against training images
	{

		Imgproc.resize(newImage, newImage, new Size(125, 150));

		double[] imagedouble = todoubleimage(newImage);
		return findMatch(imagedouble); // no checking of image size or

	} // end of match() using filename

	// ----------------- find matching results -----------------

	private MatchResult findMatch(double[] imagedouble) {
		double[] imArr = imagedouble;

		// convert array to normalized 1D matrix
		Matrix2D imMat = new Matrix2D(imArr, 1);
		imMat.normalise();

		imMat.subtract(new Matrix2D(bundle.getAvgImage(), 1)); // subtract mean
																// image
		Matrix2D imWeights = getImageWeights(numEFs, imMat);
		// map image into eigenspace, returning its coordinates (weights);
		// limit mapping to use only numEFs eigenfaces

		double[] dists = getDists(imWeights);
		ImageDistanceInfo distInfo = getMinDistInfo(dists);
		// find smallest Euclidian distance between image and training image

		ArrayList<String> imageFNms = bundle.getImageFnms();
		String matchingFNm = imageFNms.get(distInfo.getIndex());
		// get the training image filename that is closest

		double minDist = Math.sqrt(distInfo.getValue());

		return new MatchResult(matchingFNm, minDist);
	} // end of findMatch()

	private Matrix2D getImageWeights(int numEFs, Matrix2D imMat)
	/*
	 * map image onto numEFs eigenfaces returning its weights (i.e. its
	 * coordinates in eigenspace)
	 */
	{
		Matrix2D egFacesMat = new Matrix2D(bundle.getEigenFaces());
		Matrix2D egFacesMatPart = egFacesMat.getSubMatrix(numEFs);
		Matrix2D egFacesMatPartTr = egFacesMatPart.transpose();

		return imMat.multiply(egFacesMatPartTr);
	} // end of getImageWeights()

	private double[] getDists(Matrix2D imWeights)
	/*
	 * return an array of the sum of the squared Euclidian distance between the
	 * input image weights and all the training image weights
	 */
	{
		Matrix2D tempWt = new Matrix2D(weights); // training image weights
		double[] wts = imWeights.flatten();

		tempWt.subtractFromEachRow(wts);
		tempWt.multiplyElementWise(tempWt);
		double[][] sqrWDiffs = tempWt.toArray();
		double[] dists = new double[sqrWDiffs.length];

		for (int row = 0; row < sqrWDiffs.length; row++) {
			double sum = 0.0;
			for (int col = 0; col < sqrWDiffs[0].length; col++)
				sum += sqrWDiffs[row][col];
			dists[row] = sum;
		}
		return dists;
	} // end of getDists()

	private ImageDistanceInfo getMinDistInfo(double[] dists) {
		double minDist = Double.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < dists.length; i++)
			if (dists[i] < minDist) {
				minDist = dists[i];
				index = i;
			}
		return new ImageDistanceInfo(dists[index], index);
	} // end of getMinDistInfo()

	// ----------------------- test rig -------------------------
	public static double[] todoubleimage(Mat matrix) {

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

		int imWidth = image.getWidth();
		int imHeight = image.getHeight();

		double[] imageArray = new double[imWidth * imHeight];
		image = ImageUtils.toScaledGray(image, 1.0);
		image.getData().getPixels(0, 0, imWidth, imHeight, imageArray);

		return imageArray;
	}

	public Double RecognizeMat(String Path, Mat Image) {

		// total se 1 km

		long startTime = System.currentTimeMillis();

		MatchResult result = match(Path, Image);

		if (result == null)
			System.out.println("No match found");
		else {
			System.out.println();
			System.out.print("Matches image in " + result.getMatchFileName());
			System.out.printf("; distance = %.4f\n", result.getMatchDistance());
			System.out.println("Matched name: " + result.getName());
			return result.getMatchDistance();
		}
		System.out.println("Total time taken: " + (System.currentTimeMillis() - startTime) + " ms");
		return -1.0;
	} // end of main()

} // end of FaceRecognition class
