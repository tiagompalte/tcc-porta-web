package br.com.utfpr.porta.validacao;

import java.nio.ByteBuffer;

public class Algorithm {
	
	private static double TOLERANCIA = 0.1;
	private static int NUM_AMOSTRAS = 96000; //2*48000
	private static int DELAY_MAX = 480;	
	
	private static float maxFloat(int nElem, float[] buffer) {
		
		float maior = 0;

		for (int i=0; i<nElem; i++) {
			if (buffer[i] > maior) {
				maior = buffer[i];
			}
		}

		return maior;
	}
	
	private static void normalizar(float[] bufferDatabase, float[] bufferRecebido) {
		
		float maxValue;
		int i;

		maxValue = maxFloat(NUM_AMOSTRAS, bufferDatabase);

		for (i=0; i<NUM_AMOSTRAS + 2*DELAY_MAX; i++) {
			bufferDatabase[i] = (bufferDatabase[i])/(maxValue);
		}

		maxValue = maxFloat(NUM_AMOSTRAS, bufferRecebido);

		for (i=0; i<NUM_AMOSTRAS + 2*DELAY_MAX; i++) {
			bufferRecebido[i] = (bufferRecebido[i])/(maxValue);
		}
	}

	private static float autoCorr(float[] buffer) {
		
		float rAuto = 0;

		for (int n = 0; n < (NUM_AMOSTRAS + 2*DELAY_MAX) - 1; n++) {
			rAuto += buffer[n]*buffer[n];
		}
		
		return rAuto;
	}

	private static float crossCorr(float[] bufferDatabase, float[] bufferRecebido) {
		
		float[] rCross = new float[2*DELAY_MAX + 1];
		
		for (int k = -DELAY_MAX; k <= DELAY_MAX; k++) {
			for (int n = 0; n < (NUM_AMOSTRAS + 2*DELAY_MAX) - 1; n++) {
				rCross[DELAY_MAX + k] += bufferDatabase[n]*bufferRecebido[n-k];
			}
		}

		return maxFloat(2*DELAY_MAX + 1, rCross);
	}
	
	private static float[] floatMe(short[] pcms) {
	    float[] floaters = new float[pcms.length];
	    for (int i = 0; i < pcms.length; i++) {
	        floaters[i] = pcms[i];
	    }
	    return floaters;
	}
	
	private static short[] shortMe(byte[] bytes) {
	    short[] out = new short[bytes.length / 2]; // will drop last byte if odd number
	    ByteBuffer bb = ByteBuffer.wrap(bytes);
	    for (int i = 0; i < out.length; i++) {
	        out[i] = bb.getShort();
	    }
	    return out;
	}
	
	public static boolean validate(byte[] audioDatabase, byte[] audioRecebido) {
		
		float[] bufferDatabase = floatMe(shortMe(audioDatabase));
		float[] bufferRecebido = floatMe(shortMe(audioRecebido));
		
		normalizar(bufferDatabase, bufferRecebido);
		double coef = crossCorr(bufferDatabase, bufferRecebido) / Math.sqrt(autoCorr(bufferDatabase)*autoCorr(bufferRecebido));
		
		return coef >= (1 - TOLERANCIA);
	}

}
