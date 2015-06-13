package cameraLineDetection;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

@SuppressWarnings("serial")
public class liveControl extends PApplet {
	/*
	 * BLOCK D'INITIALIATION DES SEUILS
	 */
	private final float SATURATION_THRESHOLD = 80.f;
	private final float HUE_FLOOR = 100.f;
	private final float HUE_CEIL = 140.f;
	private final float BRIGHTNESS_THRESHOLD = 25.f;
	private final float SOBEL_THRESHOLD = 0.3f;
	/*
	 * ****************************
	 * ****************************
	 */

	Capture cam;
	PImage img;

	public void setup() {
		size(640, 480);
		//img = loadImage("tux_error.jpg");
		String[] cameras = Capture.list();
		if (cameras.length == 0) {
			println("There are no cameras available for capture.");
			exit();
		} else {
			println("Available cameras:");
			for (int i = 0; i < cameras.length; i++) {
				println(cameras[i]);
			}
			cam = new Capture(this, cameras[0]);
			cam.start();
		}
	}

	public void draw() {
		if (cam.available() == true) {
			cam.read();
		}
		img = cam.get();
		image(img, 0, 0);
		spotLines(boardDetection(img));
	}

	public void spotLines(PImage edgeImg) {
		float discretizationStepsPhi = 0.06f;
		float discretizationStepsR = 2.5f;
		// dimensions of the accumulator
		int phiDim = (int) (Math.PI / discretizationStepsPhi);
		int rDim = (int) (((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);
		// println("imgWidth:" + edgeImg.width + "\nimgheight:" + edgeImg.height
		// + "\nphiDim:" + phiDim + "\nrDim:" + rDim);
		// our accumulator (with a 1 pix margin around)
		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];
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

		for (int idx = 0; idx < accumulator.length; idx++) {
			if (accumulator[idx] > 200) {
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
				int x3 = Math.round(-(y3 - r / sin(phi))
						* (sin(phi) / cos(phi)));

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
		}
	}

	public PImage boardDetection(PImage img) {
		PImage result = img;
		result = colorThresholding(result, HUE_FLOOR, HUE_CEIL);
		result = saturationThresholding(result, SATURATION_THRESHOLD);
		result = gaussianBlur(result);
		result = thresholding(result, BRIGHTNESS_THRESHOLD);
		result = sobel(result, SOBEL_THRESHOLD);
		return result;
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