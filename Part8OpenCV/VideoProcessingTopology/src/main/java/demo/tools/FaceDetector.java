package demo.tools;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;

public class FaceDetector implements FeatureDetector {

	CvHaarClassifierCascade cascadeInstance;

	FrameFilter<IplImage> filter;

	CvSeq detectedSeq;

	IplImage image;

	public FaceDetector(String historyLocation) {
		if (cascadeInstance == null) {
			cascadeInstance = new CvHaarClassifierCascade(cvLoad(historyLocation));
		}
	}

	@Override
	public CvSeq detect(IplImage src) {
		image = src;
		CvMemStorage storage = CvMemStorage.create();
		CvSeq signs = cvHaarDetectObjects(src, cascadeInstance, storage, 2, 1, 1);
		if (signs.total() > 0) {

			cvClearMemStorage(storage);

			if (filter != null) {
				signs = filter.execute(src, signs);
			}
		}

		detectedSeq = signs;
		return signs;
	}

	@Override
	public void mark(CvScalar color) {
		if (detectedSeq != null && image != null) {
			for (int i = 0; i < detectedSeq.total(); i++) {
				CvRect rect = new CvRect(cvGetSeqElem(detectedSeq, i));
				cvRectangle(image, cvPoint(rect.x(), rect.y()),
						cvPoint(rect.width() + rect.x(), rect.height() + rect.y()), color, 2, CV_AA, 0);
			}
		}
	}

	public void WithSkinDetection(boolean markSkin) {
		filter = new SkinFilter(filter, markSkin);
	}
}
