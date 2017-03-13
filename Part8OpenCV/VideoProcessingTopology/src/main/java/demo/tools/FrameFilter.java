package demo.tools;

import org.bytedeco.javacpp.opencv_core.CvSeq;

public interface FrameFilter<T> {

	CvSeq execute(T image, CvSeq catches);
}
