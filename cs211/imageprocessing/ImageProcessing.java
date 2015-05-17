package imageprocessing;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

@SuppressWarnings("serial")
public class ImageProcessing extends PApplet {
	final float HUE_FLOOR = 100.f;
	final float HUE_CEIL = 140.f;
	final float BRIGHTNESS_THRESHOLD = 25.f;
	final float SATURATION_THRESHOLD = 80.f;
	final float SOBEL_THRESHOLD = 0.3f;
	
	private int height, width;

	PImage img;
	int[] accumulator;

	List<PVector> bestCandidatesVect = new ArrayList<PVector>();

	public void setup() {
		img = loadImage("board1.jpg");
		img.resize((img.width/2), img.height/2);
		height = img.height;
		width = img.width;

		size(2 * width + height, height);
	}

	public void draw() {
		background(color(0, 0, 0));
		noLoop();
		PImage edgeImg = img;
		edgeImg = colorThresholding(edgeImg, HUE_FLOOR, HUE_CEIL);
		edgeImg = saturationThresholding(edgeImg, SATURATION_THRESHOLD);
		edgeImg = gaussianBlur(edgeImg);
		edgeImg = thresholding(edgeImg, BRIGHTNESS_THRESHOLD);
		edgeImg = sobel(edgeImg, SOBEL_THRESHOLD);

		PImage accImg = hough(edgeImg);
		accImg.resize(height, height);

		image(img, 0, 0);
		int minVotes = 100;
		int nLines = 4;
		detectBoard(edgeImg, nLines, minVotes);
		image(accImg, width, 0);
		image(edgeImg, height + width, 0);
	}

