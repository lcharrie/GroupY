package tangibleGame;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class ComputeAngles {
	/*
	 * BLOCK D'INITIALIATION DES SEUILS
	 */
	private final float SATURATION_THRESHOLD = 90.f;
	private final float HUE_FLOOR = 100.f;
	private final float HUE_CEIL = 140.f;
	private final float BRIGHTNESS_THRESHOLD = 30.f;
	private final float SOBEL_THRESHOLD = 0.3f;
	/*
	 * ****************************
	 * ****************************
	 */

	/* Capture to Movie in declaring the video class */
	// Capture cam;
	PImage edgeImg;
	TwoDThreeD converter;

	TangibleGame parent;

	int[] accumulator;

	List<PVector> bestCandidatesVect = new ArrayList<PVector>();

	public ComputeAngles(TangibleGame p) {
		parent = p;

		converter = new TwoDThreeD(p.width, p.height);
	}

	public PVector getAngles(PImage img) {
		edgeImg = img;
		edgeImg = colorThresholding(edgeImg, HUE_FLOOR, HUE_CEIL);
		edgeImg = saturationThresholding(edgeImg, SATURATION_THRESHOLD);
		edgeImg = gaussianBlur(edgeImg);
		edgeImg = thresholding(edgeImg, BRIGHTNESS_THRESHOLD);
		edgeImg = sobel(edgeImg, SOBEL_THRESHOLD);

		hough(edgeImg);

		int minVotes = 100;
		int nLines = 4;
		return detectBoard(edgeImg, nLines, minVotes);
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
				if (parent.brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
					// ...determine here all the lines (r, phi) passing through
					// pixel (x,y), convert (r,phi) to coordinates in the
					// accumulator, and increment accordingly the accumulator.
					for (int coordPhi = 1; coordPhi < phiDim + 1; coordPhi++) {
						float phi = (coordPhi - 1) * discretizationStepsPhi;
						float r = (float) (x * Math.cos(phi) + y
								* Math.sin(phi));
						int coordR = Math.round((r / discretizationStepsR))
								+ (rDim - 1) / 2;
						accumulator[coordPhi * (rDim + 2) + 1 + coordR] += 1;
					}
				}
			}
		}

		PImage accImg = parent.createImage(rDim + 2, phiDim + 2, PApplet.ALPHA);
		for (int i = 0; i < accumulator.length; i++) {
			accImg.pixels[i] = parent.color(Math.min(255, accumulator[i]));
		}
		accImg.updatePixels();
		return accImg;

	}

	public PVector detectBoard(PImage edgeImg, int nLines, int minVotes) {
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

		bestCandidatesVect = new ArrayList<PVector>();

		for (int i = 0; i < Math.min(nLines, bestCandidates.size()); i++) {
			int idx = bestCandidates.get(i);
			int accPhi = Math.round(idx / (rDim + 2)) - 1;
			int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
			float phi = accPhi * discretizationStepsPhi;
			bestCandidatesVect.add(new PVector(r, phi));
		}

		QuadGraph quadGraph = new QuadGraph();
		quadGraph.build(bestCandidatesVect, parent.width, parent.height);
		List<int[]> cycles = quadGraph.findCycles();

		List<PVector> edges = new ArrayList<>();

		if (!cycles.isEmpty()) {
			int[] c = cycles.get(0);
			for (int i = 0; i < 4; i++) {
				edges.add(bestCandidatesVect.get(c[i]));
			}
			edges = getIntersections(edges);
			edges = sortEdges(edges);

			if (edges.size() == 4) {
				for (int i = 0; i < 4; i++) {
					edges.get(i).x -= edgeImg.width / 2.f;
					edges.get(i).y -= edgeImg.height / 2.f;
					edges.get(i).z = 1.f;
				}

				return converter.get3DRotations(edges);
			}
		}
		return null;

	}

	private List<PVector> sortEdges(List<PVector> unsorted) {
		List<PVector> result = new ArrayList<>();
		List<PVector> sortedX = new ArrayList<PVector>(unsorted);
		List<PVector> sortedY = new ArrayList<PVector>(unsorted);

		Collections.sort(sortedX, new VectorComparator(0));
		Collections.sort(sortedY, new VectorComparator(1));

		result.add(inter(sortedX.subList(0, 2), sortedY.subList(0, 2)));
		result.add(inter(sortedX.subList(2, 4), sortedY.subList(0, 2)));
		result.add(inter(sortedX.subList(2, 4), sortedY.subList(2, 4)));
		result.add(inter(sortedX.subList(0, 2), sortedY.subList(2, 4)));

		for (int i = 0; i < 4; i++) {
			if (result.get(i) == null) {
				return new ArrayList<>();
			}
		}

		return result;
	}

	private PVector inter(List<PVector> l1, List<PVector> l2) {
		for (int i = 0; i < l1.size(); i++) {
			for (int j = 0; j < l2.size(); j++) {
				if (l1.get(i).equals(l2.get(j))) {
					return l1.get(i);
				}
			}
		}
		return null;
	}

	public ArrayList<PVector> getIntersections(List<PVector> lines) {
		ArrayList<PVector> intersections = new ArrayList<PVector>();
		for (int i = 0; i < lines.size() - 1; i++) {
			PVector line1 = lines.get(i);
			for (int j = i + 1; j < lines.size(); j++) {
				PVector line2 = lines.get(j);
				// compute the intersection and add it to 'intersections'
				float d = (float) (Math.cos(line2.y) * Math.sin(line1.y) - Math
						.cos(line1.y) * Math.sin(line2.y));
				float x = (float) ((line2.x * Math.sin(line1.y) - line1.x
						* Math.sin(line2.y)) / d);
				float y = (float) ((-line2.x * Math.cos(line1.y) + line1.x
						* Math.cos(line2.y)) / d);

				if (x >= 0 && x <= edgeImg.width && y >= 0
						&& y <= edgeImg.height) {
					intersections.add(new PVector(x, y));
				}
			}
		}
		return intersections;
	}

	public PImage thresholding(PImage img, float threshold) {
		PImage result = parent.createImage(edgeImg.width, edgeImg.height,
				PApplet.ALPHA);
		for (int i = 0; i < img.width * img.height; i++) {
			if (parent.brightness(img.pixels[i]) > threshold) {
				result.pixels[i] = parent.color(255);
			}
		}
		return result;
	}

	public PImage saturationThresholding(PImage img, float threshold) {
		PImage result = parent.createImage(edgeImg.width, edgeImg.height,
				PApplet.ALPHA);
		for (int i = 0; i < img.width * img.height; i++) {
			if (parent.saturation(img.pixels[i]) > threshold) {
				result.pixels[i] = img.pixels[i];
			} else {
				result.pixels[i] = parent.color(0);
			}
		}
		return result;

	}

	public PImage colorThresholding(PImage img, float hueFloor, float hueCeil) {
		PImage result = parent.createImage(edgeImg.width, edgeImg.height,
				PApplet.RGB);
		for (int i = 0; i < img.width * img.height; i++) {
			float hue = parent.hue(img.pixels[i]);
			if (hue < hueFloor || hue > hueCeil) {
				result.pixels[i] = parent.color(0);
			} else {
				result.pixels[i] = img.pixels[i];
			}
		}
		return result;
	}

	public PImage gaussianBlur(PImage img) {
		PImage result = parent
				.createImage(img.width, img.height, PApplet.ALPHA);
		float[][] gaussianKernel = { { 9, 12, 9 }, { 12, 15, 12 }, { 9, 12, 9 } };
		float[] convol = convolute(img, gaussianKernel, 99.f);
		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = parent.color(convol[i]);
		}
		return result;
	}

	public PImage sobel(PImage img, float threshold) {
		float[][] hKernel = { { 0, 1, 0 }, { 0, 0, 0 }, { 0, -1, 0 } };
		float[][] vKernel = { { 0, 0, 0 }, { 1, 0, -1 }, { 0, 0, 0 } };
		PImage result = parent
				.createImage(img.width, img.height, PApplet.ALPHA);
		// clear the image
		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = parent.color(0);
		}
		float[] sum_h = convolute(img, hKernel, 1.f);
		float[] sum_v = convolute(img, vKernel, 1.f);
		float sum_hv;
		float max = 0;
		float[] buffer = new float[img.width * img.height];

		for (int i = 0; i < img.width * img.height; i++) {
			sum_hv = (float) Math.sqrt(Math.pow(sum_h[i], 2)
					+ Math.pow(sum_v[i], 2));
			max = Math.max(max, sum_hv);
			buffer[i] = sum_hv;
		}

		for (int y = 2; y < img.height - 2; y++) { // Skip top and bottom edges
			for (int x = 2; x < img.width - 2; x++) { // Skip left and right
				if (buffer[y * img.width + x] > (int) (max * threshold)) {
					result.pixels[y * img.width + x] = parent.color(255);
				} else {
					result.pixels[y * img.width + x] = parent.color(0);
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
						sum += parent.brightness(img.pixels[kernelCord])
								* kernel[i + 1][j + 1];
					}
				}
			}
			result[px] = sum / weight;
		}
		return result;
	}

}