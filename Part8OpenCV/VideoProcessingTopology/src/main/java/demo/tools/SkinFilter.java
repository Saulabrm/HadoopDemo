package demo.tools;

import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvInRangeS;
import static org.bytedeco.javacpp.opencv_core.cvScalar;
import static org.bytedeco.javacpp.opencv_imgproc.CHAIN_APPROX_SIMPLE;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2YCrCb;
import static org.bytedeco.javacpp.opencv_imgproc.RETR_EXTERNAL;
import static org.bytedeco.javacpp.opencv_imgproc.contourArea;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.drawContours;
import static org.bytedeco.javacpp.opencv_imgproc.findContours;

import org.bytedeco.javacpp.helper.opencv_core.AbstractScalar;

import static org.bytedeco.javacpp.opencv_core.*;

public class SkinFilter extends SequenceFrameFilterBase<IplImage> {

	public static int SkinThreshhold = 35;
	
	protected boolean markArea = false;
	
	public SkinFilter(FrameFilter<IplImage> frameFilter, boolean markArea) {
		super(frameFilter);
		this.markArea = markArea;
		
	}

	@Override
	public CvSeq execute(IplImage image, CvSeq catches) {		
		if (frameFilter!=null) catches = frameFilter.execute(image, catches);
		locateSkin(image, catches);
		return catches;
	}

	private void locateSkin(IplImage image, CvSeq catches) {

		for (int i = 0; i < catches.total(); i++) {
			CvRect rect = new CvRect(cvGetSeqElem(catches, i));
			IplImage ROIFrame = image;
			cvSetImageROI(ROIFrame, rect);
			int persentage = getSkinPercentageInFrame(new Mat(ROIFrame));
			if (persentage < SkinThreshhold)
			{
				cvSeqRemove(catches, i);
			}				
			cvSetImageROI(image, new CvRect(0, 0, image.width(), image.height()));
		}
	}

	private int getSkinPercentageInFrame(Mat original) {

		IplImage imageWithPhotoFilter = new IplImage(original.clone());

		cvtColor(original, new Mat(imageWithPhotoFilter), COLOR_BGR2YCrCb);

		CvScalar min = cvScalar(0, 133, 77, 0);
		CvScalar max = cvScalar(255, 173, 127, 0);

		IplImage imgSkin = cvCreateImage(cvGetSize(imageWithPhotoFilter), 8, 1);

		cvInRangeS(imageWithPhotoFilter, min, max, imgSkin);

		final MatVector skinContours = new MatVector();
		findContours(new Mat(imgSkin), skinContours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

		if (markArea) drawContours(original, skinContours, -1, AbstractScalar.GREEN);

		double totalSize = 0;
		for (int i = 0; i < skinContours.size(); i++) {
			totalSize = totalSize + contourArea(skinContours.get(i));
		}

		int persentage = (int) ((totalSize / (original.size().width() * original.size().height())) * 100);

		return persentage;
	}

}