	public PImage hough(PImage edgeImg) {
		float discretizationStepsPhi = 0.06f;
		float discretizationStepsR = 2.5f;
		// dimensions of the accumulator
		int phiDim = (int) (Math.PI / discretizationStepsPhi);
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

		// pre-compute the sin and cos values
		float[] tabSin = new float[phiDim];
		float[] tabCos = new float[phiDim];
		float ang = 0;
		float inverseR = 1.f / discretizationStepsR;
		for (int accPhi = 0; accPhi < phiDim; ang += discretizationStepsPhi, accPhi++) {
			// we can also pre-multiply by (1/discretizationStepsR) since we
			// need it in the Hough loop
			tabSin[accPhi] = (float) (Math.sin(ang) * inverseR);
			tabCos[accPhi] = (float) (Math.cos(ang) * inverseR);
		}

		// println("imgWidth:" + edgeImg.width + "\nimgheight:" + edgeImg.height
		// + "\nphiDim:" + phiDim + "\nrDim:" + rDim);
		// our accumulator (with a 1 pix margin around)
		accumulator = new int[(phiDim + 2) * (rDim + 2)];
		// Fill the accumulator: on edge points (ie, white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		for (int y = 0; y < edgeImg.height; y++) {
			for (int x = 0; x < edgeImg.width; x++) {
				// Are we on an edge?
				if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
					// ...determine here all the lines (r, phi) passing through
					// pixel (x,y), convert (r,phi) to coordinates in the
					// accumulator, and increment accordingly the accumulator.
					for (int coordPhi = 1; coordPhi < phiDim + 1; coordPhi++) {
						float phi = (coordPhi - 1) * discretizationStepsPhi;
						float r = x * cos(phi) + y * sin(phi);
						int coordR = Math.round((r / discretizationStepsR))
								+ (rDim - 1) / 2;
						accumulator[coordPhi * (rDim + 2) + 1 + coordR] += 1;
					}
				}
			}
		}

		PImage accImg = createImage(rDim + 2, phiDim + 2, ALPHA);
		for (int i = 0; i < accumulator.length; i++) {
			accImg.pixels[i] = color(min(255, accumulator[i]));
		}
		accImg.updatePixels();
		return accImg;

	}

	public void detectBoard(PImage edgeImg, int nLines, int minVotes) {
		ArrayList<Integer> bestCandidates = new ArrayList<Integer>();

		float discretizationStepsPhi = 0.06f;
		float discretizationStepsR = 2.5f;
		// dimensions of the accumulator
		int phiDim = (int) (Math.PI / discretizationStepsPhi);
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);

		// pre-compute the sin and cos values
		float[] tabSin = new float[phiDim];
		float[] tabCos = new float[phiDim];
		float ang = 0;
		float inverseR = 1.f / discretizationStepsR;
		for (int accPhi = 0; accPhi < phiDim; ang += discretizationStepsPhi, accPhi++) {
			// we can also pre-multiply by (1/discretizationStepsR) since we
			// need it in the Hough loop
			tabSin[accPhi] = (float) (Math.sin(ang) * inverseR);
			tabCos[accPhi] = (float) (Math.cos(ang) * inverseR);
		}

		// size of the region we search for a local maximum
		int neighbourhood = 10;
		// only search around lines with more that this amount of votes
		// (to be adapted to your image)
		for (int accR = 0; accR < rDim; accR++) {
			for (int accPhi = 0; accPhi < phiDim; accPhi++) {
				// compute current index in the accumulator
				int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
				if (accumulator[idx] > minVotes) {
					boolean bestCandidate = true;
					// iterate over the neighbourhood
					for (int dPhi = -neighbourhood / 2; dPhi < neighbourhood / 2 + 1; dPhi++) {
						// check we are not outside the image
						if (accPhi + dPhi < 0 || accPhi + dPhi >= phiDim)
							continue;
						for (int dR = -neighbourhood / 2; dR < neighbourhood / 2 + 1; dR++) {
							// check we are not outside the image
							if (accR + dR < 0 || accR + dR >= rDim)
								continue;
							int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2)
									+ accR + dR + 1;
							if (accumulator[idx] < accumulator[neighbourIdx]) {
								// the current idx is not a local maximum!
								bestCandidate = false;
								break;
							}
						}
						if (!bestCandidate)
							break;
					}
					if (bestCandidate) {
						// the current idx *is* a local maximum
						bestCandidates.add(idx);
					}
				}
			}
		}

		Collections.sort(bestCandidates, new HoughComparator(accumulator));

		for (int i = 0; i < min(nLines, bestCandidates.size()); i++) {
			int idx = bestCandidates.get(i);
			// first, compute back the (r, phi) polar coordinates:
			int accPhi = Math.round(idx / (rDim + 2)) - 1;
			int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
			float phi = accPhi * discretizationStepsPhi;

			// Cartesian equation of a line: y = ax + b
			// in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
			// => y = 0 : x = r / cos(phi)
			// => x = 0 : y = r / sin(phi)

			// compute the intersection of this line with the 4 borders
			// of
			// the image
			int x0 = 0;
			int y0 = Math.round(r / sin(phi));
			int x1 = Math.round(r / cos(phi));
			int y1 = 0;
			int x2 = edgeImg.width;
			int y2 = Math.round(-cos(phi) / sin(phi) * x2 + r / sin(phi));
			int y3 = edgeImg.width;
			int x3 = Math.round(-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));

			// Finally, plot the lines
			stroke(204, 102, 0);
			if (y0 > 0) {
				if (x1 > 0)
					line(x0, y0, x1, y1);
				else if (y2 > 0)
					line(x0, y0, x2, y2);
				else
					line(x0, y0, x3, y3);
			} else {
				if (x1 > 0) {
					if (y2 > 0)
						line(x1, y1, x2, y2);
					else
						line(x1, y1, x3, y3);
				} else
					line(x2, y2, x3, y3);
			}
		}

		for (int i = 0; i < min(nLines, bestCandidates.size()); i++) {
			int idx = bestCandidates.get(i);
			int accPhi = Math.round(idx / (rDim + 2)) - 1;
			int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
			float phi = accPhi * discretizationStepsPhi;
			bestCandidatesVect.add(new PVector(r, phi));
		}

		getIntersections(bestCandidatesVect);
	}

	public ArrayList<PVector> getIntersections(List<PVector> lines) {
		ArrayList<PVector> intersections = new ArrayList<PVector>();
		for (int i = 0; i < lines.size() - 1; i++) {
			PVector line1 = lines.get(i);
			for (int j = i + 1; j < lines.size(); j++) {
				PVector line2 = lines.get(j);
				// compute the intersection and add it to 'intersections'
				float d = cos(line2.y) * sin(line1.y) - cos(line1.y)
						* sin(line2.y);
				float x = (line2.x * sin(line1.y) - line1.x * sin(line2.y)) / d;
				float y = (-line2.x * cos(line1.y) + line1.x * cos(line2.y))
						/ d;

				// draw the intersection
				fill(255, 128, 0);
				ellipse(x, y, 10, 10);
			}
		}
		return intersections;
	}

	public PVector intersection(PVector line1, PVector line2) {
		float d = cos(line2.y) * sin(line1.y) - cos(line1.y) * sin(line2.y);
		float x = (line2.x * sin(line1.y) - line1.x * sin(line2.y)) / d;
		float y = (-line2.x * cos(line1.y) + line1.x * cos(line2.y)) / d;
		return new PVector(x, y);
	}

	public PImage thresholding(PImage img, float threshold) {
		PImage result = createImage(width, height, ALPHA);
		for (int i = 0; i < img.width * img.height; i++) {
			if (brightness(img.pixels[i]) > threshold) {
				result.pixels[i] = color(255);
			}
		}
		return result;
	}

	public PImage saturationThresholding(PImage img, float threshold) {
		PImage result = createImage(width, height, ALPHA);
		for (int i = 0; i < img.width * img.height; i++) {
			if (saturation(img.pixels[i]) > threshold) {
				result.pixels[i] = img.pixels[i];
			} else {
				result.pixels[i] = color(0);
			}
		}
		return result;

	}

	public PImage colorThresholding(PImage img, float hueFloor, float hueCeil) {
		PImage result = createImage(width, height, RGB);
		for (int i = 0; i < img.width * img.height; i++) {
			float hue = hue(img.pixels[i]);
			if (hue < hueFloor || hue > hueCeil) {
				result.pixels[i] = color(0);
			} else {
				result.pixels[i] = img.pixels[i];
			}
		}
		return result;
	}

	public PImage gaussianBlur(PImage img) {
		PImage result = createImage(img.width, img.height, ALPHA);
		float[][] gaussianKernel = { { 9, 12, 9 }, { 12, 15, 12 }, { 9, 12, 9 } };
		float[] convol = convolute(img, gaussianKernel, 99.f);
		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = color(convol[i]);
		}
		return result;
	}

	public PImage sobel(PImage img, float threshold) {
		float[][] hKernel = { { 0, 1, 0 }, { 0, 0, 0 }, { 0, -1, 0 } };
		float[][] vKernel = { { 0, 0, 0 }, { 1, 0, -1 }, { 0, 0, 0 } };
		PImage result = createImage(img.width, img.height, ALPHA);
		// clear the image
		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = color(0);
		}
		float[] sum_h = convolute(img, hKernel, 1.f);
		float[] sum_v = convolute(img, vKernel, 1.f);
		float sum_hv;
		float max = 0;
		float[] buffer = new float[img.width * img.height];

		for (int i = 0; i < img.width * img.height; i++) {
			sum_hv = sqrt(pow(sum_h[i], 2) + pow(sum_v[i], 2));
			max = max(max, sum_hv);
			buffer[i] = sum_hv;
		}

		for (int y = 2; y < img.height - 2; y++) { // Skip top and bottom edges
			for (int x = 2; x < img.width - 2; x++) { // Skip left and right
				if (buffer[y * img.width + x] > (int) (max * threshold)) {
					result.pixels[y * img.width + x] = color(255);
				} else {
					result.pixels[y * img.width + x] = color(0);
				}
			}
		}
		return result;
	}

	// HELPERS METHODS
	public float[] convolute(PImage img, float[][] kernel, float weight) {
		// create a greyscale image (type: ALPHA) for output
		float[] result = new float[img.width * img.height];
		int kernelSize = 3;
		for (int px = 0; px < img.width * img.height; px++) {
			float sum = 0;
			for (int i = -kernelSize / 2; i <= kernelSize / 2; i++) {
				for (int j = -kernelSize / 2; j <= kernelSize / 2; j++) {
					int kernelCord = px + i * img.width + j;
					if (kernelCord < img.width * img.height && kernelCord >= 0) {
						sum += brightness(img.pixels[kernelCord])
								* kernel[i + 1][j + 1];
					}
				}
			}
			result[px] = sum / weight;
		}
		return result;
	}

}