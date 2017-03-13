package demo.tools;

import org.bytedeco.javacpp.opencv_core.CvSeq;

public abstract class SequenceFrameFilterBase<T> implements FrameFilter<T> {
	
	protected FrameFilter<T> frameFilter;
	
	public SequenceFrameFilterBase(FrameFilter<T> frameFilter)
	{
		this.frameFilter = frameFilter;
	}
	
	public CvSeq execute(T image, CvSeq catches)
	{
		return frameFilter.execute(image, catches);
	}
	
}
