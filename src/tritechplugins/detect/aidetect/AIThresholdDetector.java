package tritechplugins.detect.aidetect;

import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import ai.onnxruntime.OrtSession.SessionOptions;
import ai.onnxruntime.OrtSession.SessionOptions.OptLevel;

import tritechgemini.detect.DetectedRegion;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.detect.threshold.ChannelDetector;
import tritechplugins.detect.threshold.ThresholdDetector;
import tritechplugins.detect.threshold.ThresholdProcess;
/**
 * Simple wrap around Ben's detector copying code from
 * https://github.com/OniDaito/oceanmotion/blob/main/wrappers/java/oceanmotion/src/main/java/oceanmotion/WrapperProg.java
 * 
 * @author dg50
 *
 */
public class AIThresholdDetector extends ChannelDetector {

	private static final int WINDOW_LENGTH = 16;
	private static final int IMG_HEIGHT = 816;
	private static final int IMG_WIDTH = 256;
	private static final float CONFIDENCE = 0.8f;
	private OrtEnvironment env;
	Vector<float[][]> queue = new Vector<>();
	float[][][][][] stack = new float[1][1][WINDOW_LENGTH][IMG_HEIGHT][IMG_WIDTH]; // B,C,D,H,W

	private String modelFile = "C:\\Users\\dg50\\Pamguard_deep_learning\\model.onnx";
	private OrtSession session;
	private String inputName;

	public AIThresholdDetector(ThresholdDetector thresholdDetector, ThresholdProcess thresholdProcess, int sonarId) {
		super(thresholdDetector, thresholdProcess, sonarId);

		// Load the ONNX Model
		env = OrtEnvironment.getEnvironment();
		OrtSession.SessionOptions opts = new SessionOptions();
		try {
			opts.setOptimizationLevel(OptLevel.ALL_OPT);

			session = env.createSession(modelFile, opts);

			//            logger.info("Inputs:");
			//            for (NodeInfo i : session.getInputInfo().values()) {
			//                logger.info(i.toString());
			//            }

			//            logger.info("Outputs:");
			//            for (NodeInfo i : session.getOutputInfo().values()) {
			//                logger.info(i.toString());
			//            }

			// Now loop through all the FITS files, building up the queue till
			// it reaches 16 - the window size for this model.

			inputName = session.getInputNames().iterator().next();
			//            assert fitsFiles.size() > WINDOW_LENGTH;
			//            Vector<BufferedImage> final_frames = new Vector<>();
			//            
			//            // This line isn't necessary really but it cuts down on the number of fits we want to process.
			//            List<String> subfiles = fitsFiles.subList(0, 32);
		} catch (OrtException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<DetectedRegion> newData(ImageDataUnit imageDataUnit) {
		if (session == null) {
			return null;
		}
		float[][] imData = prepareImage(imageDataUnit);
		if (imData == null) {
			return null;
		}
		queue.add(imData);
		if (queue.size() > WINDOW_LENGTH) {
			queue.remove(0);
		}
		if (queue.size() < WINDOW_LENGTH) {
			return null;
		}
		for (int i = 0; i < WINDOW_LENGTH; i++) {
			float[][] frame = queue.get(i);
			stack[0][0][i] = frame;
		}
		OnnxTensor stack_data;
		OnnxValue val = null;
		long tic = System.currentTimeMillis();
		long toc1 = tic;
		try {
			stack_data = OnnxTensor.createTensor(env, stack);
			toc1 = System.currentTimeMillis();
			Map<String, OnnxTensor> sMap = Collections.singletonMap(inputName, stack_data);
			Result output = session.run(sMap);
			val = output.get(0);
		} catch (OrtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		long toc2 = System.currentTimeMillis();
		System.out.printf("Inference time %d and %d millis\n", toc1-tic, toc2-toc1);
		if (val.getType() != OnnxValue.OnnxValueType.ONNX_TYPE_TENSOR) {
			return null;
		}
        OnnxTensor pred_tensor = (OnnxTensor)val;
        FloatBuffer fb = pred_tensor.getFloatBuffer();
        /*
         * The floatbuffer has a value for every input pixel in every frame, i.e. 256x816x16 values. 
         * Ben only looked at the last frame, so running many many predictions. Might be worth trying
         * to speed this up a LOT by running overlapping groups and taking an OR for each underlying 
         * frame, but do what he's done for now. 
         */
        // Move to the last frame position
        int num_pixels = IMG_HEIGHT * IMG_WIDTH;
        int last_frame_pos = IMG_HEIGHT * IMG_WIDTH * (WINDOW_LENGTH-1);
        fb = fb.position(last_frame_pos);

        /**
         * Need to 
         */
        for (int i = 0; i < num_pixels; i++) {
            float value = fb.get();
            // Make sure to pass through the sigmoid confidence value.
            value = sigmoid(value);

            if (value >= CONFIDENCE) {
                value = 1.0f;
            } else {
                value = 0.0f;
            }

            int y = i / IMG_WIDTH;
            int x = i % IMG_WIDTH;
        }
   
		


		return null;
	}
	
	public static float sigmoid(float x) {
		return 1.0f / (1.0f + (float)Math.exp(-x));
	}

	float[][] prepareImage(ImageDataUnit imageDataUnit) {
		if (imageDataUnit == null) {
			return null;
		}
		GeminiImageRecordI gemImage = imageDataUnit.getGeminiImage();
		short[] imData = gemImage.getShortImageData();
		int nBeam = gemImage.getnBeam();
		int nRange = gemImage.getnRange();
		float[][] data = new float[IMG_HEIGHT][IMG_WIDTH];
		int hStep = 2;
		int vStep = 2;
		for (int iX = 0; iX < IMG_WIDTH; iX++) {
			for (int iY = 0; iY < IMG_HEIGHT; iY++) {
				int iD = iY*2*nBeam+iX*2;
				if (iD > imData.length) {
					break;
				}
				data[iY][iX] = imData[iD]/(float)255.0;
			}
		}
		return data;
	}

}
