package demo.tools;

import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;

public interface FeatureDetector {

	CvSeq detect(IplImage src);
	
	void mark(CvScalar color);
}
