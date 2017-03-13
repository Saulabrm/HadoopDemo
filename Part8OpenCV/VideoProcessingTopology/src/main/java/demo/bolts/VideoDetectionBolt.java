package demo.bolts;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import demo.tools.FaceDetector;
import demo.tools.FaceScanner;

public class VideoDetectionBolt extends BaseBasicBolt {

	private static Logger logger = Logger.getLogger(VideoDetectionBolt.class.getName());

	public static final String XML_FILE_FACE = "/tmp/haarcascade_frontalface_alt.xml";

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		try {
			
			byte[] data = Base64.getDecoder().decode(tuple.getString(1));

			logger.info("Got new message " + data.length);

			List<IplImage> images = Grab(data);
			
			logger.info("Located  " + data.length + " images");

			DetectFaces(images);

			byte[] updatedVideo = SaveToVideo(images);

			String dataStr = Base64.getEncoder().encodeToString(updatedVideo);

			logger.info("Video processed. Emitting back to sender - " + dataStr.length());

			collector.emit(new Values(tuple.getValue(0), dataStr));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void DetectFaces(List<IplImage> images) {
		int size = images.size();

		FaceScanner searcher = new FaceScanner();
		FaceDetector frontalDetector = new FaceDetector(XML_FILE_FACE);
		frontalDetector.WithSkinDetection(false);
		searcher.AddDetector(frontalDetector);

		logger.info("Face detector created");

		for (int i = 0; i < size; i++) {

			IplImage img = images.get(i);

			try {
				List<IplImage> faces = searcher.Detect(img);
			} catch (Exception e) {

				e.printStackTrace();
			}

			IplImage imageWithPhotoFilter = new IplImage(img.clone());

			Mat imgmt = new Mat(img);

			images.set(i, imageWithPhotoFilter);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("id", "video"));
	}

	private static List<IplImage> Grab(byte[] data) throws IOException {

		List<IplImage> images = new ArrayList<IplImage>();
		InputStream bis = new ByteArrayInputStream(data);

		FileOutputStream fos = new FileOutputStream("/tmp/tmp.avi");
		fos.write(data);
		fos.close();

		logger.info("File saved to temp dir. Trying to read frames");

		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("/tmp/tmp.avi");

		try {
			grabber.start();

			int frame_count = grabber.getLengthInFrames();

			logger.info("Found " + frame_count + " frames");

			for (int i = 0; i < frame_count; i++) {

				grabber.setFrameNumber(i);
				Frame frame = grabber.grabFrame();

				if (frame == null)
					break;
				if (frame.image == null)
					continue;

				OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
				IplImage grabbedImage = converter.convert(frame);

				IplImage grayImage = grabbedImage.clone();

				images.add(grayImage);
			}

			grabber.stop();

			logger.info("Grabbing finished. Processes images - " + images.size());

		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
		return images;
	}

	private static byte[] SaveToVideo(List<IplImage> images)
			throws org.bytedeco.javacv.FrameRecorder.Exception, IOException {
		File newFile = new File("/tmp/tmp.avi");

		FFmpegFrameRecorder fr = new FFmpegFrameRecorder(newFile, 640, 480);
		fr.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		fr.setFormat("mp4");

		fr.start();

		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

		for (int i = 0; i < images.size(); i++) {
			IplImage img = images.get(i);
			Frame frame = converter.convert(img);

			fr.record(frame);
		}

		fr.stop();
		byte[] data = Files.readAllBytes(Paths.get("/tmp/tmp.avi"));

		return data;
	}
}