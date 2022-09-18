package me.pride.spirits.util.objects;

public class LinearInterpolator implements Interpolator {
	@Override
	public double[] interpolate(double from, double to, int max) {
		final double[] res = new double[max];
		for (int i = 0; i < max; i++) {
			res[i] = from + i * ((to - from) / (max - 1));
		}
		return res;
	}
}