package demo.tools;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;

import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;

public class FaceScanner {

	List<FaceDetector> detectors = new ArrayList<FaceDetector>();

	public List<IplImage> Detect(IplImage image) throws Exception {
		if (detectors.size() == 0) {
			throw new Exception("No detectors found to perform recognition");
		}

		List<CvRect> rects = new ArrayList<CvRect>();
		List<IplImage> faces = new ArrayList<IplImage>();

		for (FaceDetector detector : detectors) {
			CvSeq catches = detector.detect(image);

			for (int i = 0; i < catches.total(); i++) {
				CvRect rectNext = new CvRect(cvGetSeqElem(catches, i));

				IplImage ROIFrame = image.clone();
				cvSetImageROI(ROIFrame, rectNext);
				faces.add(ROIFrame);

			}
			detector.mark(CvScalar.RED);
		}

		return faces;
	}

	public void AddDetector(FaceDetector detector) {
		detectors.add(detector);
	}
}
