package imageProcessing;

import processing.core.PApplet;
import processing.core.PImage;

@SuppressWarnings("serial")
public class ImageProcessing extends PApplet {
	PImage img;
	HScrollbar bar1 ,bar2, bar3, bar4;
	int shiftMode = 0;
	final int modeNb = 6;

	public void setup() {
		size(800, 600);
		img = loadImage("board1.jpg");
		bar1 = new HScrollbar(this, 0, 590, 800, 10);
		bar2 = new HScrollbar(this, 0, 575, 800, 10);
		bar3 = new HScrollbar(this, 0, 560, 800, 10);
		bar4 = new HScrollbar(this, 0, 545, 800, 10);
	}

	public void draw() {
		background(color(0, 0, 0));
		switch (shiftMode) {
		// part I step 1-2
		case 0:
			noLoop();
			image(thresholding(img, 128), 0, 0);
			break;
		// part I step 3
		case 1:
			image(thresholding(img, bar3.getPos() * 255), 0, 0);
			bar3.display();
			bar3.update();
			break;
		// part I step 4
		case 2:
			image(colorThresholding(img, bar1.getPos() * 255, bar2.getPos() * 255), 0, 0);
			println("ceil: "+ bar1.getPos() * 255 + " floor: " + bar2.getPos() * 255);
			bar1.display();
			bar2.display();
			bar1.update();
			bar2.update();
			break;
		// step II step 1-2
		case 3:
			noLoop();
			image(gaussianBlur(img), 0, 0);
			break;
		// part II step 3
		case 4:
			noLoop();
			image(sobel(img, 0.3f), 0, 0);
			break;
		// part III
		case 5:
			image(boardDetection(img, bar1.getPos() * 255, bar2.getPos() * 255, bar3.getPos() * 255, bar4.getPos()), 0, 0);
			bar1.display();
			bar2.display();
			bar3.display();
			bar4.display();
			bar1.update();
			bar2.update();
			bar3.update();
			bar4.update();
			break;
		default:
		}
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
	
	PImage boardDetection(PImage img, float hueFloor, float hueCeil, float brThreshold, float sobelThreshold) {
		PImage result = img;
		result = colorThresholding(result, hueFloor, hueCeil);
		//result = gaussianBlur(result);
		result = thresholding(result, brThreshold);
		result = sobel(result, sobelThreshold);
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

	public void keyPressed() {
		if (key == CODED) {
			switch (keyCode) {
			case SHIFT:
				shiftMode += 1;
				if (shiftMode >= modeNb) {
					shiftMode = 0;
				}
				loop();
			default:
				break;
			}
		}
	}

}